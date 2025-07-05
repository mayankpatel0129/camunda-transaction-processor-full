package com.example.transactionprocessor.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("adjustmentReviewDelegate")
public class AdjustmentReviewDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String transactionType = (String) execution.getVariable("transactionType");
        Object amount = execution.getVariable("amount");
        String cardHolderName = (String) execution.getVariable("cardHolderName");
        String description = (String) execution.getVariable("description");
        String referenceNumber = (String) execution.getVariable("referenceNumber");
        
        System.out.println("=== Adjustment Review ===");
        System.out.println("Transaction Type: " + transactionType);
        System.out.println("Amount: " + amount);
        System.out.println("Card Holder: " + cardHolderName);
        System.out.println("Description: " + description);
        System.out.println("Reference: " + referenceNumber);
        
        // Simulate review logic
        String reviewResult = performReview(execution);
        
        execution.setVariable("reviewStatus", reviewResult);
        execution.setVariable("reviewTimestamp", System.currentTimeMillis());
        execution.setVariable("reviewId", generateReviewId());
        execution.setVariable("reviewedBy", "SYSTEM_REVIEWER");
        
        System.out.println("Adjustment Review completed. Status: " + reviewResult);
        System.out.println("Review ID: " + execution.getVariable("reviewId"));
        System.out.println("Reviewed By: " + execution.getVariable("reviewedBy"));
        System.out.println("========================");
    }
    
    private String performReview(DelegateExecution execution) {
        Object amount = execution.getVariable("amount");
        String description = (String) execution.getVariable("description");
        
        // Simulate review logic - check adjustment reason, amount thresholds
        if (amount != null && amount.toString().contains("5000")) {
            System.out.println("High-value adjustment detected, requiring manual review");
            return "PENDING_MANUAL_REVIEW";
        }
        
        if (description == null || description.trim().isEmpty()) {
            System.out.println("Missing adjustment description, requiring additional info");
            return "PENDING_INFO";
        }
        
        System.out.println("Adjustment review checks passed");
        return "APPROVED";
    }
    
    private String generateReviewId() {
        return "REV" + System.currentTimeMillis() % 1000000;
    }
}