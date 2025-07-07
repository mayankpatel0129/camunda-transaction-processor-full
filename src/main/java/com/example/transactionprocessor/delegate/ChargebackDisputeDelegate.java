package com.example.transactionprocessor.delegate;

import com.example.transactionprocessor.exception.BusinessRuleException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component("chargebackDisputeDelegate")
public class ChargebackDisputeDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String transactionType = (String) execution.getVariable("transactionType");
        Object amount = execution.getVariable("amount");
        String investigationStatus = (String) execution.getVariable("investigationStatus");
        String disputeRecommendation = (String) execution.getVariable("disputeRecommendation");
        String investigationFindings = (String) execution.getVariable("investigationFindings");
        String chargebackCode = (String) execution.getVariable("chargebackCode");
        String cardHolderName = (String) execution.getVariable("cardHolderName");
        
        System.out.println("=== Chargeback Dispute Processing ===");
        System.out.println("Transaction Type: " + transactionType);
        System.out.println("Amount: " + amount);
        System.out.println("Investigation Status: " + investigationStatus);
        System.out.println("Dispute Recommendation: " + disputeRecommendation);
        System.out.println("Investigation Findings: " + investigationFindings);
        System.out.println("Chargeback Code: " + chargebackCode);
        System.out.println("Card Holder: " + cardHolderName);
        
        if (!"COMPLETED".equals(investigationStatus)) {
            System.out.println("Dispute Processing FAILED: Investigation not completed");
            throw new BusinessRuleException("INVESTIGATION_INCOMPLETE", "Cannot process dispute - investigation not completed");
        }
        
        // Process dispute based on recommendation
        String disputeResult = processDisputeDecision(execution);
        
        execution.setVariable("disputeStatus", disputeResult);
        execution.setVariable("disputeTimestamp", System.currentTimeMillis());
        execution.setVariable("disputeId", generateDisputeId());
        execution.setVariable("disputeDeadline", calculateDisputeDeadline());
        
        System.out.println("Chargeback Dispute completed. Status: " + disputeResult);
        System.out.println("Dispute ID: " + execution.getVariable("disputeId"));
        System.out.println("Dispute Deadline: " + execution.getVariable("disputeDeadline"));
        System.out.println("====================================");
    }
    
    private String processDisputeDecision(DelegateExecution execution) {
        String disputeRecommendation = (String) execution.getVariable("disputeRecommendation");
        String chargebackCode = (String) execution.getVariable("chargebackCode");
        Object amount = execution.getVariable("amount");
        String investigationFindings = (String) execution.getVariable("investigationFindings");
        
        try {
            if ("ACCEPT".equals(disputeRecommendation)) {
                System.out.println("Accepting chargeback based on investigation findings");
                return processChargebackAcceptance(execution);
            } else if ("DISPUTE".equals(disputeRecommendation)) {
                System.out.println("Disputing chargeback with card network");
                return processChargebackDispute(execution);
            } else if ("MANUAL_REVIEW".equals(disputeRecommendation)) {
                System.out.println("Escalating to manual review team");
                return "MANUAL_REVIEW_REQUIRED";
            } else {
                throw new BusinessRuleException("INVALID_DISPUTE_RECOMMENDATION", "Invalid dispute recommendation: " + disputeRecommendation);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Dispute processing interrupted", e);
        } catch (Exception e) {
            System.err.println("Dispute processing failed: " + e.getMessage());
            throw new BusinessRuleException("DISPUTE_PROCESSING_FAILED", "Dispute processing failed: " + e.getMessage());
        }
    }
    
    private String processChargebackAcceptance(DelegateExecution execution) throws InterruptedException {
        Object amount = execution.getVariable("amount");
        
        System.out.println("Processing chargeback acceptance...");
        
        // Simulate acceptance processing
        Thread.sleep(50);
        
        // Update accounting systems
        if (amount != null) {
            BigDecimal chargebackAmount = new BigDecimal(amount.toString());
            System.out.println("Adjusting merchant account for chargeback amount: " + chargebackAmount.abs());
        }
        
        // Generate acceptance documentation
        execution.setVariable("acceptanceReason", "Accepted based on investigation findings");
        execution.setVariable("accountingAdjustment", "COMPLETED");
        
        return "ACCEPTED";
    }
    
    private String processChargebackDispute(DelegateExecution execution) throws InterruptedException {
        String chargebackCode = (String) execution.getVariable("chargebackCode");
        String investigationFindings = (String) execution.getVariable("investigationFindings");
        Object amount = execution.getVariable("amount");
        
        System.out.println("Processing chargeback dispute submission...");
        
        // Simulate dispute processing time
        Thread.sleep(100 + (int)(Math.random() * 100));
        
        // Prepare dispute documentation
        String evidence = prepareDisputeEvidence(chargebackCode, investigationFindings);
        execution.setVariable("disputeEvidence", evidence);
        
        // Submit dispute to card network (simulation)
        boolean submissionSuccess = submitDisputeToNetwork(chargebackCode, evidence, amount);
        
        if (submissionSuccess) {
            execution.setVariable("networkSubmissionStatus", "SUBMITTED");
            execution.setVariable("disputeTrackingNumber", generateTrackingNumber());
            
            // Simulate 10% chance of immediate rejection
            if (Math.random() < 0.1) {
                execution.setVariable("networkResponse", "REJECTED");
                return "DISPUTE_REJECTED";
            } else {
                execution.setVariable("networkResponse", "UNDER_REVIEW");
                return "DISPUTE_SUBMITTED";
            }
        } else {
            execution.setVariable("networkSubmissionStatus", "FAILED");
            throw new RuntimeException("Failed to submit dispute to card network");
        }
    }
    
    private String prepareDisputeEvidence(String chargebackCode, String investigationFindings) {
        StringBuilder evidence = new StringBuilder();
        evidence.append("Dispute Evidence Package:\n");
        evidence.append("Chargeback Code: ").append(chargebackCode).append("\n");
        evidence.append("Investigation Findings: ").append(investigationFindings).append("\n");
        
        // Add specific evidence based on chargeback code
        switch (chargebackCode != null ? chargebackCode : "") {
            case "4863":
                evidence.append("- Transaction authorization records\n");
                evidence.append("- Delivery confirmation\n");
                evidence.append("- Customer communication logs\n");
                break;
            case "4855":
                evidence.append("- Proof of delivery/service completion\n");
                evidence.append("- Digital receipts and confirmations\n");
                evidence.append("- Service completion timestamps\n");
                break;
            case "4834":
                evidence.append("- Single transaction processing proof\n");
                evidence.append("- System audit logs\n");
                evidence.append("- Transaction reconciliation records\n");
                break;
            default:
                evidence.append("- Standard transaction documentation\n");
                evidence.append("- Authorization and processing records\n");
                break;
        }
        
        evidence.append("Evidence compiled on: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return evidence.toString();
    }
    
    private boolean submitDisputeToNetwork(String chargebackCode, String evidence, Object amount) throws InterruptedException {
        System.out.println("Submitting dispute to card network...");
        System.out.println("Chargeback Code: " + chargebackCode);
        System.out.println("Evidence Length: " + (evidence != null ? evidence.length() : 0) + " characters");
        
        // Simulate network submission time
        Thread.sleep(75);
        
        // Simulate 5% network failure rate
        if (Math.random() < 0.05) {
            System.err.println("Network submission failed - temporary network issue");
            return false;
        }
        
        System.out.println("Dispute successfully submitted to card network");
        return true;
    }
    
    private String generateDisputeId() {
        return "DIS" + System.currentTimeMillis() % 1000000;
    }
    
    private String generateTrackingNumber() {
        return "TRK" + System.currentTimeMillis() % 10000000;
    }
    
    private String calculateDisputeDeadline() {
        // Disputes typically have 30-45 days deadline
        LocalDateTime deadline = LocalDateTime.now().plusDays(30);
        return deadline.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}