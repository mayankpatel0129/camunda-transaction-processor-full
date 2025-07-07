package com.example.transactionprocessor.delegate;

import com.example.transactionprocessor.exception.BusinessRuleException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Component("fraudCheckDelegate")
public class FraudCheckDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(FraudCheckDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Starting fraud check for transaction: {}", execution.getProcessInstanceId());
        
        // Get transaction details
        BigDecimal amount = (BigDecimal) execution.getVariable("amount");
        String cardNumber = (String) execution.getVariable("cardNumber");
        String location = (String) execution.getVariable("vendorLocation");
        
        // Simulate fraud check processing time
        Thread.sleep(150 + ThreadLocalRandom.current().nextInt(100));
        
        // Enhanced fraud detection logic
        boolean fraudDetected = performFraudAnalysis(amount, cardNumber, location);
        
        if (fraudDetected) {
            logger.warn("FRAUD DETECTED for transaction: {} - Amount: {}, Card: ****{}", 
                execution.getProcessInstanceId(), amount, 
                cardNumber != null ? cardNumber.substring(Math.max(0, cardNumber.length() - 4)) : "N/A");
            
            throw new BusinessRuleException("FRAUD_DETECTED", 
                "Transaction flagged by fraud detection system - unusual spending pattern detected");
        }
        
        // Calculate risk score
        int riskScore = calculateRiskScore(amount, location);
        execution.setVariable("riskScore", riskScore);
        execution.setVariable("fraudCheckStatus", "PASSED");
        execution.setVariable("fraudCheckTimestamp", System.currentTimeMillis());
        
        logger.info("Fraud check completed for transaction: {} - Risk Score: {}", 
            execution.getProcessInstanceId(), riskScore);
    }
    
    private boolean performFraudAnalysis(BigDecimal amount, String cardNumber, String location) {
        // Simulate sophisticated fraud detection
        
        // Check for suspicious amounts (exactly $15,000 triggers fraud for testing)
        if (amount != null && amount.compareTo(new BigDecimal("15000")) == 0) {
            return true;
        }
        
        // Simulate random fraud detection for very high amounts
        if (amount != null && amount.compareTo(new BigDecimal("50000")) >= 0) {
            return ThreadLocalRandom.current().nextDouble() < 0.3; // 30% chance
        }
        
        // Check for suspicious locations
        if (location != null && location.toLowerCase().contains("high risk")) {
            return ThreadLocalRandom.current().nextDouble() < 0.2; // 20% chance
        }
        
        return false;
    }
    
    private int calculateRiskScore(BigDecimal amount, String location) {
        int score = 0;
        
        // Amount-based risk
        if (amount != null) {
            if (amount.compareTo(new BigDecimal("10000")) >= 0) {
                score += 30;
            } else if (amount.compareTo(new BigDecimal("5000")) >= 0) {
                score += 20;
            } else if (amount.compareTo(new BigDecimal("1000")) >= 0) {
                score += 10;
            }
        }
        
        // Location-based risk
        if (location != null) {
            if (location.toLowerCase().contains("international")) {
                score += 15;
            }
            if (location.toLowerCase().contains("online")) {
                score += 5;
            }
        }
        
        // Add some randomness for simulation
        score += ThreadLocalRandom.current().nextInt(20);
        
        return Math.min(score, 100); // Cap at 100
    }
}