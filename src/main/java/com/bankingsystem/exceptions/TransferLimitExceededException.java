package com.bankingsystem.exceptions;

import java.math.BigDecimal;

public class TransferLimitExceededException extends Exception {
    private BigDecimal requestedAmount;
    private BigDecimal limitAmount;
    private String limitType;
    
    public TransferLimitExceededException(String message) {
        super(message);
    }
    
    public TransferLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TransferLimitExceededException(String message, BigDecimal requestedAmount, BigDecimal limitAmount, String limitType) {
        super(message);
        this.requestedAmount = requestedAmount;
        this.limitAmount = limitAmount;
        this.limitType = limitType;
    }
    
    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
    
    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }
    
    public BigDecimal getLimitAmount() {
        return limitAmount;
    }
    
    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }
    
    public String getLimitType() {
        return limitType;
    }
    
    public void setLimitType(String limitType) {
        this.limitType = limitType;
    }
    
    @Override
    public String toString() {
        if (requestedAmount != null && limitAmount != null && limitType != null) {
            return String.format("TransferLimitExceededException: %s (Requested: $%.2f, %s Limit: $%.2f)",
                               getMessage(), requestedAmount, limitType, limitAmount);
        }
        return super.toString();
    }
}
