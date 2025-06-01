package com.bankingsystem.exceptions;

import java.math.BigDecimal;

public class InsufficientFundsException extends Exception {
    private final BigDecimal availableBalance;
    private final BigDecimal requestedAmount;
    
    public InsufficientFundsException(String message, BigDecimal availableBalance, BigDecimal requestedAmount) {
        super(message);
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
    }
    
    public InsufficientFundsException(String message, BigDecimal availableBalance, BigDecimal requestedAmount, Throwable cause) {
        super(message, cause);
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
    }
    
    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }
    
    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
    
    @Override
    public String getMessage() {
        return String.format("%s Available balance: $%.2f, Requested amount: $%.2f", 
                           super.getMessage(), availableBalance, requestedAmount);
    }
}
