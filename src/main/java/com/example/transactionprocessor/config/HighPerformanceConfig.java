package com.example.transactionprocessor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@EnableTransactionManagement
public class HighPerformanceConfig {

    @Bean(name = "highThroughputExecutor")
    public Executor highThroughputExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // High-throughput configuration for 1000s TPS
        executor.setCorePoolSize(500);
        executor.setMaxPoolSize(2000);
        executor.setQueueCapacity(10000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("HighThroughput-");
        
        // Thread safety and performance settings
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        
        executor.initialize();
        return executor;
    }

    @Bean(name = "batchProcessingExecutor")
    public Executor batchProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Optimized for batch processing
        executor.setCorePoolSize(200);
        executor.setMaxPoolSize(800);
        executor.setQueueCapacity(5000);
        executor.setKeepAliveSeconds(120);
        executor.setThreadNamePrefix("BatchProcessing-");
        
        // Batch-specific settings
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        
        executor.initialize();
        return executor;
    }

    @Bean(name = "priorityExecutor")
    public Executor priorityExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // High-priority transactions
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(300);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("Priority-");
        
        // Priority processing settings
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }

    /**
     * Performance monitoring configuration
     */
    @ConfigurationProperties(prefix = "app.performance")
    public static class PerformanceProperties {
        private int maxConcurrentTransactions = 10000;
        private int circuitBreakerThreshold = 1000;
        private long metricsUpdateIntervalMs = 5000;
        private boolean enableDetailedMetrics = true;
        private int threadPoolMonitoringIntervalSeconds = 30;

        // Getters and setters
        public int getMaxConcurrentTransactions() { return maxConcurrentTransactions; }
        public void setMaxConcurrentTransactions(int maxConcurrentTransactions) { 
            this.maxConcurrentTransactions = maxConcurrentTransactions; 
        }

        public int getCircuitBreakerThreshold() { return circuitBreakerThreshold; }
        public void setCircuitBreakerThreshold(int circuitBreakerThreshold) { 
            this.circuitBreakerThreshold = circuitBreakerThreshold; 
        }

        public long getMetricsUpdateIntervalMs() { return metricsUpdateIntervalMs; }
        public void setMetricsUpdateIntervalMs(long metricsUpdateIntervalMs) { 
            this.metricsUpdateIntervalMs = metricsUpdateIntervalMs; 
        }

        public boolean isEnableDetailedMetrics() { return enableDetailedMetrics; }
        public void setEnableDetailedMetrics(boolean enableDetailedMetrics) { 
            this.enableDetailedMetrics = enableDetailedMetrics; 
        }

        public int getThreadPoolMonitoringIntervalSeconds() { return threadPoolMonitoringIntervalSeconds; }
        public void setThreadPoolMonitoringIntervalSeconds(int threadPoolMonitoringIntervalSeconds) { 
            this.threadPoolMonitoringIntervalSeconds = threadPoolMonitoringIntervalSeconds; 
        }
    }
}