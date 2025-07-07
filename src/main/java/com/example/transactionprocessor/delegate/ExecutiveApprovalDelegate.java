package com.example.transactionprocessor.delegate;

import com.example.transactionprocessor.exception.BusinessRuleException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component("executiveApprovalDelegate")
public class ExecutiveApprovalDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ExecutiveApprovalDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Processing executive approval for high-value adjustment: {}", execution.getProcessInstanceId());
        
        // Get transaction details
        BigDecimal amount = (BigDecimal) execution.getVariable("amount");
        String transactionType = (String) execution.getVariable("transactionType");
        String customerName = (String) execution.getVariable("customerName");
        String description = (String) execution.getVariable("description");
        
        // Simulate executive review time (longer than other approvals)
        Thread.sleep(300 + ThreadLocalRandom.current().nextInt(200));
        
        // Perform executive approval logic
        ApprovalResult result = processExecutiveApproval(amount, transactionType, customerName, description);
        
        // Set approval results
        execution.setVariable("executiveApprovalStatus", result.status);
        execution.setVariable("executiveApprovalReason", result.reason);
        execution.setVariable("executiveApprovalTimestamp", System.currentTimeMillis());
        execution.setVariable("executiveApprovalOfficer", result.approver);
        execution.setVariable("executiveApprovalComments", result.comments);
        
        // If rejected, throw business rule exception
        if ("REJECTED".equals(result.status)) {
            logger.warn("Executive approval REJECTED for transaction: {} - Reason: {}", 
                execution.getProcessInstanceId(), result.reason);
            
            throw new BusinessRuleException("EXECUTIVE_APPROVAL_DENIED", 
                "Executive approval denied: " + result.reason);
        }
        
        // If requires additional review, set flag
        if ("CONDITIONAL".equals(result.status)) {
            execution.setVariable("requiresAdditionalReview", true);
            execution.setVariable("additionalReviewType", "BOARD_APPROVAL");
        }
        
        logger.info("Executive approval completed for transaction: {} - Status: {}, Approver: {}", 
            execution.getProcessInstanceId(), result.status, result.approver);
    }
    
    private ApprovalResult processExecutiveApproval(BigDecimal amount, String transactionType, 
                                                  String customerName, String description) {
        ApprovalResult result = new ApprovalResult();
        
        // Determine executive approver based on amount
        if (amount != null && amount.abs().compareTo(new BigDecimal("50000")) >= 0) {
            result.approver = "CEO John Smith";
        } else if (amount != null && amount.abs().compareTo(new BigDecimal("25000")) >= 0) {
            result.approver = "CFO Sarah Johnson";
        } else {
            result.approver = "VP Finance Michael Brown";
        }
        
        // Simulate executive decision-making process
        double approvalProbability = calculateApprovalProbability(amount, description, customerName);
        double randomValue = ThreadLocalRandom.current().nextDouble();
        
        if (randomValue <= approvalProbability) {
            result.status = "APPROVED";
            result.reason = "Transaction approved after executive review";
            result.comments = generateApprovalComments(amount, customerName, true);
        } else if (randomValue <= approvalProbability + 0.15) {
            result.status = "CONDITIONAL";
            result.reason = "Conditional approval pending additional review";
            result.comments = generateApprovalComments(amount, customerName, false) + " Additional board review required.";
        } else {
            result.status = "REJECTED";
            result.reason = determineRejectionReason(amount, description);
            result.comments = generateApprovalComments(amount, customerName, false);
        }
        
        return result;
    }
    
    private double calculateApprovalProbability(BigDecimal amount, String description, String customerName) {
        double probability = 0.7; // Base approval rate
        
        // Amount-based adjustments
        if (amount != null) {
            BigDecimal absAmount = amount.abs();
            if (absAmount.compareTo(new BigDecimal("100000")) >= 0) {
                probability -= 0.4; // Very high amounts are more likely to be rejected
            } else if (absAmount.compareTo(new BigDecimal("50000")) >= 0) {
                probability -= 0.2;
            } else if (absAmount.compareTo(new BigDecimal("25000")) >= 0) {
                probability -= 0.1;
            }
        }
        
        // Description-based adjustments
        if (description != null) {
            String lowerDesc = description.toLowerCase();
            if (lowerDesc.contains("fraud") || lowerDesc.contains("dispute")) {
                probability += 0.2; // Legitimate fraud/dispute adjustments more likely to be approved
            } else if (lowerDesc.contains("goodwill") || lowerDesc.contains("courtesy")) {
                probability -= 0.1; // Goodwill adjustments face more scrutiny
            } else if (lowerDesc.contains("error") || lowerDesc.contains("mistake")) {
                probability += 0.15; // Error corrections more likely to be approved
            }
        }
        
        // Customer-based adjustments
        if (customerName != null) {
            if (customerName.toLowerCase().contains("vip") || customerName.toLowerCase().contains("premium")) {
                probability += 0.1; // VIP customers get more favorable treatment
            }
        }
        
        return Math.max(0.1, Math.min(0.95, probability)); // Keep between 10% and 95%
    }
    
    private String determineRejectionReason(BigDecimal amount, String description) {
        if (amount != null && amount.abs().compareTo(new BigDecimal("100000")) >= 0) {
            return "Amount exceeds executive approval limits - requires board approval";
        }
        
        if (description != null && description.toLowerCase().contains("goodwill")) {
            return "Goodwill adjustment does not meet criteria for approval";
        }
        
        return "Insufficient justification for adjustment amount";
    }
    
    private String generateApprovalComments(BigDecimal amount, String customerName, boolean approved) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        StringBuilder comments = new StringBuilder();
        
        comments.append("Executive Review completed on ").append(timestamp).append(". ");
        
        if (approved) {
            comments.append("After careful consideration, this adjustment is approved. ");
            if (amount != null && amount.abs().compareTo(new BigDecimal("25000")) >= 0) {
                comments.append("Given the significant amount, enhanced monitoring will be applied. ");
            }
        } else {
            comments.append("This adjustment requires additional review or has been declined. ");
            comments.append("Recommendation: Seek alternative resolution or additional documentation. ");
        }
        
        if (customerName != null) {
            comments.append("Customer: ").append(customerName).append(". ");
        }
        
        return comments.toString();
    }
    
    private static class ApprovalResult {
        String status;
        String reason;
        String approver;
        String comments;
    }
}