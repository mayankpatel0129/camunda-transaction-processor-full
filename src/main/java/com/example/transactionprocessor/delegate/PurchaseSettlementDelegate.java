package com.example.transactionprocessor.delegate;

import com.example.transactionprocessor.exception.BusinessRuleException;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("purchaseSettlementDelegate")
public class PurchaseSettlementDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String transactionType = (String) execution.getVariable("transactionType");
        Boolean authorized = (Boolean) execution.getVariable("authorized");
        Object amount = execution.getVariable("amount");
        String authorizationCode = (String) execution.getVariable("authorizationCode");
        String vendorName = (String) execution.getVariable("vendorName");
        String referenceNumber = (String) execution.getVariable("referenceNumber");
        
        System.out.println("=== Purchase Settlement ===");
        System.out.println("Transaction Type: " + transactionType);
        System.out.println("Amount: " + amount);
        System.out.println("Authorization Code: " + authorizationCode);
        System.out.println("Vendor: " + vendorName);
        System.out.println("Reference: " + referenceNumber);
        
        if (authorized == null || !authorized) {
            System.out.println("Settlement FAILED: Transaction not authorized");
            throw new BusinessRuleException("UNAUTHORIZED_SETTLEMENT", "Cannot settle unauthorized transaction");
        }
        
        // Simulate settlement logic
        String settlementResult = performSettlement(execution);
        
        execution.setVariable("settlementStatus", settlementResult);
        execution.setVariable("settlementTimestamp", System.currentTimeMillis());
        execution.setVariable("settlementId", generateSettlementId());
        
        System.out.println("Purchase Settlement completed. Status: " + settlementResult);
        System.out.println("Settlement ID: " + execution.getVariable("settlementId"));
        System.out.println("===========================");
    }
    
    private String performSettlement(DelegateExecution execution) {
        Object amount = execution.getVariable("amount");
        String vendorName = (String) execution.getVariable("vendorName");
        
        // Simulate settlement logic - check if vendor is active, process funds transfer
        System.out.println("Processing funds transfer of " + amount + " to " + vendorName);
        
        // Simulate some settlement processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return "SETTLED";
    }
    
    private String generateSettlementId() {
        return "SETTLE" + System.currentTimeMillis() % 1000000;
    }
}