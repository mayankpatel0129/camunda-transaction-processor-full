package com.example.transactionprocessor.controller;

import com.example.transactionprocessor.exception.BusinessRuleException;
import com.example.transactionprocessor.model.TransactionRequest;
import com.example.transactionprocessor.service.HighThroughputTransactionService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.camunda.bpm.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v2/transactions")
public class EnhancedTransactionController {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedTransactionController.class);

    @Autowired
    private RuntimeService runtimeService;
    
    @Autowired
    private HighThroughputTransactionService highThroughputService;
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    private final AtomicLong requestCounter = new AtomicLong(0);
    private Counter httpRequestCounter;
    private Counter businessRuleViolationCounter;
    private Counter systemErrorCounter;

    @PostConstruct
    public void initMetrics() {
        httpRequestCounter = Counter.builder("http.requests.total")
            .description("Total HTTP requests received")
            .tag("endpoint", "/api/v2/transactions/process")
            .register(meterRegistry);
            
        businessRuleViolationCounter = Counter.builder("business.rule.violations")
            .description("Business rule violations")
            .register(meterRegistry);
            
        systemErrorCounter = Counter.builder("system.errors")
            .description("System errors")
            .register(meterRegistry);
    }

    @PostMapping("/process")
    @Timed(value = "transaction.processing.controller", description = "Time taken to process transaction request")
    public ResponseEntity<Map<String, Object>> processTransaction(@RequestBody TransactionRequest request) {
        String requestId = "REQ-" + System.currentTimeMillis() + "-" + requestCounter.getAndIncrement();
        Instant startTime = Instant.now();
        
        // Set up MDC for request tracking
        MDC.put("requestId", requestId);
        MDC.put("transactionType", request.getTransactionType());
        
        try {
            httpRequestCounter.increment();
            logger.info("Received high-throughput transaction request: {} - Type: {}, Amount: {}", 
                requestId, request.getTransactionType(), request.getAmount());
            
            // Use high-throughput service for async processing
            CompletableFuture<String> futureResult = highThroughputService.processTransactionAsync(request);
            
            // For demo purposes, we'll also start the traditional Camunda process
            String processInstanceId = startCamundaProcess(request);
            
            // Get the async result (in production, you might want to return immediately and use callbacks)
            String asyncResult = futureResult.get(); // This will block, but it's optimized internally
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Transaction processed successfully");
            response.put("requestId", requestId);
            response.put("processInstanceId", processInstanceId);
            response.put("asyncResult", asyncResult);
            response.put("transactionType", request.getTransactionType());
            response.put("amount", request.getAmount());
            response.put("currency", request.getCurrency());
            response.put("processingTimeMs", Instant.now().toEpochMilli() - startTime.toEpochMilli());
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Transaction processed successfully - Request: {}, Process: {}, Result: {}, Time: {}ms", 
                requestId, processInstanceId, asyncResult, 
                Instant.now().toEpochMilli() - startTime.toEpochMilli());
            
            return ResponseEntity.ok(response);
            
        } catch (BusinessRuleException e) {
            businessRuleViolationCounter.increment();
            logger.warn("Business rule violation - Request: {} - Code: {}, Reason: {}", 
                requestId, e.getBusinessCode(), e.getBusinessReason());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "business_rule_violation");
            errorResponse.put("requestId", requestId);
            errorResponse.put("businessCode", e.getBusinessCode());
            errorResponse.put("message", e.getBusinessReason());
            errorResponse.put("processingTimeMs", Instant.now().toEpochMilli() - startTime.toEpochMilli());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            systemErrorCounter.increment();
            logger.error("System error processing transaction - Request: {} - Error: {}", 
                requestId, e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("requestId", requestId);
            errorResponse.put("message", "Internal server error occurred while processing transaction");
            errorResponse.put("processingTimeMs", Instant.now().toEpochMilli() - startTime.toEpochMilli());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        } finally {
            MDC.clear();
        }
    }
    
    private String startCamundaProcess(TransactionRequest request) {
        // Start the Camunda process with the transaction data
        Map<String, Object> variables = new HashMap<>();
        variables.put("transactionType", request.getTransactionType());
        variables.put("amount", request.getAmount());
        variables.put("currency", request.getCurrency());
        variables.put("transactionDateTime", request.getTransactionDateTime());
        variables.put("description", request.getDescription());
        variables.put("referenceNumber", request.getReferenceNumber());
        
        // Credit card information
        if (request.getCreditCardInfo() != null) {
            variables.put("cardNumber", request.getCreditCardInfo().getCardNumber());
            variables.put("holderName", request.getCreditCardInfo().getHolderName());
            variables.put("expiryDate", request.getCreditCardInfo().getExpiryDate());
            variables.put("cvv", request.getCreditCardInfo().getCvv());
            variables.put("cardType", request.getCreditCardInfo().getCardType());
        }
        
        // Billing address
        if (request.getBillingAddress() != null) {
            variables.put("billingStreet", request.getBillingAddress().getStreet());
            variables.put("billingCity", request.getBillingAddress().getCity());
            variables.put("billingState", request.getBillingAddress().getState());
            variables.put("billingZip", request.getBillingAddress().getZipCode());
            variables.put("billingCountry", request.getBillingAddress().getCountry());
            variables.put("country", request.getBillingAddress().getCountry());
        }
        
        // Vendor information
        if (request.getVendorInfo() != null) {
            variables.put("vendorName", request.getVendorInfo().getName());
            variables.put("vendorLocation", request.getVendorInfo().getLocation());
            variables.put("merchantId", request.getVendorInfo().getMerchantId());
            variables.put("vendorCategory", request.getVendorInfo().getCategory());
        }
        
        // Customer name
        if (request.getCreditCardInfo() != null && request.getCreditCardInfo().getHolderName() != null) {
            variables.put("customerName", request.getCreditCardInfo().getHolderName());
        }
        
        // Transaction-specific fields
        if ("Refund".equals(request.getTransactionType())) {
            variables.put("originalTransactionId", request.getOriginalTransactionId());
            variables.put("refundReason", request.getRefundReason());
        }
        
        if ("Chargeback".equals(request.getTransactionType())) {
            variables.put("chargebackReason", request.getChargebackReason());
            variables.put("chargebackCode", request.getChargebackCode());
            variables.put("liabilityShift", request.getLiabilityShift());
        }
        
        // Start the process
        return runtimeService.startProcessInstanceByKey("transactionProcessingDMNBased", variables)
                .getProcessInstanceId();
    }

    @GetMapping("/health")
    @Timed(value = "transaction.health.check", description = "Health check endpoint response time")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "transaction-processor-enterprise");
        health.put("version", "3.0.0-enterprise");
        health.put("build", "high-throughput-optimized");
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/metrics/performance")
    @Timed(value = "transaction.metrics.performance", description = "Performance metrics endpoint response time")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        HighThroughputTransactionService.PerformanceMetrics metrics = 
            highThroughputService.getPerformanceMetrics();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalTransactions", metrics.totalTransactions);
        response.put("successfulTransactions", metrics.successfulTransactions);
        response.put("failedTransactions", metrics.failedTransactions);
        response.put("activeTransactions", metrics.activeTransactions);
        response.put("availableCapacity", metrics.availableCapacity);
        response.put("successRate", String.format("%.2f%%", metrics.getSuccessRate()));
        response.put("currentTPS", calculateCurrentTPS());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/metrics/system")
    @Timed(value = "transaction.metrics.system", description = "System metrics endpoint response time")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        Runtime runtime = Runtime.getRuntime();
        
        Map<String, Object> systemMetrics = new HashMap<>();
        systemMetrics.put("jvm", Map.of(
            "memoryUsed", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
            "memoryFree", runtime.freeMemory() / (1024 * 1024),
            "memoryTotal", runtime.totalMemory() / (1024 * 1024),
            "memoryMax", runtime.maxMemory() / (1024 * 1024),
            "processors", runtime.availableProcessors()
        ));
        
        systemMetrics.put("threads", Map.of(
            "active", Thread.activeCount(),
            "peak", ManagementFactory.getThreadMXBean().getPeakThreadCount(),
            "daemon", ManagementFactory.getThreadMXBean().getDaemonThreadCount(),
            "total", ManagementFactory.getThreadMXBean().getTotalStartedThreadCount()
        ));
        
        systemMetrics.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(systemMetrics);
    }
    
    private double calculateCurrentTPS() {
        // This would typically be calculated from metrics over a time window
        // For demonstration, returning a simulated value
        return Math.random() * 1000 + 500; // Simulated 500-1500 TPS
    }
}