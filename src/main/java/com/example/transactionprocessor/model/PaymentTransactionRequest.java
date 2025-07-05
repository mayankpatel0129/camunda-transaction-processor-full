package com.example.transactionprocessor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PaymentTransactionRequest {
    
    @JsonProperty("transactionType")
    private String transactionType = "Payment";
    
    @JsonProperty("paymentAmount")
    private BigDecimal paymentAmount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("paymentDate")
    private LocalDate paymentDate;
    
    @JsonProperty("scheduledDateTime")
    private LocalDateTime scheduledDateTime;
    
    @JsonProperty("customerAccount")
    private CustomerAccount customerAccount;
    
    @JsonProperty("paymentMethod")
    private PaymentMethod paymentMethod;
    
    @JsonProperty("paymentType")
    private String paymentType; // MINIMUM_PAYMENT, FULL_BALANCE, CUSTOM_AMOUNT
    
    @JsonProperty("isRecurring")
    private boolean isRecurring;
    
    @JsonProperty("recurringFrequency")
    private String recurringFrequency; // MONTHLY, WEEKLY, QUARTERLY
    
    @JsonProperty("paymentReference")
    private String paymentReference;
    
    @JsonProperty("memo")
    private String memo;
    
    @JsonProperty("confirmationEmail")
    private boolean confirmationEmail;
    
    public PaymentTransactionRequest() {}
    
    public PaymentTransactionRequest(String transactionType, BigDecimal paymentAmount, String currency,
                                   LocalDate paymentDate, LocalDateTime scheduledDateTime,
                                   CustomerAccount customerAccount, PaymentMethod paymentMethod,
                                   String paymentType, boolean isRecurring, String recurringFrequency,
                                   String paymentReference, String memo, boolean confirmationEmail) {
        this.transactionType = transactionType;
        this.paymentAmount = paymentAmount;
        this.currency = currency;
        this.paymentDate = paymentDate;
        this.scheduledDateTime = scheduledDateTime;
        this.customerAccount = customerAccount;
        this.paymentMethod = paymentMethod;
        this.paymentType = paymentType;
        this.isRecurring = isRecurring;
        this.recurringFrequency = recurringFrequency;
        this.paymentReference = paymentReference;
        this.memo = memo;
        this.confirmationEmail = confirmationEmail;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }
    
    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public LocalDate getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public LocalDateTime getScheduledDateTime() {
        return scheduledDateTime;
    }
    
    public void setScheduledDateTime(LocalDateTime scheduledDateTime) {
        this.scheduledDateTime = scheduledDateTime;
    }
    
    public CustomerAccount getCustomerAccount() {
        return customerAccount;
    }
    
    public void setCustomerAccount(CustomerAccount customerAccount) {
        this.customerAccount = customerAccount;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    
    public boolean isRecurring() {
        return isRecurring;
    }
    
    public void setRecurring(boolean isRecurring) {
        this.isRecurring = isRecurring;
    }
    
    public String getRecurringFrequency() {
        return recurringFrequency;
    }
    
    public void setRecurringFrequency(String recurringFrequency) {
        this.recurringFrequency = recurringFrequency;
    }
    
    public String getPaymentReference() {
        return paymentReference;
    }
    
    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }
    
    public String getMemo() {
        return memo;
    }
    
    public void setMemo(String memo) {
        this.memo = memo;
    }
    
    public boolean isConfirmationEmail() {
        return confirmationEmail;
    }
    
    public void setConfirmationEmail(boolean confirmationEmail) {
        this.confirmationEmail = confirmationEmail;
    }
    
    @Override
    public String toString() {
        return "PaymentTransactionRequest{" +
                "transactionType='" + transactionType + '\'' +
                ", paymentAmount=" + paymentAmount +
                ", currency='" + currency + '\'' +
                ", paymentDate=" + paymentDate +
                ", scheduledDateTime=" + scheduledDateTime +
                ", customerAccount=" + customerAccount +
                ", paymentMethod=" + paymentMethod +
                ", paymentType='" + paymentType + '\'' +
                ", isRecurring=" + isRecurring +
                ", recurringFrequency='" + recurringFrequency + '\'' +
                ", paymentReference='" + paymentReference + '\'' +
                ", memo='" + memo + '\'' +
                ", confirmationEmail=" + confirmationEmail +
                '}';
    }
}