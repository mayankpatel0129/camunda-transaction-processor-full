package com.example.transactionprocessor.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("paymentValidationDelegate")
public class PaymentValidationDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String transactionType = (String) execution.getVariable("transactionType");
        Object paymentAmount = execution.getVariable("paymentAmount");
        String customerAccountNumber = (String) execution.getVariable("customerAccountNumber");
        String customerName = (String) execution.getVariable("customerName");
        String accountStatus = (String) execution.getVariable("accountStatus");
        String paymentMethodType = (String) execution.getVariable("paymentMethodType");
        String bankName = (String) execution.getVariable("bankName");
        String bankAccountNumber = (String) execution.getVariable("bankAccountNumber");
        String routingNumber = (String) execution.getVariable("routingNumber");
        Object minimumPaymentDue = execution.getVariable("minimumPaymentDue");
        Object currentBalance = execution.getVariable("currentBalance");
        
        System.out.println("=== Payment Validation ===");
        System.out.println("Transaction Type: " + transactionType);
        System.out.println("Payment Amount: " + paymentAmount);
        System.out.println("Customer Account: " + customerAccountNumber);
        System.out.println("Customer Name: " + customerName);
        System.out.println("Account Status: " + accountStatus);
        System.out.println("Payment Method: " + paymentMethodType);
        System.out.println("Bank: " + bankName);
        System.out.println("Bank Account: " + bankAccountNumber);
        System.out.println("Routing Number: " + routingNumber);
        System.out.println("Current Balance: " + currentBalance);
        System.out.println("Minimum Payment Due: " + minimumPaymentDue);
        
        // Simulate validation logic
        boolean isValid = performValidation(execution);
        
        execution.setVariable("validationResult", isValid);
        execution.setVariable("validationTimestamp", System.currentTimeMillis());
        execution.setVariable("validationCode", generateValidationCode());
        
        if (!isValid) {
            System.out.println("Payment validation FAILED");
            throw new RuntimeException("Payment validation failed");
        }
        
        System.out.println("Payment Validation completed. Status: " + 
                          (isValid ? "VALID" : "INVALID"));
        System.out.println("Validation Code: " + execution.getVariable("validationCode"));
        System.out.println("==========================");
    }
    
    private boolean performValidation(DelegateExecution execution) {
        String customerName = (String) execution.getVariable("customerName");
        String accountStatus = (String) execution.getVariable("accountStatus");
        String bankName = (String) execution.getVariable("bankName");
        String routingNumber = (String) execution.getVariable("routingNumber");
        Object paymentAmount = execution.getVariable("paymentAmount");
        Object minimumPaymentDue = execution.getVariable("minimumPaymentDue");
        
        // Validate customer account status
        if (!"ACTIVE".equalsIgnoreCase(accountStatus)) {
            System.out.println("Validation failed: Account is not active. Status: " + accountStatus);
            return false;
        }
        
        // Validate customer name
        if (customerName == null || customerName.trim().isEmpty()) {
            System.out.println("Validation failed: Invalid customer name");
            return false;
        }
        
        // Validate bank details
        if (bankName == null || bankName.trim().isEmpty()) {
            System.out.println("Validation failed: Invalid bank name");
            return false;
        }
        
        if (routingNumber == null || routingNumber.trim().isEmpty() || routingNumber.length() != 9) {
            System.out.println("Validation failed: Invalid routing number");
            return false;
        }
        
        // Validate payment amount against minimum payment
        if (paymentAmount != null && minimumPaymentDue != null) {
            try {
                double paymentAmt = Double.parseDouble(paymentAmount.toString());
                double minPayment = Double.parseDouble(minimumPaymentDue.toString());
                
                if (paymentAmt <= 0) {
                    System.out.println("Validation failed: Payment amount must be positive");
                    return false;
                }
                
                if (paymentAmt < minPayment) {
                    System.out.println("Validation warning: Payment amount is less than minimum payment due");
                    // Allow but log warning
                }
            } catch (NumberFormatException e) {
                System.out.println("Validation failed: Invalid payment amount format");
                return false;
            }
        }
        
        System.out.println("Payment validation checks passed");
        return true;
    }
    
    private String generateValidationCode() {
        return "VAL" + System.currentTimeMillis() % 1000000;
    }
}