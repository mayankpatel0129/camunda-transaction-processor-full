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

@Component("notificationDelegate")
public class NotificationDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Processing notifications for transaction: {}", execution.getProcessInstanceId());
        
        // Get transaction details
        String transactionType = (String) execution.getVariable("transactionType");
        BigDecimal amount = (BigDecimal) execution.getVariable("amount");
        String customerName = (String) execution.getVariable("customerName");
        String customerEmail = (String) execution.getVariable("customerEmail");
        String riskLevel = (String) execution.getVariable("riskLevel");
        
        // Simulate notification processing time
        Thread.sleep(50 + ThreadLocalRandom.current().nextInt(100));
        
        // Determine notification types needed
        boolean sendCustomerNotification = shouldSendCustomerNotification(transactionType, amount);
        boolean sendMerchantNotification = shouldSendMerchantNotification(transactionType, amount);
        boolean sendComplianceAlert = shouldSendComplianceAlert(riskLevel, amount);
        boolean sendManagementAlert = shouldSendManagementAlert(amount);
        
        // Process customer notification
        if (sendCustomerNotification) {
            sendCustomerNotification(execution, transactionType, amount, customerName, customerEmail);
        }
        
        // Process merchant notification
        if (sendMerchantNotification) {
            sendMerchantNotification(execution, transactionType, amount);
        }
        
        // Process compliance alert
        if (sendComplianceAlert) {
            sendComplianceAlert(execution, riskLevel, amount);
        }
        
        // Process management alert
        if (sendManagementAlert) {
            sendManagementAlert(execution, amount);
        }
        
        // Set notification results
        execution.setVariable("customerNotificationSent", sendCustomerNotification);
        execution.setVariable("merchantNotificationSent", sendMerchantNotification);
        execution.setVariable("complianceAlertSent", sendComplianceAlert);
        execution.setVariable("managementAlertSent", sendManagementAlert);
        execution.setVariable("notificationTimestamp", System.currentTimeMillis());
        execution.setVariable("notificationStatus", "COMPLETED");
        
        logger.info("Notification processing completed for transaction: {} - Customer: {}, Merchant: {}, Compliance: {}, Management: {}", 
            execution.getProcessInstanceId(), sendCustomerNotification, sendMerchantNotification, 
            sendComplianceAlert, sendManagementAlert);
    }
    
    private boolean shouldSendCustomerNotification(String transactionType, BigDecimal amount) {
        // Always send for high-value transactions
        if (amount != null && amount.compareTo(new BigDecimal("1000")) >= 0) {
            return true;
        }
        
        // Send for certain transaction types
        return "Purchase".equals(transactionType) || "Adjustment".equals(transactionType);
    }
    
    private boolean shouldSendMerchantNotification(String transactionType, BigDecimal amount) {
        // Send for high-value purchases
        if ("Purchase".equals(transactionType) && amount != null && amount.compareTo(new BigDecimal("5000")) >= 0) {
            return true;
        }
        
        // Send for all adjustments
        return "Adjustment".equals(transactionType);
    }
    
    private boolean shouldSendComplianceAlert(String riskLevel, BigDecimal amount) {
        // Send for high/extreme risk transactions
        if (riskLevel != null && ("HIGH".equals(riskLevel) || "EXTREME".equals(riskLevel))) {
            return true;
        }
        
        // Send for very high amounts regardless of risk
        return amount != null && amount.compareTo(new BigDecimal("25000")) >= 0;
    }
    
    private boolean shouldSendManagementAlert(BigDecimal amount) {
        // Send for ultra-high value transactions
        return amount != null && amount.compareTo(new BigDecimal("50000")) >= 0;
    }
    
    private void sendCustomerNotification(DelegateExecution execution, String transactionType, 
                                        BigDecimal amount, String customerName, String customerEmail) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        String message = String.format(
            "Dear %s, your %s transaction for $%.2f has been processed on %s. Transaction ID: %s",
            customerName != null ? customerName : "Valued Customer",
            transactionType != null ? transactionType.toLowerCase() : "transaction",
            amount != null ? amount.doubleValue() : 0.0,
            timestamp,
            execution.getProcessInstanceId()
        );
        
        logger.info("CUSTOMER NOTIFICATION: {} (Email: {})", message, customerEmail);
        
        // Simulate email/SMS sending time
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void sendMerchantNotification(DelegateExecution execution, String transactionType, BigDecimal amount) {
        String merchantId = (String) execution.getVariable("merchantId");
        String merchantName = (String) execution.getVariable("merchantName");
        
        String message = String.format(
            "Merchant %s (%s): %s transaction for $%.2f completed. Transaction ID: %s",
            merchantName != null ? merchantName : "Unknown",
            merchantId != null ? merchantId : "N/A",
            transactionType != null ? transactionType : "Unknown",
            amount != null ? amount.doubleValue() : 0.0,
            execution.getProcessInstanceId()
        );
        
        logger.info("MERCHANT NOTIFICATION: {}", message);
        
        try {
            Thread.sleep(25);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void sendComplianceAlert(DelegateExecution execution, String riskLevel, BigDecimal amount) {
        String message = String.format(
            "COMPLIANCE ALERT: %s risk transaction detected. Amount: $%.2f, Transaction ID: %s",
            riskLevel != null ? riskLevel : "Unknown",
            amount != null ? amount.doubleValue() : 0.0,
            execution.getProcessInstanceId()
        );
        
        logger.warn("COMPLIANCE ALERT: {}", message);
        
        try {
            Thread.sleep(40);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void sendManagementAlert(DelegateExecution execution, BigDecimal amount) {
        String message = String.format(
            "MANAGEMENT ALERT: Ultra-high value transaction detected. Amount: $%.2f, Transaction ID: %s - Requires immediate attention",
            amount != null ? amount.doubleValue() : 0.0,
            execution.getProcessInstanceId()
        );
        
        logger.error("MANAGEMENT ALERT: {}", message);
        
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}