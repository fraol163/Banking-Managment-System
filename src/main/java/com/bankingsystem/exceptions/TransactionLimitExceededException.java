package com.bankingsystem.exceptions;

import java.math.BigDecimal;

public class TransactionLimitExceededException extends Exception {
    private final BigDecimal requestedAmount;
    private final BigDecimal dailyLimit;
    private final BigDecimal currentDailyTotal;
    
    public TransactionLimitExceededException(String message, BigDecimal requestedAmount, 
                                           BigDecimal dailyLimit, BigDecimal currentDailyTotal) {
        super(message);
        this.requestedAmount = requestedAmount;
        this.dailyLimit = dailyLimit;
        this.currentDailyTotal = currentDailyTotal;
    }
    
    public TransactionLimitExceededException(String message, BigDecimal requestedAmount, 
                                           BigDecimal dailyLimit, BigDecimal currentDailyTotal, Throwable cause) {
        super(message, cause);
        this.requestedAmount = requestedAmount;
        this.dailyLimit = dailyLimit;
        this.currentDailyTotal = currentDailyTotal;
    }
    
    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
    
    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }
    
    public BigDecimal getCurrentDailyTotal() {
        return currentDailyTotal;
    }
    
    @Override
    public String getMessage() {
        return String.format("%s Requested: $%.2f, Daily limit: $%.2f, Current daily total: $%.2f", 
                           super.getMessage(), requestedAmount, dailyLimit, currentDailyTotal);
    }
}
