package com.example.transactionprocessor.service;

import com.example.transactionprocessor.model.TransactionRequest;
import com.example.transactionprocessor.exception.BusinessRuleException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@Service
public class HighThroughputTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(HighThroughputTransactionService.class);
    
    @Autowired
    @Qualifier("highThroughputExecutor")
    private Executor highThroughputExecutor;
    
    @Autowired
    @Qualifier("batchProcessingExecutor")  
    private Executor batchProcessingExecutor;
    
    @Autowired
    @Qualifier("priorityExecutor")
    private Executor priorityExecutor;
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    // Thread-safe counters and metrics
    private final AtomicLong transactionIdGenerator = new AtomicLong(1);
    private final LongAdder totalTransactions = new LongAdder();
    private final LongAdder successfulTransactions = new LongAdder();
    private final LongAdder failedTransactions = new LongAdder();
    
    // Concurrent rate limiting
    private final Semaphore rateLimiter = new Semaphore(10000); // Max 10K concurrent transactions
    
    // Thread-safe transaction tracking
    private final ConcurrentHashMap<String, TransactionContext> activeTransactions = new ConcurrentHashMap<>();
    
    // Metrics
    private Counter transactionCounter;
    private Counter successCounter;
    private Counter failureCounter;
    private Timer processingTimer;
    private Timer queueTimer;
    
    @PostConstruct
    public void initMetrics() {
        transactionCounter = Counter.builder("transactions.total")
            .description("Total number of transactions processed")
            .register(meterRegistry);
            
        successCounter = Counter.builder("transactions.success")
            .description("Number of successful transactions")
            .register(meterRegistry);
            
        failureCounter = Counter.builder("transactions.failure")
            .description("Number of failed transactions")
            .register(meterRegistry);
            
        processingTimer = Timer.builder("transactions.processing.time")
            .description("Transaction processing time")
            .register(meterRegistry);
            
        queueTimer = Timer.builder("transactions.queue.time")
            .description("Time spent waiting in queue")
            .register(meterRegistry);
    }
    
    /**
     * High-throughput transaction processing with thread safety
     */
    @Async("highThroughputExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED, timeout = 30)
    public CompletableFuture<String> processTransactionAsync(TransactionRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String transactionId = null;
            Timer.Sample sample = Timer.start(meterRegistry);
            Timer.Sample queueSample = Timer.start(meterRegistry);
            
            try {
                // Rate limiting
                if (!rateLimiter.tryAcquire()) {
                    throw new BusinessRuleException("RATE_LIMIT_EXCEEDED", "System is at maximum capacity");
                }
                
                queueSample.stop(queueTimer);
                
                // Generate unique transaction ID
                transactionId = generateTransactionId();
                
                // Set up MDC for thread-safe logging
                MDC.put("transactionId", transactionId);
                MDC.put("transactionType", request.getTransactionType());
                MDC.put("amount", String.valueOf(request.getAmount()));
                
                // Track transaction context
                TransactionContext context = new TransactionContext(transactionId, request, Instant.now());
                activeTransactions.put(transactionId, context);
                
                // Increment counters
                totalTransactions.increment();
                transactionCounter.increment();
                
                logger.info("Processing high-throughput transaction: {} - Type: {}, Amount: {}",
                    transactionId, request.getTransactionType(), request.getAmount());
                
                // Select appropriate executor based on transaction characteristics
                Executor selectedExecutor = selectOptimalExecutor(request);
                
                // Process transaction with thread safety
                String result = processTransactionSafely(transactionId, request, context);
                
                // Update success metrics
                successfulTransactions.increment();
                successCounter.increment();
                
                logger.info("Successfully processed transaction: {} in {} ms", 
                    transactionId, context.getProcessingTimeMs());
                
                return result;
                
            } catch (Exception e) {
                // Update failure metrics
                failedTransactions.increment();
                failureCounter.increment();
                
                logger.error("Failed to process transaction: {} - Error: {}", transactionId, e.getMessage(), e);
                
                if (e instanceof BusinessRuleException) {
                    throw e; // Re-throw business exceptions
                }
                
                throw new RuntimeException("Transaction processing failed: " + e.getMessage(), e);
                
            } finally {
                // Cleanup
                if (transactionId != null) {
                    activeTransactions.remove(transactionId);
                }
                rateLimiter.release();
                sample.stop(processingTimer);
                MDC.clear();
            }
        }, highThroughputExecutor);
    }
    
    /**
     * Thread-safe transaction processing
     */
    private String processTransactionSafely(String transactionId, TransactionRequest request, TransactionContext context) {
        try {
            // Validate request thread-safely
            validateTransactionRequest(request);
            
            // Update context with validation completion
            context.markValidationComplete();
            
            // Process based on transaction type with appropriate thread safety
            String result = switch (request.getTransactionType().toUpperCase()) {
                case "PURCHASE" -> processPurchaseTransaction(transactionId, request, context);
                case "PAYMENT" -> processPaymentTransaction(transactionId, request, context);
                case "ADJUSTMENT" -> processAdjustmentTransaction(transactionId, request, context);
                case "REFUND" -> processRefundTransaction(transactionId, request, context);
                case "CHARGEBACK" -> processChargebackTransaction(transactionId, request, context);
                default -> throw new BusinessRuleException("INVALID_TRANSACTION_TYPE", 
                    "Unsupported transaction type: " + request.getTransactionType());
            };
            
            // Mark processing complete
            context.markProcessingComplete();
            
            return result;
            
        } catch (Exception e) {
            context.markProcessingFailed(e.getMessage());
            throw e;
        }
    }
    
    /**
     * Select optimal executor based on transaction characteristics
     */
    private Executor selectOptimalExecutor(TransactionRequest request) {
        // High-value or priority transactions
        if (request.getAmount().doubleValue() > 10000.0 || isVIPCustomer(request)) {
            return priorityExecutor;
        }
        
        // Batch processing for adjustments and refunds
        if ("ADJUSTMENT".equals(request.getTransactionType()) || "REFUND".equals(request.getTransactionType())) {
            return batchProcessingExecutor;
        }
        
        // Default high-throughput executor
        return highThroughputExecutor;
    }
    
    private boolean isVIPCustomer(TransactionRequest request) {
        return request.getCreditCardInfo() != null && 
               request.getCreditCardInfo().getHolderName() != null &&
               request.getCreditCardInfo().getHolderName().toLowerCase().contains("vip");
    }
    
    private void validateTransactionRequest(TransactionRequest request) {
        if (request == null) {
            throw new BusinessRuleException("INVALID_REQUEST", "Transaction request cannot be null");
        }
        
        if (request.getTransactionType() == null || request.getTransactionType().trim().isEmpty()) {
            throw new BusinessRuleException("INVALID_TRANSACTION_TYPE", "Transaction type is required");
        }
        
        if (request.getAmount() == null) {
            throw new BusinessRuleException("INVALID_AMOUNT", "Transaction amount is required");
        }
    }
    
    // Transaction type processors with thread safety
    private String processPurchaseTransaction(String transactionId, TransactionRequest request, TransactionContext context) {
        logger.debug("Processing purchase transaction: {}", transactionId);
        // Simulate purchase processing
        simulateProcessingDelay(50, 200);
        return "PURCHASE_SUCCESS:" + transactionId;
    }
    
    private String processPaymentTransaction(String transactionId, TransactionRequest request, TransactionContext context) {
        logger.debug("Processing payment transaction: {}", transactionId);
        simulateProcessingDelay(30, 150);
        return "PAYMENT_SUCCESS:" + transactionId;
    }
    
    private String processAdjustmentTransaction(String transactionId, TransactionRequest request, TransactionContext context) {
        logger.debug("Processing adjustment transaction: {}", transactionId);
        simulateProcessingDelay(100, 300);
        return "ADJUSTMENT_SUCCESS:" + transactionId;
    }
    
    private String processRefundTransaction(String transactionId, TransactionRequest request, TransactionContext context) {
        logger.debug("Processing refund transaction: {}", transactionId);
        simulateProcessingDelay(80, 250);
        return "REFUND_SUCCESS:" + transactionId;
    }
    
    private String processChargebackTransaction(String transactionId, TransactionRequest request, TransactionContext context) {
        logger.debug("Processing chargeback transaction: {}", transactionId);
        simulateProcessingDelay(150, 400);
        return "CHARGEBACK_SUCCESS:" + transactionId;
    }
    
    private void simulateProcessingDelay(int minMs, int maxMs) {
        try {
            int delay = (int) (Math.random() * (maxMs - minMs)) + minMs;
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted", e);
        }
    }
    
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + transactionIdGenerator.getAndIncrement();
    }
    
    /**
     * Get current performance metrics
     */
    public PerformanceMetrics getPerformanceMetrics() {
        return new PerformanceMetrics(
            totalTransactions.sum(),
            successfulTransactions.sum(),
            failedTransactions.sum(),
            activeTransactions.size(),
            rateLimiter.availablePermits()
        );
    }
    
    /**
     * Transaction context for thread-safe tracking
     */
    private static class TransactionContext {
        private final String transactionId;
        private final TransactionRequest request;
        private final Instant startTime;
        private volatile Instant validationCompleteTime;
        private volatile Instant processingCompleteTime;
        private volatile String failureReason;
        
        public TransactionContext(String transactionId, TransactionRequest request, Instant startTime) {
            this.transactionId = transactionId;
            this.request = request;
            this.startTime = startTime;
        }
        
        public void markValidationComplete() {
            this.validationCompleteTime = Instant.now();
        }
        
        public void markProcessingComplete() {
            this.processingCompleteTime = Instant.now();
        }
        
        public void markProcessingFailed(String reason) {
            this.failureReason = reason;
        }
        
        public long getProcessingTimeMs() {
            Instant endTime = processingCompleteTime != null ? processingCompleteTime : Instant.now();
            return endTime.toEpochMilli() - startTime.toEpochMilli();
        }
    }
    
    /**
     * Performance metrics data class
     */
    public static class PerformanceMetrics {
        public final long totalTransactions;
        public final long successfulTransactions;
        public final long failedTransactions;
        public final int activeTransactions;
        public final int availableCapacity;
        
        public PerformanceMetrics(long totalTransactions, long successfulTransactions, 
                                long failedTransactions, int activeTransactions, int availableCapacity) {
            this.totalTransactions = totalTransactions;
            this.successfulTransactions = successfulTransactions;
            this.failedTransactions = failedTransactions;
            this.activeTransactions = activeTransactions;
            this.availableCapacity = availableCapacity;
        }
        
        public double getSuccessRate() {
            return totalTransactions > 0 ? (double) successfulTransactions / totalTransactions * 100.0 : 0.0;
        }
    }
}