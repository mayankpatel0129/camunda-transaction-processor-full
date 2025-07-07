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

@Component("complianceDelegate")
public class ComplianceDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(ComplianceDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Processing compliance checks for transaction: {}", execution.getProcessInstanceId());
        
        // Get transaction details
        BigDecimal amount = (BigDecimal) execution.getVariable("amount");
        String transactionType = (String) execution.getVariable("transactionType");
        String customerName = (String) execution.getVariable("customerName");
        String riskLevel = (String) execution.getVariable("riskLevel");
        Integer riskScore = (Integer) execution.getVariable("comprehensiveRiskScore");
        
        // Simulate compliance processing time
        Thread.sleep(100 + ThreadLocalRandom.current().nextInt(200));
        
        // Perform compliance checks
        Map<String, Object> complianceResults = performComplianceChecks(amount, transactionType, customerName, riskLevel, riskScore);
        
        // Generate compliance report
        String complianceReport = generateComplianceReport(execution, complianceResults);
        
        // Set compliance results in process variables
        execution.setVariable("complianceStatus", complianceResults.get("status"));
        execution.setVariable("complianceScore", complianceResults.get("score"));
        execution.setVariable("complianceReport", complianceReport);
        execution.setVariable("amlCheckStatus", complianceResults.get("amlStatus"));
        execution.setVariable("kycCheckStatus", complianceResults.get("kycStatus"));
        execution.setVariable("sanctionsCheckStatus", complianceResults.get("sanctionsStatus"));
        execution.setVariable("complianceTimestamp", System.currentTimeMillis());
        
        // Check if transaction requires regulatory reporting
        boolean requiresReporting = (Boolean) complianceResults.get("requiresReporting");
        if (requiresReporting) {
            execution.setVariable("regulatoryReportingRequired", true);
            generateRegulatoryReport(execution, amount, transactionType);
        }
        
        logger.info("Compliance processing completed for transaction: {} - Status: {}, Score: {}, Reporting Required: {}", 
            execution.getProcessInstanceId(), complianceResults.get("status"), 
            complianceResults.get("score"), requiresReporting);
    }
    
    private Map<String, Object> performComplianceChecks(BigDecimal amount, String transactionType, 
                                                       String customerName, String riskLevel, Integer riskScore) {
        Map<String, Object> results = new HashMap<>();
        
        // AML (Anti-Money Laundering) Check
        String amlStatus = performAMLCheck(amount, customerName);
        results.put("amlStatus", amlStatus);
        
        // KYC (Know Your Customer) Check
        String kycStatus = performKYCCheck(customerName, amount);
        results.put("kycStatus", kycStatus);
        
        // Sanctions List Check
        String sanctionsStatus = performSanctionsCheck(customerName);
        results.put("sanctionsStatus", sanctionsStatus);
        
        // Calculate overall compliance score
        int complianceScore = calculateComplianceScore(amlStatus, kycStatus, sanctionsStatus, riskScore);
        results.put("score", complianceScore);
        
        // Determine overall compliance status
        String status = determineComplianceStatus(complianceScore, amlStatus, kycStatus, sanctionsStatus);
        results.put("status", status);
        
        // Check if regulatory reporting is required
        boolean requiresReporting = requiresRegulatoryReporting(amount, transactionType, status);
        results.put("requiresReporting", requiresReporting);
        
        return results;
    }
    
    private String performAMLCheck(BigDecimal amount, String customerName) {
        // Simulate AML processing
        try {
            Thread.sleep(30 + ThreadLocalRandom.current().nextInt(50));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Check for suspicious patterns
        if (amount != null && amount.compareTo(new BigDecimal("10000")) >= 0) {
            // High-value transactions require enhanced AML
            if (ThreadLocalRandom.current().nextDouble() < 0.05) { // 5% chance of AML flag
                return "FLAGGED";
            }
        }
        
        // Check for suspicious customer names (for testing)
        if (customerName != null && customerName.toLowerCase().contains("suspicious")) {
            return "FLAGGED";
        }
        
        return "PASSED";
    }
    
    private String performKYCCheck(String customerName, BigDecimal amount) {
        // Simulate KYC processing
        try {
            Thread.sleep(40 + ThreadLocalRandom.current().nextInt(60));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Enhanced KYC for high-value transactions
        if (amount != null && amount.compareTo(new BigDecimal("25000")) >= 0) {
            if (ThreadLocalRandom.current().nextDouble() < 0.1) { // 10% chance of requiring enhanced KYC
                return "ENHANCED_REQUIRED";
            }
        }
        
        // Basic KYC validation
        if (customerName == null || customerName.trim().isEmpty()) {
            return "INCOMPLETE";
        }
        
        return "VERIFIED";
    }
    
    private String performSanctionsCheck(String customerName) {
        // Simulate sanctions list check
        try {
            Thread.sleep(25 + ThreadLocalRandom.current().nextInt(40));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Check against sanctions list (simulated)
        if (customerName != null && customerName.toLowerCase().contains("blocked")) {
            return "MATCH_FOUND";
        }
        
        return "CLEAR";
    }
    
    private int calculateComplianceScore(String amlStatus, String kycStatus, String sanctionsStatus, Integer riskScore) {
        int score = 100; // Start with perfect score
        
        // Deduct points for compliance issues
        if ("FLAGGED".equals(amlStatus)) {
            score -= 40;
        }
        
        if ("INCOMPLETE".equals(kycStatus) || "ENHANCED_REQUIRED".equals(kycStatus)) {
            score -= 30;
        }
        
        if ("MATCH_FOUND".equals(sanctionsStatus)) {
            score -= 50; // Major deduction for sanctions match
        }
        
        // Factor in risk score
        if (riskScore != null && riskScore > 50) {
            score -= Math.min(25, (riskScore - 50) / 2);
        }
        
        return Math.max(0, score); // Ensure score doesn't go below 0
    }
    
    private String determineComplianceStatus(int score, String amlStatus, String kycStatus, String sanctionsStatus) {
        // Critical issues override score
        if ("MATCH_FOUND".equals(sanctionsStatus)) {
            return "BLOCKED";
        }
        
        if ("FLAGGED".equals(amlStatus)) {
            return "REQUIRES_REVIEW";
        }
        
        // Score-based determination
        if (score >= 80) {
            return "APPROVED";
        } else if (score >= 60) {
            return "CONDITIONAL_APPROVAL";
        } else if (score >= 40) {
            return "REQUIRES_REVIEW";
        } else {
            return "REJECTED";
        }
    }
    
    private boolean requiresRegulatoryReporting(BigDecimal amount, String transactionType, String status) {
        // High-value transactions require reporting
        if (amount != null && amount.compareTo(new BigDecimal("10000")) >= 0) {
            return true;
        }
        
        // Flagged transactions require reporting
        if ("REQUIRES_REVIEW".equals(status) || "REJECTED".equals(status) || "BLOCKED".equals(status)) {
            return true;
        }
        
        // Certain transaction types always require reporting
        return "Adjustment".equals(transactionType) && amount != null && amount.compareTo(new BigDecimal("5000")) <= 0;
    }
    
    private String generateComplianceReport(DelegateExecution execution, Map<String, Object> results) {
        StringBuilder report = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        report.append("=== COMPLIANCE REPORT ===\n");
        report.append("Transaction ID: ").append(execution.getProcessInstanceId()).append("\n");
        report.append("Timestamp: ").append(timestamp).append("\n");
        report.append("Overall Status: ").append(results.get("status")).append("\n");
        report.append("Compliance Score: ").append(results.get("score")).append("/100\n");
        report.append("\n=== DETAILED CHECKS ===\n");
        report.append("AML Status: ").append(results.get("amlStatus")).append("\n");
        report.append("KYC Status: ").append(results.get("kycStatus")).append("\n");
        report.append("Sanctions Status: ").append(results.get("sanctionsStatus")).append("\n");
        report.append("Regulatory Reporting Required: ").append(results.get("requiresReporting")).append("\n");
        report.append("========================\n");
        
        return report.toString();
    }
    
    private void generateRegulatoryReport(DelegateExecution execution, BigDecimal amount, String transactionType) {
        String reportId = "REG-" + System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        StringBuilder regulatoryReport = new StringBuilder();
        regulatoryReport.append("=== REGULATORY REPORT ===\n");
        regulatoryReport.append("Report ID: ").append(reportId).append("\n");
        regulatoryReport.append("Transaction ID: ").append(execution.getProcessInstanceId()).append("\n");
        regulatoryReport.append("Generated: ").append(timestamp).append("\n");
        regulatoryReport.append("Transaction Type: ").append(transactionType).append("\n");
        regulatoryReport.append("Amount: $").append(amount).append("\n");
        regulatoryReport.append("Regulatory Authority: FinCEN\n");
        regulatoryReport.append("Report Type: Suspicious Activity Report (SAR)\n");
        regulatoryReport.append("========================\n");
        
        execution.setVariable("regulatoryReportId", reportId);
        execution.setVariable("regulatoryReport", regulatoryReport.toString());
        
        logger.info("Regulatory report generated: {} for transaction: {}", reportId, execution.getProcessInstanceId());
    }
}