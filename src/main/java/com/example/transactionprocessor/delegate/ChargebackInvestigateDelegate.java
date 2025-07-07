package com.example.transactionprocessor.delegate;

import com.example.transactionprocessor.exception.BusinessRuleException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component("chargebackInvestigateDelegate")
public class ChargebackInvestigateDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String transactionType = (String) execution.getVariable("transactionType");
        Object amount = execution.getVariable("amount");
        String chargebackReason = (String) execution.getVariable("chargebackReason");
        String chargebackCode = (String) execution.getVariable("chargebackCode");
        Boolean liabilityShift = (Boolean) execution.getVariable("liabilityShift");
        String cardHolderName = (String) execution.getVariable("cardHolderName");
        String vendorName = (String) execution.getVariable("vendorName");
        
        System.out.println("=== Chargeback Investigation ===");
        System.out.println("Transaction Type: " + transactionType);
        System.out.println("Amount: " + amount);
        System.out.println("Chargeback Reason: " + chargebackReason);
        System.out.println("Chargeback Code: " + chargebackCode);
        System.out.println("Liability Shift: " + liabilityShift);
        System.out.println("Card Holder: " + cardHolderName);
        System.out.println("Vendor: " + vendorName);
        
        // Perform chargeback investigation
        Map<String, Object> investigationResult = performChargebackInvestigation(execution);
        
        execution.setVariable("investigationStatus", investigationResult.get("status"));
        execution.setVariable("investigationFindings", investigationResult.get("findings"));
        execution.setVariable("disputeRecommendation", investigationResult.get("disputeRecommendation"));
        execution.setVariable("investigationTimestamp", System.currentTimeMillis());
        execution.setVariable("investigationId", generateInvestigationId());
        
        System.out.println("Chargeback Investigation completed.");
        System.out.println("Status: " + investigationResult.get("status"));
        System.out.println("Findings: " + investigationResult.get("findings"));
        System.out.println("Dispute Recommendation: " + investigationResult.get("disputeRecommendation"));
        System.out.println("Investigation ID: " + execution.getVariable("investigationId"));
        System.out.println("==============================");
    }
    
    private Map<String, Object> performChargebackInvestigation(DelegateExecution execution) {
        Map<String, Object> result = new HashMap<>();
        
        String chargebackReason = (String) execution.getVariable("chargebackReason");
        String chargebackCode = (String) execution.getVariable("chargebackCode");
        Boolean liabilityShift = (Boolean) execution.getVariable("liabilityShift");
        Object amount = execution.getVariable("amount");
        
        try {
            // Simulate investigation processing time
            Thread.sleep(150 + (int)(Math.random() * 100));
            
            // Analyze chargeback based on reason code
            String findings = analyzeChargebackReason(chargebackCode, chargebackReason);
            boolean shouldDispute = determineDisputeRecommendation(chargebackCode, liabilityShift, amount);
            
            result.put("status", "COMPLETED");
            result.put("findings", findings);
            result.put("disputeRecommendation", shouldDispute ? "DISPUTE" : "ACCEPT");
            
            System.out.println("Investigation analysis completed for code: " + chargebackCode);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Chargeback investigation interrupted", e);
        } catch (Exception e) {
            System.err.println("Chargeback investigation failed: " + e.getMessage());
            result.put("status", "FAILED");
            result.put("findings", "Investigation failed: " + e.getMessage());
            result.put("disputeRecommendation", "MANUAL_REVIEW");
        }
        
        return result;
    }
    
    private String analyzeChargebackReason(String chargebackCode, String chargebackReason) {
        if (chargebackCode == null) {
            return "Unable to analyze - missing chargeback code";
        }
        
        switch (chargebackCode) {
            case "4863": // Cardholder does not recognize
                return "Customer claims no recognition of transaction. Requires proof of authorization and delivery.";
            case "4855": // Goods/Services not provided
                return "Customer claims goods/services not received. Requires proof of delivery or service completion.";
            case "4834": // Duplicate processing
                return "Customer claims transaction processed multiple times. Requires proof of single processing.";
            case "4808": // Authorization required
                return "Transaction processed without proper authorization. Requires valid authorization proof.";
            case "4812": // Account number not on file
                return "Card number not recognized by issuer. Requires valid account verification.";
            case "4837": // No cardholder authorization
                return "No authorization from cardholder. Requires signed receipt or PIN verification.";
            default:
                return "Standard chargeback analysis completed. Reason: " + (chargebackReason != null ? chargebackReason : "Unknown");
        }
    }
    
    private boolean determineDisputeRecommendation(String chargebackCode, Boolean liabilityShift, Object amount) {
        // If liability shift is true (3D Secure), we have strong grounds to dispute
        if (Boolean.TRUE.equals(liabilityShift)) {
            return true;
        }
        
        // For certain chargeback codes, we typically accept
        if ("4808".equals(chargebackCode) || "4812".equals(chargebackCode)) {
            return false; // Authorization issues are hard to dispute
        }
        
        // For high-value transactions, more likely to dispute
        if (amount != null) {
            try {
                BigDecimal transactionAmount = new BigDecimal(amount.toString());
                if (transactionAmount.abs().compareTo(new BigDecimal("500")) > 0) {
                    return true; // Dispute high-value transactions
                }
            } catch (NumberFormatException e) {
                // If we can't parse amount, default to dispute
                return true;
            }
        }
        
        // Default recommendation based on random factor (simulating evidence availability)
        return Math.random() > 0.3; // 70% chance to dispute
    }
    
    private String generateInvestigationId() {
        return "INV" + System.currentTimeMillis() % 1000000;
    }
}