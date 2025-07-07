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

@Component("supervisorApprovalDelegate")
public class SupervisorApprovalDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(SupervisorApprovalDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Processing supervisor approval for adjustment: {}", execution.getProcessInstanceId());
        
        // Get transaction details
        BigDecimal amount = (BigDecimal) execution.getVariable("amount");
        String customerName = (String) execution.getVariable("customerName");
        String description = (String) execution.getVariable("description");
        String reviewStatus = (String) execution.getVariable("reviewStatus");
        
        // Simulate supervisor review time
        Thread.sleep(100 + ThreadLocalRandom.current().nextInt(80));
        
        // Perform supervisor approval logic
        ApprovalResult result = processSupervisorApproval(amount, customerName, description, reviewStatus);
        
        // Set approval results
        execution.setVariable("supervisorApprovalStatus", result.status);
        execution.setVariable("supervisorApprovalReason", result.reason);
        execution.setVariable("supervisorApprovalTimestamp", System.currentTimeMillis());
        execution.setVariable("approvingSupervisor", result.approver);
        execution.setVariable("supervisorComments", result.comments);
        
        // If rejected, throw business rule exception
        if ("REJECTED".equals(result.status)) {
            logger.warn("Supervisor approval REJECTED for transaction: {} - Reason: {}", 
                execution.getProcessInstanceId(), result.reason);
            
            throw new BusinessRuleException("SUPERVISOR_APPROVAL_DENIED", 
                "Supervisor approval denied: " + result.reason);
        }
        
        logger.info("Supervisor approval completed for transaction: {} - Status: {}, Supervisor: {}", 
            execution.getProcessInstanceId(), result.status, result.approver);
    }
    
    private ApprovalResult processSupervisorApproval(BigDecimal amount, String customerName, 
                                                   String description, String reviewStatus) {
        ApprovalResult result = new ApprovalResult();
        
        // Assign supervisor based on amount range
        result.approver = assignSupervisor(amount);
        
        // Calculate approval probability
        double approvalProbability = calculateApprovalProbability(amount, description, reviewStatus);
        double randomValue = ThreadLocalRandom.current().nextDouble();
        
        if (randomValue <= approvalProbability) {
            result.status = "APPROVED";
            result.reason = "Adjustment approved by supervisor review";
            result.comments = generateApprovalComments(amount, description, true);
        } else {
            result.status = "REJECTED";
            result.reason = determineRejectionReason(amount, description);
            result.comments = generateApprovalComments(amount, description, false);
        }
        
        return result;
    }
    
    private String assignSupervisor(BigDecimal amount) {
        if (amount != null && amount.abs().compareTo(new BigDecimal("500")) >= 0) {
            return "Senior Supervisor Emma Johnson";
        } else {
            return "Team Supervisor David Chen";
        }
    }
    
    private double calculateApprovalProbability(BigDecimal amount, String description, String reviewStatus) {
        double probability = 0.85; // High approval rate for supervisor level (smaller amounts)
        
        // Amount-based adjustments
        if (amount != null) {
            BigDecimal absAmount = amount.abs();
            if (absAmount.compareTo(new BigDecimal("800")) >= 0) {
                probability -= 0.2;
            } else if (absAmount.compareTo(new BigDecimal("500")) >= 0) {
                probability -= 0.1;
            }
        }
        
        // Review status consideration
        if ("FLAGGED".equals(reviewStatus)) {
            probability -= 0.15;
        } else if ("APPROVED".equals(reviewStatus)) {
            probability += 0.05;
        }
        
        // Description-based adjustments
        if (description != null) {
            String lowerDesc = description.toLowerCase();
            if (lowerDesc.contains("billing error") || lowerDesc.contains("overcharge")) {
                probability += 0.1; // Clear errors are easily approved
            } else if (lowerDesc.contains("duplicate")) {
                probability += 0.15; // Duplicate charge reversals are straightforward
            } else if (lowerDesc.contains("customer service") || lowerDesc.contains("retention")) {
                probability += 0.05; // Customer service adjustments favored
            } else if (lowerDesc.contains("waive fee") || lowerDesc.contains("courtesy")) {
                probability -= 0.05; // Fee waivers require more consideration
            }
        }
        
        return Math.max(0.2, Math.min(0.95, probability));
    }
    
    private String determineRejectionReason(BigDecimal amount, String description) {
        if (amount != null && amount.abs().compareTo(new BigDecimal("1000")) >= 0) {
            return "Amount exceeds supervisor approval authority - requires manager approval";
        }
        
        if (description != null) {
            String lowerDesc = description.toLowerCase();
            if (lowerDesc.contains("courtesy") && amount != null && amount.abs().compareTo(new BigDecimal("200")) >= 0) {
                return "Courtesy adjustment amount exceeds guidelines";
            } else if (lowerDesc.contains("waive") && !lowerDesc.contains("error")) {
                return "Fee waiver requires additional justification";
            }
        }
        
        return "Adjustment does not meet standard approval criteria";
    }
    
    private String generateApprovalComments(BigDecimal amount, String description, boolean approved) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        StringBuilder comments = new StringBuilder();
        
        comments.append("Supervisor review completed on ").append(timestamp).append(". ");
        
        if (approved) {
            comments.append("Adjustment approved - within supervisor authority and meets business guidelines. ");
            if (amount != null && amount.abs().compareTo(new BigDecimal("500")) >= 0) {
                comments.append("Notable amount - logged for tracking purposes. ");
            }
        } else {
            comments.append("Adjustment declined at supervisor level - refer to manager for further review. ");
        }
        
        if (description != null && description.length() > 5) {
            comments.append("Context: ").append(description.substring(0, Math.min(40, description.length())));
            if (description.length() > 40) {
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