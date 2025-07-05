package com.example.transactionprocessor.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("purchaseAuthorizationDelegate")
public class PurchaseAuthorizationDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String transactionType = (String) execution.getVariable("transactionType");
        Object amount = execution.getVariable("amount");
        String cardHolderName = (String) execution.getVariable("cardHolderName");
        String creditCardNumber = (String) execution.getVariable("creditCardNumber");
        String vendorName = (String) execution.getVariable("vendorName");
        String vendorLocation = (String) execution.getVariable("vendorLocation");
        
        System.out.println("=== Purchase Authorization ===");
        System.out.println("Transaction Type: " + transactionType);
        System.out.println("Amount: " + amount);
        System.out.println("Card Holder: " + cardHolderName);
        System.out.println("Card Number: " + creditCardNumber);
        System.out.println("Vendor: " + vendorName + " at " + vendorLocation);
        
        // Simulate authorization logic
        boolean authorized = performAuthorization(execution);
        
        execution.setVariable("authorized", authorized);
        execution.setVariable("authorizationTimestamp", System.currentTimeMillis());
        execution.setVariable("authorizationCode", generateAuthorizationCode());
        
        System.out.println("Purchase Authorization completed. Status: " + 
                          (authorized ? "APPROVED" : "DENIED"));
        System.out.println("Authorization Code: " + execution.getVariable("authorizationCode"));
        System.out.println("===============================");
    }
    
    private boolean performAuthorization(DelegateExecution execution) {
        Object amount = execution.getVariable("amount");
        String cardType = (String) execution.getVariable("cardType");
        
        // Simulate authorization logic based on amount and card type
        if (amount != null && amount.toString().contains("10000")) {
            return false; // Deny high amounts for demo
        }
        
        if ("DEBIT".equalsIgnoreCase(cardType)) {
            // Additional checks for debit cards
            System.out.println("Performing additional debit card verification");
        }
        
        return true; // Approve for demo
    }
    
    private String generateAuthorizationCode() {
        return "AUTH" + System.currentTimeMillis() % 1000000;
    }
}