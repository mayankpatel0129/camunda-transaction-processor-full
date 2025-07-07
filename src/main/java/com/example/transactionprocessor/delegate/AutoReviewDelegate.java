package com.example.transactionprocessor.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component("autoReviewDelegate")
public class AutoReviewDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(AutoReviewDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Processing automated review for small adjustment: {}", execution.getProcessInstanceId());
        
        // Get transaction details
        BigDecimal amount = (BigDecimal) execution.getVariable("amount");
        String customerName = (String) execution.getVariable("customerName");
        String description = (String) execution.getVariable("description");
        
        // Simulate automated processing time (very fast)
        Thread.sleep(20 + ThreadLocalRandom.current().nextInt(30));
        
        // Perform automated review
        AutoReviewResult result = performAutomatedReview(amount, customerName, description);
        
        // Set review results
        execution.setVariable("autoReviewStatus", result.status);
        execution.setVariable("autoReviewReason", result.reason);
        execution.setVariable("autoReviewTimestamp", System.currentTimeMillis());
        execution.setVariable("autoReviewScore", result.score);
        execution.setVariable("autoReviewComments", result.comments);
        execution.setVariable("reviewedBy", "Automated System v2.1");
        
        // Set processing priority
        if (result.score >= 80) {
            execution.setVariable("processingPriority", "HIGH");
        } else if (result.score >= 60) {
            execution.setVariable("processingPriority", "NORMAL");
        } else {
            execution.setVariable("processingPriority", "LOW");
        }
        
        logger.info("Automated review completed for transaction: {} - Status: {}, Score: {}", 
            execution.getProcessInstanceId(), result.status, result.score);
    }
    
    private AutoReviewResult performAutomatedReview(BigDecimal amount, String customerName, String description) {
        AutoReviewResult result = new AutoReviewResult();
        
        // Calculate automated review score
        result.score = calculateReviewScore(amount, customerName, description);
        
        // Determine status based on score
        if (result.score >= 70) {
            result.status = "AUTO_APPROVED";
            result.reason = "Meets all automated approval criteria";
        } else if (result.score >= 40) {
            result.status = "APPROVED_WITH_CONDITIONS";
            result.reason = "Approved with automated monitoring";
        } else {
            result.status = "APPROVED";
            result.reason = "Standard automated approval for small adjustment";
        }
        
        // Generate automated comments
        result.comments = generateAutomatedComments(amount, description, result.score);
        
        return result;
    }
    
    private int calculateReviewScore(BigDecimal amount, String customerName, String description) {
        int score = 50; // Base score for small adjustments
        
        // Amount-based scoring (small amounts are safer)
        if (amount != null) {
            BigDecimal absAmount = amount.abs();
            if (absAmount.compareTo(new BigDecimal("10")) <= 0) {
                score += 30; // Very small amounts get high scores
            } else if (absAmount.compareTo(new BigDecimal("50")) <= 0) {
                score += 20;
            } else if (absAmount.compareTo(new BigDecimal("100")) <= 0) {
                score += 10;
            }
        }
        
        // Customer-based scoring
        if (customerName != null) {
            if (customerName.toLowerCase().contains("premium") || customerName.toLowerCase().contains("vip")) {
                score += 15; // Premium customers get higher scores
            } else if (customerName.toLowerCase().contains("new")) {
                score += 5; // New customers get slight boost
            }
        }
        
        // Description-based scoring
        if (description != null) {
            String lowerDesc = description.toLowerCase();
            if (lowerDesc.contains("system error") || lowerDesc.contains("technical")) {
                score += 25; // Technical errors are clear-cut
            } else if (lowerDesc.contains("billing error") || lowerDesc.contains("duplicate")) {
                score += 20; // Billing errors are straightforward
            } else if (lowerDesc.contains("overcharge") || lowerDesc.contains("incorrect")) {
                score += 15; // Clear overcharges are legitimate
            } else if (lowerDesc.contains("fee reversal") || lowerDesc.contains("waive")) {
                score += 10; // Fee reversals are common
            } else if (lowerDesc.contains("goodwill") || lowerDesc.contains("courtesy")) {
                score -= 5; // Goodwill adjustments get lower scores
            }
        }
        
        // Add slight randomness for realistic variation
        score += ThreadLocalRandom.current().nextInt(10) - 5;
        
        return Math.max(0, Math.min(100, score));
    }
    
    private String generateAutomatedComments(BigDecimal amount, String description, int score) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        StringBuilder comments = new StringBuilder();
        
        comments.append("Automated review completed on ").append(timestamp).append(". ");
        comments.append("Review Score: ").append(score).append("/100. ");
        
        if (score >= 80) {
            comments.append("High confidence approval - all automated criteria met. ");
        } else if (score >= 60) {
            comments.append("Standard approval with good confidence level. ");
        } else {
            comments.append("Approved with basic confidence - routine small adjustment. ");
        }
        
        if (amount != null) {
            comments.append("Amount: $").append(amount.abs()).append(" (within auto-approval limits). ");
        }
        
        if (description != null && description.length() > 5) {
            comments.append("Type: ").append(description.substring(0, Math.min(30, description.length())));
            if (description.length() > 30) {
                comments.append("...");
            }
            comments.append(". ");
        }
        
        comments.append("No manual intervention required.");
        
        return comments.toString();
    }
    
    private static class AutoReviewResult {
        String status;
        String reason;
        int score;
        String comments;
    }
}