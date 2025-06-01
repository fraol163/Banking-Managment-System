package com.bankingsystem.exceptions;

public class InvalidAccountException extends Exception {
    private final String accountNumber;
    
    public InvalidAccountException(String message, String accountNumber) {
        super(message);
        this.accountNumber = accountNumber;
    }
    
    public InvalidAccountException(String message, String accountNumber, Throwable cause) {
        super(message, cause);
        this.accountNumber = accountNumber;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    @Override
    public String getMessage() {
        return String.format("%s Account Number: %s", super.getMessage(), accountNumber);
    }
}
