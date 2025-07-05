package com.example.transactionprocessor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CustomerAccount {
    
    @JsonProperty("accountNumber")
    private String accountNumber;
    
    @JsonProperty("customerName")
    private String customerName;
    
    @JsonProperty("customerEmail")
    private String customerEmail;
    
    @JsonProperty("customerPhone")
    private String customerPhone;
    
    @JsonProperty("accountType")
    private String accountType; // CREDIT, CHECKING, SAVINGS
    
    @JsonProperty("currentBalance")
    private BigDecimal currentBalance;
    
    @JsonProperty("creditLimit")
    private BigDecimal creditLimit;
    
    @JsonProperty("minimumPaymentDue")
    private BigDecimal minimumPaymentDue;
    
    @JsonProperty("paymentDueDate")
    private LocalDate paymentDueDate;
    
    @JsonProperty("lastPaymentDate")
    private LocalDate lastPaymentDate;
    
    @JsonProperty("accountStatus")
    private String accountStatus; // ACTIVE, SUSPENDED, CLOSED
    
    public CustomerAccount() {}
    
    public CustomerAccount(String accountNumber, String customerName, String customerEmail, 
                          String customerPhone, String accountType, BigDecimal currentBalance,
                          BigDecimal creditLimit, BigDecimal minimumPaymentDue, 
                          LocalDate paymentDueDate, LocalDate lastPaymentDate, String accountStatus) {
        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.accountType = accountType;
        this.currentBalance = currentBalance;
        this.creditLimit = creditLimit;
        this.minimumPaymentDue = minimumPaymentDue;
        this.paymentDueDate = paymentDueDate;
        this.lastPaymentDate = lastPaymentDate;
        this.accountStatus = accountStatus;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }
    
    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }
    
    public BigDecimal getCreditLimit() {
        return creditLimit;
    }
    
    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }
    
    public BigDecimal getMinimumPaymentDue() {
        return minimumPaymentDue;
    }
    
    public void setMinimumPaymentDue(BigDecimal minimumPaymentDue) {
        this.minimumPaymentDue = minimumPaymentDue;
    }
    
    public LocalDate getPaymentDueDate() {
        return paymentDueDate;
    }
    
    public void setPaymentDueDate(LocalDate paymentDueDate) {
        this.paymentDueDate = paymentDueDate;
    }
    
    public LocalDate getLastPaymentDate() {
        return lastPaymentDate;
    }
    
    public void setLastPaymentDate(LocalDate lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
    }
    
    public String getAccountStatus() {
        return accountStatus;
    }
    
    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
    
    public String getMaskedAccountNumber() {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "****-****-****-" + accountNumber.substring(accountNumber.length() - 4);
    }
    
    @Override
    public String toString() {
        return "CustomerAccount{" +
                "accountNumber='" + getMaskedAccountNumber() + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", accountType='" + accountType + '\'' +
                ", currentBalance=" + currentBalance +
                ", creditLimit=" + creditLimit +
                ", minimumPaymentDue=" + minimumPaymentDue +
                ", paymentDueDate=" + paymentDueDate +
                ", accountStatus='" + accountStatus + '\'' +
                '}';
    }
}