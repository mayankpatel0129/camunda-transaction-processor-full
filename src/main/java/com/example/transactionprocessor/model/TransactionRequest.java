package com.example.transactionprocessor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionRequest {
    
    @JsonProperty("transactionType")
    private String transactionType;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("transactionDateTime")
    private LocalDateTime transactionDateTime;
    
    @JsonProperty("creditCardInfo")
    private CreditCardInfo creditCardInfo;
    
    @JsonProperty("billingAddress")
    private Address billingAddress;
    
    @JsonProperty("vendorInfo")
    private VendorInfo vendorInfo;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("referenceNumber")
    private String referenceNumber;
    
    // Refund-specific fields
    @JsonProperty("originalTransactionId")
    private String originalTransactionId;
    
    @JsonProperty("refundReason")
    private String refundReason;
    
    // Chargeback-specific fields
    @JsonProperty("chargebackReason")
    private String chargebackReason;
    
    @JsonProperty("chargebackCode")
    private String chargebackCode;
    
    @JsonProperty("liabilityShift")
    private Boolean liabilityShift;
    
    public TransactionRequest() {}
    
    public TransactionRequest(String transactionType, BigDecimal amount, String currency, 
                            LocalDateTime transactionDateTime, CreditCardInfo creditCardInfo,
                            Address billingAddress, VendorInfo vendorInfo, String description,
                            String referenceNumber) {
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.transactionDateTime = transactionDateTime;
        this.creditCardInfo = creditCardInfo;
        this.billingAddress = billingAddress;
        this.vendorInfo = vendorInfo;
        this.description = description;
        this.referenceNumber = referenceNumber;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public LocalDateTime getTransactionDateTime() {
        return transactionDateTime;
    }
    
    public void setTransactionDateTime(LocalDateTime transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
    }
    
    public CreditCardInfo getCreditCardInfo() {
        return creditCardInfo;
    }
    
    public void setCreditCardInfo(CreditCardInfo creditCardInfo) {
        this.creditCardInfo = creditCardInfo;
    }
    
    public Address getBillingAddress() {
        return billingAddress;
    }
    
    public void setBillingAddress(Address billingAddress) {
        this.billingAddress = billingAddress;
    }
    
    public VendorInfo getVendorInfo() {
        return vendorInfo;
    }
    
    public void setVendorInfo(VendorInfo vendorInfo) {
        this.vendorInfo = vendorInfo;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getReferenceNumber() {
        return referenceNumber;
    }
    
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
    
    // Refund-specific getters and setters
    public String getOriginalTransactionId() {
        return originalTransactionId;
    }
    
    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }
    
    public String getRefundReason() {
        return refundReason;
    }
    
    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }
    
    // Chargeback-specific getters and setters
    public String getChargebackReason() {
        return chargebackReason;
    }
    
    public void setChargebackReason(String chargebackReason) {
        this.chargebackReason = chargebackReason;
    }
    
    public String getChargebackCode() {
        return chargebackCode;
    }
    
    public void setChargebackCode(String chargebackCode) {
        this.chargebackCode = chargebackCode;
    }
    
    public Boolean getLiabilityShift() {
        return liabilityShift;
    }
    
    public void setLiabilityShift(Boolean liabilityShift) {
        this.liabilityShift = liabilityShift;
    }
    
    @Override
    public String toString() {
        return "TransactionRequest{" +
                "transactionType='" + transactionType + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", transactionDateTime=" + transactionDateTime +
                ", creditCardInfo=" + creditCardInfo +
                ", billingAddress=" + billingAddress +
                ", vendorInfo=" + vendorInfo +
                ", description='" + description + '\'' +
                ", referenceNumber='" + referenceNumber + '\'' +
                '}';
    }
}