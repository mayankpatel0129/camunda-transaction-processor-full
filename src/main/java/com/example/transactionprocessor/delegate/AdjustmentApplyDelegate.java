package com.example.transactionprocessor.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("adjustmentApplyDelegate")
public class AdjustmentApplyDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String transactionType = (String) execution.getVariable("transactionType");
        String reviewStatus = (String) execution.getVariable("reviewStatus");
        Object amount = execution.getVariable("amount");
        String reviewId = (String) execution.getVariable("reviewId");
        String cardHolderName = (String) execution.getVariable("cardHolderName");
        String description = (String) execution.getVariable("description");
        
        System.out.println("=== Adjustment Apply ===");
        System.out.println("Transaction Type: " + transactionType);
        System.out.println("Amount: " + amount);
        System.out.println("Review Status: " + reviewStatus);
        System.out.println("Review ID: " + reviewId);
        System.out.println("Card Holder: " + cardHolderName);
        System.out.println("Description: " + description);
        
        if (!"APPROVED".equals(reviewStatus)) {
            System.out.println("Apply FAILED: Review not approved. Status: " + reviewStatus);
            throw new RuntimeException("Cannot apply adjustment - review not approved");
        }
        
        // Simulate apply logic
        String applyResult = performApply(execution);
        
        execution.setVariable("applyStatus", applyResult);
        execution.setVariable("applyTimestamp", System.currentTimeMillis());
        execution.setVariable("adjustmentId", generateAdjustmentId());
        
        System.out.println("Adjustment Apply completed. Status: " + applyResult);
        System.out.println("Adjustment ID: " + execution.getVariable("adjustmentId"));
        System.out.println("========================");
    }
    
    private String performApply(DelegateExecution execution) {
        Object amount = execution.getVariable("amount");
        String cardHolderName = (String) execution.getVariable("cardHolderName");
        String description = (String) execution.getVariable("description");
        
        // Simulate apply logic - update account balance, create adjustment record
        System.out.println("Applying adjustment of " + amount + " for " + cardHolderName);
        System.out.println("Adjustment reason: " + description);
        
        // Simulate some processing time
        try {
            Thread.sleep(75);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return "APPLIED";
    }
    
    private String generateAdjustmentId() {
        return "ADJ" + System.currentTimeMillis() % 1000000;
    }
}