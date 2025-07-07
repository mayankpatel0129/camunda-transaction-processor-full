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

@Component("managerApprovalDelegate")
public class ManagerApprovalDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ManagerApprovalDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Processing manager approval for adjustment: {}", execution.getProcessInstanceId());
        
        // Get transaction details
        BigDecimal amount = (BigDecimal) execution.getVariable("amount");
        String customerName = (String) execution.getVariable("customerName");
        String description = (String) execution.getVariable("description");
        String reviewStatus = (String) execution.getVariable("reviewStatus");
        
        // Simulate manager review time
        Thread.sleep(150 + ThreadLocalRandom.current().nextInt(100));
        
        // Perform manager approval logic
        ApprovalResult result = processManagerApproval(amount, customerName, description, reviewStatus);
        
        // Set approval results
        execution.setVariable("managerApprovalStatus", result.status);
        execution.setVariable("managerApprovalReason", result.reason);
        execution.setVariable("managerApprovalTimestamp", System.currentTimeMillis());
        execution.setVariable("approvingManager", result.approver);
        execution.setVariable("managerComments", result.comments);
        
        // If rejected, throw business rule exception
        if ("REJECTED".equals(result.status)) {
            logger.warn("Manager approval REJECTED for transaction: {} - Reason: {}", 
                execution.getProcessInstanceId(), result.reason);
            
            throw new BusinessRuleException("MANAGER_APPROVAL_DENIED", 
                "Manager approval denied: " + result.reason);
        }
        
        // If escalated, set escalation flag
        if ("ESCALATED".equals(result.status)) {
            execution.setVariable("requiresEscalation", true);
            execution.setVariable("escalationType", "SENIOR_MANAGEMENT");
        }
        
        logger.info("Manager approval completed for transaction: {} - Status: {}, Manager: {}", 
            execution.getProcessInstanceId(), result.status, result.approver);
    }
    
    private ApprovalResult processManagerApproval(BigDecimal amount, String customerName, 
                                                String description, String reviewStatus) {
        ApprovalResult result = new ApprovalResult();
        
        // Assign manager based on amount and customer
        result.approver = assignApprover(amount, customerName);
        
        // Calculate approval probability
        double approvalProbability = calculateApprovalProbability(amount, description, reviewStatus);
        double randomValue = ThreadLocalRandom.current().nextDouble();
        
        if (randomValue <= approvalProbability) {
            result.status = "APPROVED";
            result.reason = "Adjustment approved by manager review";
            result.comments = generateApprovalComments(amount, description, true);
        } else if (randomValue <= approvalProbability + 0.2) {
            result.status = "ESCALATED";
            result.reason = "Amount or complexity requires senior management approval";
            result.comments = generateApprovalComments(amount, description, false) + " Escalated for senior review.";
        } else {
            result.status = "REJECTED";
            result.reason = determineRejectionReason(amount, description);
            result.comments = generateApprovalComments(amount, description, false);
        }
        
        return result;
    }
    
    private String assignApprover(BigDecimal amount, String customerName) {
        if (amount != null && amount.abs().compareTo(new BigDecimal("4000")) >= 0) {
            return "Senior Manager Alice Wilson";
        } else if (customerName != null && customerName.toLowerCase().contains("enterprise")) {
            return "Enterprise Manager Bob Thompson";
        } else {
            return "Operations Manager Carol Davis";
        }
    }
    
    private double calculateApprovalProbability(BigDecimal amount, String description, String reviewStatus) {
        double probability = 0.75; // Base approval rate for manager level
        
        // Amount-based adjustments
        if (amount != null) {
            BigDecimal absAmount = amount.abs();
            if (absAmount.compareTo(new BigDecimal("4000")) >= 0) {
                probability -= 0.25;
            } else if (absAmount.compareTo(new BigDecimal("2000")) >= 0) {
                probability -= 0.15;
            }
        }
        
        // Review status consideration
        if ("FLAGGED".equals(reviewStatus)) {
            probability -= 0.2;
        } else if ("APPROVED".equals(reviewStatus)) {
            probability += 0.1;
        }
        
        // Description-based adjustments
        if (description != null) {
            String lowerDesc = description.toLowerCase();
            if (lowerDesc.contains("system error") || lowerDesc.contains("technical")) {
                probability += 0.2; // Technical errors more likely to be approved
            } else if (lowerDesc.contains("customer complaint")) {
                probability += 0.1; // Customer service adjustments favored
            } else if (lowerDesc.contains("promotional") || lowerDesc.contains("marketing")) {
                probability -= 0.1; // Marketing adjustments face more scrutiny
            }
        }
        
        return Math.max(0.15, Math.min(0.9, probability));
    }
    
    private String determineRejectionReason(BigDecimal amount, String description) {
        if (amount != null && amount.abs().compareTo(new BigDecimal("5000")) >= 0) {
            return "Amount exceeds manager approval authority";
        }
        
        if (description != null) {
            String lowerDesc = description.toLowerCase();
            if (lowerDesc.contains("promotional")) {
                return "Promotional adjustments require additional justification";
            } else if (lowerDesc.contains("goodwill")) {
                return "Goodwill gesture exceeds standard guidelines";
            }
        }
        
        return "Insufficient documentation or justification provided";
    }
    
    private String generateApprovalComments(BigDecimal amount, String description, boolean approved) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        StringBuilder comments = new StringBuilder();
        
        comments.append("Manager review completed on ").append(timestamp).append(". ");
        
        if (approved) {
            comments.append("Adjustment approved based on provided documentation and business justification. ");
            if (amount != null && amount.abs().compareTo(new BigDecimal("3000")) >= 0) {
                comments.append("High-value adjustment - enhanced tracking applied. ");
            }
        } else {
            comments.append("Adjustment requires additional review or has been declined at manager level. ");
        }
        
        if (description != null && description.length() > 10) {
            comments.append("Reason: ").append(description.substring(0, Math.min(50, description.length())));
            if (description.length() > 50) {
                comments.append("...");
            }
            comments.append(". ");
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