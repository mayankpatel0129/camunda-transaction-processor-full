package com.example.transactionprocessor.delegate;

import com.example.transactionprocessor.exception.BusinessRuleException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Component("riskAssessmentDelegate")
public class RiskAssessmentDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(RiskAssessmentDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Starting comprehensive risk assessment for transaction: {}", execution.getProcessInstanceId());
        
        // Get previous risk data
        Integer previousRiskScore = (Integer) execution.getVariable("riskScore");
        if (previousRiskScore == null) {
            previousRiskScore = 0;
        }
        
        // Get transaction details
        BigDecimal amount = (BigDecimal) execution.getVariable("amount");
        String customerName = (String) execution.getVariable("customerName");
        String merchantCategory = (String) execution.getVariable("merchantCategory");
        
        // Simulate risk assessment processing time
        Thread.sleep(200 + ThreadLocalRandom.current().nextInt(150));
        
        // Perform comprehensive risk analysis
        int comprehensiveRiskScore = performRiskAssessment(amount, customerName, merchantCategory, previousRiskScore);
        
        // Determine risk level
        String riskLevel = determineRiskLevel(comprehensiveRiskScore);
        
        // Check if transaction should be blocked due to extreme risk
        if (comprehensiveRiskScore >= 90) {
            logger.error("EXTREME RISK DETECTED for transaction: {} - Risk Score: {}", 
                execution.getProcessInstanceId(), comprehensiveRiskScore);
            
            throw new BusinessRuleException("EXTREME_RISK", 
                "Transaction blocked due to extreme risk assessment score: " + comprehensiveRiskScore);
        }
        
        // Set risk assessment results
        execution.setVariable("comprehensiveRiskScore", comprehensiveRiskScore);
        execution.setVariable("riskLevel", riskLevel);
        execution.setVariable("riskAssessmentStatus", "COMPLETED");
        execution.setVariable("riskAssessmentTimestamp", System.currentTimeMillis());
        
        // Set additional monitoring flags
        if (comprehensiveRiskScore >= 70) {
            execution.setVariable("requiresManualReview", true);
            execution.setVariable("enhancedMonitoring", true);
        } else if (comprehensiveRiskScore >= 50) {
            execution.setVariable("enhancedMonitoring", true);
        }
        
        logger.info("Risk assessment completed for transaction: {} - Comprehensive Risk Score: {}, Level: {}", 
            execution.getProcessInstanceId(), comprehensiveRiskScore, riskLevel);
    }
    
    private int performRiskAssessment(BigDecimal amount, String customerName, String merchantCategory, int baseRiskScore) {
        int totalRiskScore = baseRiskScore;
        
        // Advanced amount-based risk analysis
        if (amount != null) {
            if (amount.compareTo(new BigDecimal("25000")) >= 0) {
                totalRiskScore += 25;
            } else if (amount.compareTo(new BigDecimal("15000")) >= 0) {
                totalRiskScore += 20;
            } else if (amount.compareTo(new BigDecimal("10000")) >= 0) {
                totalRiskScore += 15;
            }
        }
        
        // Customer-based risk analysis
        if (customerName != null) {
            // Simulate customer history analysis
            if (customerName.toLowerCase().contains("test") || customerName.toLowerCase().contains("demo")) {
                totalRiskScore += 10; // Test accounts have higher risk
            }
        }
        
        // Merchant category risk analysis
        if (merchantCategory != null) {
            switch (merchantCategory.toLowerCase()) {
                case "luxury goods":
                case "jewelry":
                case "electronics":
                    totalRiskScore += 15;
                    break;
                case "gambling":
                case "cryptocurrency":
                    totalRiskScore += 25;
                    break;
                case "cash advance":
                    totalRiskScore += 20;
                    break;
                case "gas station":
                case "grocery":
                    totalRiskScore -= 5; // Lower risk
                    break;
            }
        }
        
        // Simulate market conditions and external factors
        totalRiskScore += ThreadLocalRandom.current().nextInt(15);
        
        return Math.min(totalRiskScore, 100); // Cap at 100
    }
    
    private String determineRiskLevel(int riskScore) {
        if (riskScore >= 80) {
            return "EXTREME";
        } else if (riskScore >= 60) {
            return "HIGH";
        } else if (riskScore >= 40) {
            return "MEDIUM";
        } else if (riskScore >= 20) {
            return "LOW";
        } else {
            return "MINIMAL";
        }
    }
}