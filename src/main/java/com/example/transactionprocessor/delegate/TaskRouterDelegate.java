package com.example.transactionprocessor.delegate;

import com.example.transactionprocessor.exception.BusinessRuleException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component("taskRouterDelegate")
public class TaskRouterDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(TaskRouterDelegate.class);
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Value("${app.transaction.thread-pool.core-size:30}")
    private int corePoolSize;
    
    @Value("${app.transaction.thread-pool.max-size:100}")
    private int maxPoolSize;
    
    @Value("${app.transaction.thread-pool.queue-capacity:500}")
    private int queueCapacity;
    
    @Value("${app.transaction.thread-pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;
    
    @Value("${app.transaction.batch.size:100}")
    private int batchSize;
    
    @Value("${app.transaction.batch.timeout-seconds:30}")
    private int batchTimeoutSeconds;
    
    private ThreadPoolExecutor executorService;
    private final AtomicLong transactionCounter = new AtomicLong(0);
    private final AtomicInteger activeTransactions = new AtomicInteger(0);
    private final ConcurrentHashMap<String, AtomicInteger> taskExecutionCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> taskExecutionTimes = new ConcurrentHashMap<>();
    
    private Counter successCounter;
    private Counter failureCounter;
    private Timer processingTimer;
    private Counter parallelTaskCounter;
    private Counter sequentialTaskCounter;

    @PostConstruct
    public void init() {
        // Initialize custom thread pool with monitoring
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "transaction-processor-" + counter.getAndIncrement());
                t.setDaemon(false);
                t.setPriority(Thread.NORM_PRIORITY);
                return t;
            }
        };
        
        executorService = new ThreadPoolExecutor(
            corePoolSize, maxPoolSize, keepAliveSeconds, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(queueCapacity), threadFactory,
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        // Initialize metrics
        successCounter = Counter.builder("transaction.processing.success")
            .description("Number of successful transaction processing")
            .register(meterRegistry);
            
        failureCounter = Counter.builder("transaction.processing.failure")
            .description("Number of failed transaction processing")
            .register(meterRegistry);
            
        processingTimer = Timer.builder("transaction.processing.time")
            .description("Time taken to process transactions")
            .register(meterRegistry);
            
        parallelTaskCounter = Counter.builder("transaction.parallel.tasks")
            .description("Number of parallel tasks executed")
            .register(meterRegistry);
            
        sequentialTaskCounter = Counter.builder("transaction.sequential.tasks")
            .description("Number of sequential tasks executed")
            .register(meterRegistry);
            
        // Register custom metrics
        meterRegistry.gauge("transaction.thread.pool.active", executorService, ThreadPoolExecutor::getActiveCount);
        meterRegistry.gauge("transaction.thread.pool.size", executorService, ThreadPoolExecutor::getPoolSize);
        meterRegistry.gauge("transaction.thread.pool.queue.size", executorService, 
            e -> e.getQueue().size());
        meterRegistry.gauge("transaction.active.count", activeTransactions);
        
        logger.info("TaskRouterDelegate initialized with thread pool - Core: {}, Max: {}, Queue: {}", 
            corePoolSize, maxPoolSize, queueCapacity);
    }
    
    @PreDestroy
    public void destroy() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    @CircuitBreaker(name = "transactionProcessor", fallbackMethod = "fallbackExecute")
    @Retry(name = "transactionProcessor")
    public void execute(DelegateExecution execution) throws Exception {
        Timer.Sample sample = Timer.start(meterRegistry);
        long transactionId = transactionCounter.incrementAndGet();
        activeTransactions.incrementAndGet();
        
        try {
            // Set up MDC for distributed tracing
            MDC.put("transactionId", String.valueOf(transactionId));
            MDC.put("processInstanceId", execution.getProcessInstanceId());
            MDC.put("businessKey", execution.getBusinessKey());
            
            logger.info("Starting transaction processing - ID: {}", transactionId);
            
            // Get taskFlow from DMN result
            String taskFlow = (String) execution.getVariable("dmnResult");
            String transactionType = (String) execution.getVariable("transactionType");
            
            if (taskFlow == null || taskFlow.isEmpty()) {
                logger.error("Task flow cannot be null or empty for transaction: {}", transactionId);
                throw new IllegalArgumentException("Task flow cannot be null or empty");
            }

            logger.info("Executing task flow: {} for transaction type: {} (Transaction ID: {})", 
                taskFlow, transactionType, transactionId);
            
            // Parse the task flow string to determine execution pattern
            if (taskFlow.contains("parallel:")) {
                executeParallelTasks(taskFlow, execution, transactionId);
            } else {
                executeSequentialTasks(taskFlow, execution, transactionId);
            }
            
            successCounter.increment();
            logger.info("Transaction processing completed successfully - ID: {}", transactionId);
            
        } catch (BusinessRuleException e) {
            // Business rule violations are expected and should not trigger circuit breaker
            logger.warn("Business rule violation for transaction - ID: {}, Code: {}, Reason: {}", 
                transactionId, e.getBusinessCode(), e.getBusinessReason());
            
            // Set business rule failure information in process variables
            execution.setVariable("processingFailed", true);
            execution.setVariable("businessRuleViolation", true);
            execution.setVariable("businessCode", e.getBusinessCode());
            execution.setVariable("failureReason", e.getBusinessReason());
            execution.setVariable("failureTimestamp", Instant.now().toString());
            
            // Don't increment failure counter for business rule violations
            // These are expected business behaviors, not system failures
            throw e;
        } catch (Exception e) {
            // System failures should trigger circuit breaker and monitoring
            failureCounter.increment();
            logger.error("System error during transaction processing - ID: {}", transactionId, e);
            
            // Set system failure information in process variables
            execution.setVariable("processingFailed", true);
            execution.setVariable("systemError", true);
            execution.setVariable("failureReason", e.getMessage());
            execution.setVariable("failureTimestamp", Instant.now().toString());
            
            throw e;
        } finally {
            sample.stop(processingTimer);
            activeTransactions.decrementAndGet();
            MDC.clear();
        }
    }
    
    public void fallbackExecute(DelegateExecution execution, Exception ex) throws Exception {
        // If the original exception is a BusinessRuleException, preserve it
        if (ex instanceof BusinessRuleException) {
            logger.warn("Business rule violation in fallback - preserving original exception", ex);
            throw ex;
        }
        
        logger.error("Circuit breaker activated for transaction processing", ex);
        execution.setVariable("processingFailed", true);
        execution.setVariable("failureReason", "Circuit breaker activated: " + ex.getMessage());
        execution.setVariable("failureTimestamp", Instant.now().toString());
        throw new Exception("Transaction processing circuit breaker activated", ex);
    }

    private void executeSequentialTasks(String taskFlow, DelegateExecution execution, long transactionId) throws Exception {
        List<String> tasks = Arrays.asList(taskFlow.split(","));
        sequentialTaskCounter.increment();
        
        logger.info("Executing {} sequential tasks for transaction: {}", tasks.size(), transactionId);
        
        for (String task : tasks) {
            task = task.trim();
            executeTaskWithMetrics(task, execution, transactionId);
        }
    }

    private void executeParallelTasks(String taskFlow, DelegateExecution execution, long transactionId) throws Exception {
        List<String> tasks = Arrays.asList(taskFlow.split(","));
        parallelTaskCounter.increment();
        
        logger.info("Executing {} tasks with parallel processing for transaction: {}", tasks.size(), transactionId);
        
        // Separate sequential and parallel tasks
        List<String> sequentialTasks = tasks.stream()
            .filter(task -> !task.trim().startsWith("parallel:"))
            .collect(Collectors.toList());
            
        List<String> parallelTasks = tasks.stream()
            .filter(task -> task.trim().startsWith("parallel:"))
            .map(task -> task.substring("parallel:".length()).trim())
            .collect(Collectors.toList());
        
        // Execute sequential tasks first
        for (String task : sequentialTasks) {
            executeTaskWithMetrics(task.trim(), execution, transactionId);
        }
        
        // Execute parallel tasks
        if (!parallelTasks.isEmpty()) {
            executeParallelTasksBatch(parallelTasks, execution, transactionId);
        }
    }
    
    private void executeParallelTasksBatch(List<String> tasks, DelegateExecution execution, long transactionId) throws Exception {
        // Extract execution context data that will be needed in parallel tasks
        final String processInstanceId = execution.getProcessInstanceId();
        final String businessKey = execution.getBusinessKey();
        
        // Process in batches for better resource management
        int totalTasks = tasks.size();
        int processed = 0;
        
        while (processed < totalTasks) {
            int batchEnd = Math.min(processed + batchSize, totalTasks);
            List<String> batch = tasks.subList(processed, batchEnd);
            
            logger.info("Processing batch of {} tasks ({}-{} of {}) for transaction: {}", 
                batch.size(), processed + 1, batchEnd, totalTasks, transactionId);
            
            // Execute parallel tasks synchronously to maintain command context
            for (String task : batch) {
                try {
                    executeTaskWithMetrics(task, execution, transactionId);
                } catch (Exception e) {
                    logger.error("Error executing parallel task: {} for transaction: {}", task, transactionId, e);
                    throw e;
                }
            }
            
            processed = batchEnd;
        }
    }

    private void executeTaskWithMetrics(String taskName, DelegateExecution execution, long transactionId) throws Exception {
        Timer.Sample taskSample = Timer.start(meterRegistry);
        Instant startTime = Instant.now();
        
        try {
            executeTask(taskName, execution, transactionId);
            
            // Update metrics
            taskExecutionCounts.computeIfAbsent(taskName, k -> new AtomicInteger(0)).incrementAndGet();
            taskExecutionTimes.computeIfAbsent(taskName, k -> new AtomicLong(0))
                .addAndGet(Duration.between(startTime, Instant.now()).toMillis());
            
            logger.debug("Task {} completed successfully for transaction: {}", taskName, transactionId);
            
        } catch (Exception e) {
            logger.error("Task {} failed for transaction: {}", taskName, transactionId, e);
            
            // Record task failure
            meterRegistry.counter("transaction.task.failure", "task", taskName).increment();
            throw e;
        } finally {
            taskSample.stop(Timer.builder("transaction.task.time")
                .tag("task", taskName)
                .register(meterRegistry));
        }
    }

    private void executeTask(String taskName, DelegateExecution execution, long transactionId) throws Exception {
        try {
            JavaDelegate delegate = getDelegate(taskName);
            if (delegate != null) {
                delegate.execute(execution);
                logger.info("Completed task: {} for transaction: {}", taskName, transactionId);
            } else {
                logger.warn("No delegate found for task: {} - simulating execution for transaction: {}", 
                    taskName, transactionId);
                
                // Simulate task execution with some processing time
                Thread.sleep(50 + (int)(Math.random() * 100));
            }
        } catch (Exception e) {
            logger.error("Error executing task: {} for transaction: {}", taskName, transactionId, e);
            throw e;
        }
    }

    private JavaDelegate getDelegate(String taskName) {
        try {
            String beanName = mapTaskNameToBean(taskName);
            return applicationContext.getBean(beanName, JavaDelegate.class);
        } catch (Exception e) {
            logger.debug("Delegate not found for task: {}", taskName);
            return null;
        }
    }

    private String mapTaskNameToBean(String taskName) {
        switch (taskName.toLowerCase()) {
            case "authorize":
                return "purchaseAuthorizationDelegate";
            case "settle":
                return "purchaseSettlementDelegate";
            case "validate":
                return "paymentValidationDelegate";
            case "post":
                return "paymentPostingDelegate";
            case "review":
                return "adjustmentReviewDelegate";
            case "apply":
                return "adjustmentApplyDelegate";
            case "refundvalidation":
                return "refundValidationDelegate";
            case "refundprocess":
                return "refundProcessDelegate";
            case "investigate":
                return "chargebackInvestigateDelegate";
            case "dispute":
                return "chargebackDisputeDelegate";
            default:
                return taskName + "Delegate";
        }
    }
    
    // Getter methods for monitoring
    public int getActiveTransactionCount() {
        return activeTransactions.get();
    }
    
    public long getTotalTransactionCount() {
        return transactionCounter.get();
    }
    
    public ConcurrentHashMap<String, AtomicInteger> getTaskExecutionCounts() {
        return taskExecutionCounts;
    }
    
    public ConcurrentHashMap<String, AtomicLong> getTaskExecutionTimes() {
        return taskExecutionTimes;
    }
}