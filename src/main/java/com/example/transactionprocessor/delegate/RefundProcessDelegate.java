package com.example.transactionprocessor.delegate;

import com.example.transactionprocessor.exception.BusinessRuleException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("refundProcessDelegate")
public class RefundProcessDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String transactionType = (String) execution.getVariable("transactionType");
        Object amount = execution.getVariable("amount");
        String originalTransactionId = (String) execution.getVariable("originalTransactionId");
        String refundReason = (String) execution.getVariable("refundReason");
        String refundValidationStatus = (String) execution.getVariable("refundValidationStatus");
        String cardHolderName = (String) execution.getVariable("cardHolderName");
        
        System.out.println("=== Refund Processing ===");
        System.out.println("Transaction Type: " + transactionType);
        System.out.println("Amount: " + amount);
        System.out.println("Original Transaction ID: " + originalTransactionId);
        System.out.println("Refund Reason: " + refundReason);
        System.out.println("Validation Status: " + refundValidationStatus);
        System.out.println("Card Holder: " + cardHolderName);
        
        if (!"VALID".equals(refundValidationStatus)) {
            System.out.println("Refund Processing FAILED: Validation not approved");
            throw new BusinessRuleException("REFUND_NOT_VALIDATED", "Cannot process refund - validation not approved");
        }
        
        // Process the refund
        String processingResult = performRefundProcessing(execution);
        
        execution.setVariable("refundProcessingStatus", processingResult);
        execution.setVariable("refundProcessingTimestamp", System.currentTimeMillis());
        execution.setVariable("refundId", generateRefundId());
        
        System.out.println("Refund Processing completed. Status: " + processingResult);
        System.out.println("Refund ID: " + execution.getVariable("refundId"));
        System.out.println("========================");
    }
    
    private String performRefundProcessing(DelegateExecution execution) {
        Object amount = execution.getVariable("amount");
        String originalTransactionId = (String) execution.getVariable("originalTransactionId");
        String refundReason = (String) execution.getVariable("refundReason");
        String cardHolderName = (String) execution.getVariable("cardHolderName");
        
        try {
            System.out.println("Processing refund of " + amount + " for " + cardHolderName);
            System.out.println("Original Transaction: " + originalTransactionId);
            System.out.println("Refund Reason: " + refundReason);
            
            // Simulate refund processing with payment network
            Thread.sleep(100 + (int)(Math.random() * 100));
            
            // Simulate 2% failure rate for testing
            if (Math.random() < 0.02) {
                throw new RuntimeException("Payment network refund processing failed");
            }
            
            // Additional validation for large refunds
            if (amount != null) {
                BigDecimal refundAmount = new BigDecimal(amount.toString());
                if (refundAmount.abs().compareTo(new BigDecimal("1000")) > 0) {
                    System.out.println("Large refund - additional verification completed");
                    Thread.sleep(50); // Additional processing time for large refunds
                }
            }
            
            return "PROCESSED";
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Refund processing interrupted", e);
        } catch (Exception e) {
            System.err.println("Refund processing failed: " + e.getMessage());
            throw new BusinessRuleException("REFUND_PROCESSING_FAILED", "Refund processing failed: " + e.getMessage());
        }
    }
    
    private String generateRefundId() {
        return "REF" + System.currentTimeMillis() % 1000000;
    }
}