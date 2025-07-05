package com.example.transactionprocessor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public class CreditCardInfo {
    
    @JsonProperty("cardNumber")
    private String cardNumber;
    
    @JsonProperty("holderName")
    private String holderName;
    
    @JsonProperty("expiryDate")
    private LocalDate expiryDate;
    
    @JsonProperty("cvv")
    private String cvv;
    
    @JsonProperty("cardType")
    private String cardType;
    
    public CreditCardInfo() {}
    
    public CreditCardInfo(String cardNumber, String holderName, LocalDate expiryDate, String cvv, String cardType) {
        this.cardNumber = cardNumber;
        this.holderName = holderName;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.cardType = cardType;
    }
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    public String getHolderName() {
        return holderName;
    }
    
    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }
    
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public String getCvv() {
        return cvv;
    }
    
    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
    
    public String getCardType() {
        return cardType;
    }
    
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
    
    @Override
    public String toString() {
        return "CreditCardInfo{" +
                "cardNumber='" + getMaskedCardNumber() + '\'' +
                ", holderName='" + holderName + '\'' +
                ", expiryDate=" + expiryDate +
                ", cardType='" + cardType + '\'' +
                '}';
    }
}