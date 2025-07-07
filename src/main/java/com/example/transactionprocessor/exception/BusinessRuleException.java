package com.example.transactionprocessor.exception;

/**
 * Custom exception for business rule violations.
 * This exception should not trigger circuit breakers as it represents
 * expected business behavior rather than system failures.
 */
public class BusinessRuleException extends RuntimeException {
    
    private final String businessCode;
    private final String businessReason;
    
    public BusinessRuleException(String businessCode, String businessReason) {
        super(businessReason);
        this.businessCode = businessCode;
        this.businessReason = businessReason;
    }
    
    public BusinessRuleException(String businessCode, String businessReason, Throwable cause) {
        super(businessReason, cause);
        this.businessCode = businessCode;
        this.businessReason = businessReason;
    }
    
    public String getBusinessCode() {
        return businessCode;
    }
    
    public String getBusinessReason() {
        return businessReason;
    }
    
    @Override
    public String toString() {
        return "BusinessRuleException{" +
                "businessCode='" + businessCode + '\'' +
                ", businessReason='" + businessReason + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}