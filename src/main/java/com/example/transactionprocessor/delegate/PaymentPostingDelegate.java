package com.example.transactionprocessor.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("paymentPostingDelegate")
public class PaymentPostingDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String transactionType = (String) execution.getVariable("transactionType");
        Boolean validationResult = (Boolean) execution.getVariable("validationResult");
        Object paymentAmount = execution.getVariable("paymentAmount");
        String validationCode = (String) execution.getVariable("validationCode");
        String customerName = (String) execution.getVariable("customerName");
        String customerAccountNumber = (String) execution.getVariable("customerAccountNumber");
        String paymentReference = (String) execution.getVariable("paymentReference");
        String paymentMethodType = (String) execution.getVariable("paymentMethodType");
        String bankName = (String) execution.getVariable("bankName");
        Object currentBalance = execution.getVariable("currentBalance");
        Object minimumPaymentDue = execution.getVariable("minimumPaymentDue");
        
        System.out.println("=== Payment Posting ===");
        System.out.println("Transaction Type: " + transactionType);
        System.out.println("Payment Amount: " + paymentAmount);
        System.out.println("Validation Code: " + validationCode);
        System.out.println("Customer: " + customerName);
        System.out.println("Account Number: " + customerAccountNumber);
        System.out.println("Payment Reference: " + paymentReference);
        System.out.println("Payment Method: " + paymentMethodType);
        System.out.println("Bank: " + bankName);
        System.out.println("Current Balance: " + currentBalance);
        System.out.println("Minimum Payment Due: " + minimumPaymentDue);
        
        if (validationResult == null || !validationResult) {
            System.out.println("Posting FAILED: Payment validation failed");
            throw new RuntimeException("Cannot post invalid payment");
        }
        
        // Simulate posting logic
        String postingResult = performPosting(execution);
        
        execution.setVariable("postingStatus", postingResult);
        execution.setVariable("postingTimestamp", System.currentTimeMillis());
        execution.setVariable("postingId", generatePostingId());
        execution.setVariable("newBalance", calculateNewBalance(execution));
        
        System.out.println("Payment Posting completed. Status: " + postingResult);
        System.out.println("Posting ID: " + execution.getVariable("postingId"));
        System.out.println("New Account Balance: " + execution.getVariable("newBalance"));
        System.out.println("======================");
    }
    
    private String performPosting(DelegateExecution execution) {
        Object paymentAmount = execution.getVariable("paymentAmount");
        String customerName = (String) execution.getVariable("customerName");
        String customerAccountNumber = (String) execution.getVariable("customerAccountNumber");
        String paymentMethodType = (String) execution.getVariable("paymentMethodType");
        String bankName = (String) execution.getVariable("bankName");
        
        // Simulate posting logic - update customer account balance, process bank transfer
        System.out.println("Processing " + paymentMethodType + " payment of " + paymentAmount + 
                          " from " + customerName + " (Account: " + customerAccountNumber + ")");
        System.out.println("Initiating " + paymentMethodType + " transfer from " + bankName);
        System.out.println("Updating customer account balance and payment history");
        
        // Simulate ACH/Bank transfer processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return "POSTED";
    }
    
    private Object calculateNewBalance(DelegateExecution execution) {
        try {
            Object currentBalance = execution.getVariable("currentBalance");
            Object paymentAmount = execution.getVariable("paymentAmount");
            
            if (currentBalance != null && paymentAmount != null) {
                double balance = Double.parseDouble(currentBalance.toString());
                double payment = Double.parseDouble(paymentAmount.toString());
                double newBalance = balance - payment;
                
                System.out.println("Balance calculation: " + balance + " - " + payment + " = " + newBalance);
                return newBalance;
            }
        } catch (NumberFormatException e) {
            System.out.println("Error calculating new balance: " + e.getMessage());
        }
        
        return execution.getVariable("currentBalance");
    }
    
    private String generatePostingId() {
        return "POST" + System.currentTimeMillis() % 1000000;
    }
}