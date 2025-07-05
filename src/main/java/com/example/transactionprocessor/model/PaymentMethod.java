package com.example.transactionprocessor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentMethod {
    
    @JsonProperty("paymentType")
    private String paymentType; // BANK_TRANSFER, ACH, WIRE, CHECK, ONLINE_BANKING
    
    @JsonProperty("bankDetails")
    private BankDetails bankDetails;
    
    @JsonProperty("isDefault")
    private boolean isDefault;
    
    @JsonProperty("paymentMethodId")
    private String paymentMethodId;
    
    @JsonProperty("nickname")
    private String nickname; // User-friendly name like "Main Checking"
    
    public PaymentMethod() {}
    
    public PaymentMethod(String paymentType, BankDetails bankDetails, boolean isDefault, 
                        String paymentMethodId, String nickname) {
        this.paymentType = paymentType;
        this.bankDetails = bankDetails;
        this.isDefault = isDefault;
        this.paymentMethodId = paymentMethodId;
        this.nickname = nickname;
    }
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    
    public BankDetails getBankDetails() {
        return bankDetails;
    }
    
    public void setBankDetails(BankDetails bankDetails) {
        this.bankDetails = bankDetails;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public String getPaymentMethodId() {
        return paymentMethodId;
    }
    
    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    @Override
    public String toString() {
        return "PaymentMethod{" +
                "paymentType='" + paymentType + '\'' +
                ", bankDetails=" + bankDetails +
                ", isDefault=" + isDefault +
                ", paymentMethodId='" + paymentMethodId + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}