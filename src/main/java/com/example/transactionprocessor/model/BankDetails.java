package com.example.transactionprocessor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BankDetails {
    
    @JsonProperty("bankName")
    private String bankName;
    
    @JsonProperty("routingNumber")
    private String routingNumber;
    
    @JsonProperty("accountNumber")
    private String accountNumber;
    
    @JsonProperty("accountHolderName")
    private String accountHolderName;
    
    @JsonProperty("accountType")
    private String accountType; // CHECKING, SAVINGS
    
    @JsonProperty("bankAddress")
    private Address bankAddress;
    
    public BankDetails() {}
    
    public BankDetails(String bankName, String routingNumber, String accountNumber, 
                      String accountHolderName, String accountType, Address bankAddress) {
        this.bankName = bankName;
        this.routingNumber = routingNumber;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
        this.accountType = accountType;
        this.bankAddress = bankAddress;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public String getRoutingNumber() {
        return routingNumber;
    }
    
    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getAccountHolderName() {
        return accountHolderName;
    }
    
    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    public Address getBankAddress() {
        return bankAddress;
    }
    
    public void setBankAddress(Address bankAddress) {
        this.bankAddress = bankAddress;
    }
    
    public String getMaskedAccountNumber() {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
    
    public String getMaskedRoutingNumber() {
        if (routingNumber == null || routingNumber.length() < 4) {
            return "****";
        }
        return "****" + routingNumber.substring(routingNumber.length() - 4);
    }
    
    @Override
    public String toString() {
        return "BankDetails{" +
                "bankName='" + bankName + '\'' +
                ", routingNumber='" + getMaskedRoutingNumber() + '\'' +
                ", accountNumber='" + getMaskedAccountNumber() + '\'' +
                ", accountHolderName='" + accountHolderName + '\'' +
                ", accountType='" + accountType + '\'' +
                ", bankAddress=" + bankAddress +
                '}';
    }
}