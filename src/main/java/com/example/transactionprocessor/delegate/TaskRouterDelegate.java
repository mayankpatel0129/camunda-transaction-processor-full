package com.example.transactionprocessor.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component("taskRouterDelegate")
public class TaskRouterDelegate implements JavaDelegate {

    @Autowired
    private ApplicationContext applicationContext;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Debug: Print all available variables
        System.out.println("=== Available Variables ===");
        execution.getVariables().forEach((key, value) -> 
            System.out.println(key + " = " + value));
        System.out.println("========================");
        
        // Get taskFlow from DMN result
        String taskFlow = (String) execution.getVariable("dmnResult");
        String transactionType = (String) execution.getVariable("transactionType");
        
        System.out.println("Executing task flow: " + taskFlow + " for transaction type: " + transactionType);
        
        if (taskFlow == null || taskFlow.isEmpty()) {
            throw new IllegalArgumentException("Task flow cannot be null or empty");
        }

        // Parse the task flow string to determine execution pattern
        if (taskFlow.contains("parallel:")) {
            executeParallelTasks(taskFlow, execution);
        } else {
            executeSequentialTasks(taskFlow, execution);
        }
    }

    private void executeSequentialTasks(String taskFlow, DelegateExecution execution) throws Exception {
        List<String> tasks = Arrays.asList(taskFlow.split(","));
        
        for (String task : tasks) {
            task = task.trim();
            executeTask(task, execution);
        }
    }

    private void executeParallelTasks(String taskFlow, DelegateExecution execution) throws Exception {
        List<String> tasks = Arrays.asList(taskFlow.split(","));
        CompletableFuture<?>[] futures = new CompletableFuture[tasks.size()];
        
        for (int i = 0; i < tasks.size(); i++) {
            String task = tasks.get(i).trim();
            
            if (task.startsWith("parallel:")) {
                String actualTask = task.substring("parallel:".length());
                futures[i] = CompletableFuture.runAsync(() -> {
                    try {
                        executeTask(actualTask, execution);
                    } catch (Exception e) {
                        throw new RuntimeException("Error executing parallel task: " + actualTask, e);
                    }
                }, executorService);
            } else {
                executeTask(task, execution);
            }
        }
        
        // Wait for all parallel tasks to complete
        for (CompletableFuture<?> future : futures) {
            if (future != null) {
                future.get();
            }
        }
    }

    private void executeTask(String taskName, DelegateExecution execution) throws Exception {
        try {
            JavaDelegate delegate = getDelegate(taskName);
            if (delegate != null) {
                delegate.execute(execution);
                System.out.println("Completed task: " + taskName);
            } else {
                System.out.println("No delegate found for task: " + taskName + " - simulating execution");
            }
        } catch (Exception e) {
            System.err.println("Error executing task: " + taskName + " - " + e.getMessage());
            throw e;
        }
    }

    private JavaDelegate getDelegate(String taskName) {
        try {
            String beanName = mapTaskNameToBean(taskName);
            return applicationContext.getBean(beanName, JavaDelegate.class);
        } catch (Exception e) {
            System.out.println("Delegate not found for task: " + taskName);
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
            default:
                return taskName + "Delegate";
        }
    }
}