package com.example.transactionprocessor.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component("auditTrailDelegate")
public class AuditTrailDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(AuditTrailDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Creating comprehensive audit trail for transaction: {}", execution.getProcessInstanceId());
        
        // Get transaction details
        String transactionType = (String) execution.getVariable("transactionType");
        BigDecimal amount = (BigDecimal) execution.getVariable("amount");
        String customerName = (String) execution.getVariable("customerName");
        String description = (String) execution.getVariable("description");
        
        // Simulate audit trail processing time
        Thread.sleep(75 + ThreadLocalRandom.current().nextInt(50));
        
        // Create comprehensive audit trail
        Map<String, Object> auditData = createAuditTrail(execution, transactionType, amount, customerName, description);
        
        // Generate audit report
        String auditReport = generateAuditReport(auditData);
        
        // Set audit results
        execution.setVariable("auditTrailId", auditData.get("auditId"));
        execution.setVariable("auditReport", auditReport);
        execution.setVariable("auditTimestamp", System.currentTimeMillis());
        execution.setVariable("auditStatus", "COMPLETED");
        execution.setVariable("auditLevel", auditData.get("auditLevel"));
        execution.setVariable("retentionPeriod", auditData.get("retentionPeriod"));
        
        // Log critical information for external audit systems
        logAuditEntry(auditData);
        
        logger.info("Audit trail created for transaction: {} - Audit ID: {}, Level: {}", 
            execution.getProcessInstanceId(), auditData.get("auditId"), auditData.get("auditLevel"));
    }
    
    private Map<String, Object> createAuditTrail(DelegateExecution execution, String transactionType, 
                                               BigDecimal amount, String customerName, String description) {
        Map<String, Object> auditData = new HashMap<>();
        
        // Generate unique audit ID
        String auditId = "AUD-" + System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(1000);
        auditData.put("auditId", auditId);
        
        // Basic transaction information
        auditData.put("transactionId", execution.getProcessInstanceId());
        auditData.put("transactionType", transactionType);
        auditData.put("amount", amount);
        auditData.put("customerName", customerName);
        auditData.put("description", description);
        auditData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // Determine audit level based on transaction characteristics
        String auditLevel = determineAuditLevel(amount, transactionType);
        auditData.put("auditLevel", auditLevel);
        
        // Set retention period based on audit level
        int retentionYears = determineRetentionPeriod(auditLevel, amount);
        auditData.put("retentionPeriod", retentionYears + " years");
        
        // Capture process execution details
        auditData.put("processDefinitionId", execution.getProcessDefinitionId());
        auditData.put("processInstanceId", execution.getProcessInstanceId());
        auditData.put("businessKey", execution.getBusinessKey());
        
        // Capture approval chain information
        captureApprovalChain(execution, auditData);
        
        // Capture risk and compliance information
        captureRiskCompliance(execution, auditData);
        
        // Add system information
        auditData.put("systemVersion", "TransactionProcessor-v3.0.0");
        auditData.put("auditCreatedBy", "AuditTrailDelegate");
        auditData.put("environmentId", "PROD-001");
        
        return auditData;
    }
    
    private String determineAuditLevel(BigDecimal amount, String transactionType) {
        if (amount != null && amount.abs().compareTo(new BigDecimal("25000")) >= 0) {
            return "CRITICAL";
        } else if (amount != null && amount.abs().compareTo(new BigDecimal("10000")) >= 0) {
            return "HIGH";
        } else if (amount != null && amount.abs().compareTo(new BigDecimal("5000")) >= 0) {
            return "MEDIUM";
        } else if ("Adjustment".equals(transactionType)) {
            return "MEDIUM"; // All adjustments get medium audit level minimum
        } else {
            return "STANDARD";
        }
    }
    
    private int determineRetentionPeriod(String auditLevel, BigDecimal amount) {
        switch (auditLevel) {
            case "CRITICAL":
                return 10; // 10 years for critical transactions
            case "HIGH":
                return 7;  // 7 years for high-value transactions
            case "MEDIUM":
                return 5;  // 5 years for medium transactions
            default:
                return 3;  // 3 years for standard transactions
        }
    }
    
    private void captureApprovalChain(DelegateExecution execution, Map<String, Object> auditData) {
        StringBuilder approvalChain = new StringBuilder();
        
        // Check for various approval stages
        String supervisorApproval = (String) execution.getVariable("supervisorApprovalStatus");
        if (supervisorApproval != null) {
            String supervisor = (String) execution.getVariable("approvingSupervisor");
            approvalChain.append("Supervisor: ").append(supervisor).append(" (").append(supervisorApproval).append(") ");
        }
        
        String managerApproval = (String) execution.getVariable("managerApprovalStatus");
        if (managerApproval != null) {
            String manager = (String) execution.getVariable("approvingManager");
            approvalChain.append("Manager: ").append(manager).append(" (").append(managerApproval).append(") ");
        }
        
        String executiveApproval = (String) execution.getVariable("executiveApprovalStatus");
        if (executiveApproval != null) {
            String executive = (String) execution.getVariable("executiveApprovalOfficer");
            approvalChain.append("Executive: ").append(executive).append(" (").append(executiveApproval).append(") ");
        }
        
        auditData.put("approvalChain", approvalChain.toString().trim());
    }
    
    private void captureRiskCompliance(DelegateExecution execution, Map<String, Object> auditData) {
        // Risk information
        Integer riskScore = (Integer) execution.getVariable("comprehensiveRiskScore");
        String riskLevel = (String) execution.getVariable("riskLevel");
        if (riskScore != null) {
            auditData.put("riskScore", riskScore);
        }
        if (riskLevel != null) {
            auditData.put("riskLevel", riskLevel);
        }
        
        // Compliance information
        String complianceStatus = (String) execution.getVariable("complianceStatus");
        Integer complianceScore = (Integer) execution.getVariable("complianceScore");
        if (complianceStatus != null) {
            auditData.put("complianceStatus", complianceStatus);
        }
        if (complianceScore != null) {
            auditData.put("complianceScore", complianceScore);
        }
        
        // Fraud check information
        String fraudCheckStatus = (String) execution.getVariable("fraudCheckStatus");
        if (fraudCheckStatus != null) {
            auditData.put("fraudCheckStatus", fraudCheckStatus);
        }
    }
    
    private String generateAuditReport(Map<String, Object> auditData) {
        StringBuilder report = new StringBuilder();
        
        report.append("======================== AUDIT TRAIL REPORT ========================\n");
        report.append("Audit ID: ").append(auditData.get("auditId")).append("\n");
        report.append("Transaction ID: ").append(auditData.get("transactionId")).append("\n");
        report.append("Created: ").append(auditData.get("timestamp")).append("\n");
        report.append("Audit Level: ").append(auditData.get("auditLevel")).append("\n");
        report.append("Retention Period: ").append(auditData.get("retentionPeriod")).append("\n");
        report.append("\n--- TRANSACTION DETAILS ---\n");
        report.append("Type: ").append(auditData.get("transactionType")).append("\n");
        report.append("Amount: $").append(auditData.get("amount")).append("\n");
        report.append("Customer: ").append(auditData.get("customerName")).append("\n");
        report.append("Description: ").append(auditData.get("description")).append("\n");
        
        if (auditData.get("approvalChain") != null && !auditData.get("approvalChain").toString().isEmpty()) {
            report.append("\n--- APPROVAL CHAIN ---\n");
            report.append(auditData.get("approvalChain")).append("\n");
        }
        
        if (auditData.get("riskLevel") != null) {
            report.append("\n--- RISK & COMPLIANCE ---\n");
            report.append("Risk Level: ").append(auditData.get("riskLevel"));
            if (auditData.get("riskScore") != null) {
                report.append(" (Score: ").append(auditData.get("riskScore")).append(")");
            }
            report.append("\n");
            
            if (auditData.get("complianceStatus") != null) {
                report.append("Compliance Status: ").append(auditData.get("complianceStatus"));
                if (auditData.get("complianceScore") != null) {
                    report.append(" (Score: ").append(auditData.get("complianceScore")).append(")");
                }
                report.append("\n");
            }
            
            if (auditData.get("fraudCheckStatus") != null) {
                report.append("Fraud Check: ").append(auditData.get("fraudCheckStatus")).append("\n");
            }
        }
        
        report.append("\n--- SYSTEM INFORMATION ---\n");
        report.append("System Version: ").append(auditData.get("systemVersion")).append("\n");
        report.append("Environment: ").append(auditData.get("environmentId")).append("\n");
        report.append("Process Instance: ").append(auditData.get("processInstanceId")).append("\n");
        report.append("====================================================================\n");
        
        return report.toString();
    }
    
    private void logAuditEntry(Map<String, Object> auditData) {
        // This would typically integrate with external audit systems
        // For now, we'll log at appropriate levels based on audit importance
        
        String auditLevel = (String) auditData.get("auditLevel");
        String auditId = (String) auditData.get("auditId");
        String transactionId = (String) auditData.get("transactionId");
        BigDecimal amount = (BigDecimal) auditData.get("amount");
        
        if ("CRITICAL".equals(auditLevel)) {
            logger.error("CRITICAL AUDIT ENTRY - ID: {}, Transaction: {}, Amount: ${}", 
                auditId, transactionId, amount);
        } else if ("HIGH".equals(auditLevel)) {
            logger.warn("HIGH-LEVEL AUDIT ENTRY - ID: {}, Transaction: {}, Amount: ${}", 
                auditId, transactionId, amount);
        } else {
            logger.info("AUDIT ENTRY - ID: {}, Transaction: {}, Level: {}", 
                auditId, transactionId, auditLevel);
        }
    }
}