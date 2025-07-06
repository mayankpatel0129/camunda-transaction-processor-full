package com.example.transactionprocessor.delegate;

import com.example.transactionprocessor.exception.BusinessRuleException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("refundValidationDelegate")
public class RefundValidationDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String transactionType = (String) execution.getVariable("transactionType");
        Object amount = execution.getVariable("amount");
        String originalTransactionId = (String) execution.getVariable("originalTransactionId");
        String refundReason = (String) execution.getVariable("refundReason");
        String cardHolderName = (String) execution.getVariable("cardHolderName");
        
        System.out.println("=== Refund Validation ===");
        System.out.println("Transaction Type: " + transactionType);
        System.out.println("Amount: " + amount);
        System.out.println("Original Transaction ID: " + originalTransactionId);
        System.out.println("Refund Reason: " + refundReason);
        System.out.println("Card Holder: " + cardHolderName);
        
        // Validate refund request
        String validationResult = performRefundValidation(execution);
        
        if (!"VALID".equals(validationResult)) {
            System.out.println("Refund Validation FAILED: " + validationResult);
            throw new BusinessRuleException("REFUND_VALIDATION_FAILED", "Refund validation failed: " + validationResult);
        }
        
        execution.setVariable("refundValidationStatus", validationResult);
        execution.setVariable("refundValidationTimestamp", System.currentTimeMillis());
        execution.setVariable("refundValidationId", generateRefundValidationId());
        
        System.out.println("Refund Validation completed. Status: " + validationResult);
        System.out.println("Validation ID: " + execution.getVariable("refundValidationId"));
        System.out.println("========================");
    }
    
    private String performRefundValidation(DelegateExecution execution) {
        Object amount = execution.getVariable("amount");
        String originalTransactionId = (String) execution.getVariable("originalTransactionId");
        String refundReason = (String) execution.getVariable("refundReason");
        
        // Validate original transaction ID
        if (originalTransactionId == null || originalTransactionId.trim().isEmpty()) {
            return "Missing original transaction ID";
        }
        
        // Validate refund reason
        if (refundReason == null || refundReason.trim().isEmpty()) {
            return "Missing refund reason";
        }
        
        // Validate amount (should be negative for refunds)
        if (amount != null) {
            try {
                BigDecimal refundAmount = new BigDecimal(amount.toString());
                if (refundAmount.compareTo(BigDecimal.ZERO) >= 0) {
                    return "Refund amount must be negative";
                }
                
                // Check refund limit (absolute value shouldn't exceed $5000)
                if (refundAmount.abs().compareTo(new BigDecimal("5000")) > 0) {
                    return "Refund amount exceeds limit";
                }
            } catch (NumberFormatException e) {
                return "Invalid refund amount format";
            }
        }
        
        // Simulate original transaction lookup (5% failure rate)
        if (Math.random() < 0.05) {
            return "Original transaction not found";
        }
        
        // Simulate processing time
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return "VALID";
    }
    
    private String generateRefundValidationId() {
        return "REFVAL" + System.currentTimeMillis() % 1000000;
    }
}