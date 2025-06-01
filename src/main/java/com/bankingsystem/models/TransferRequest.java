package com.bankingsystem.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferRequest {
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String description;
    private Integer userId;
    private LocalDateTime requestTime;
    private String transferType;
    
    public TransferRequest() {
        this.requestTime = LocalDateTime.now();
        this.transferType = "Internal";
    }
    
    public TransferRequest(String fromAccountNumber, String toAccountNumber, BigDecimal amount, 
                          String description, Integer userId) {
        this();
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.description = description;
        this.userId = userId;
    }
    
    public String getFromAccountNumber() {
        return fromAccountNumber;
    }
    
    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }
    
    public String getToAccountNumber() {
        return toAccountNumber;
    }
    
    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public LocalDateTime getRequestTime() {
        return requestTime;
    }
    
    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }
    
    public String getTransferType() {
        return transferType;
    }
    
    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }
    
    public boolean isValid() {
        return fromAccountNumber != null && !fromAccountNumber.trim().isEmpty() &&
               toAccountNumber != null && !toAccountNumber.trim().isEmpty() &&
               !fromAccountNumber.equals(toAccountNumber) &&
               amount != null && amount.compareTo(BigDecimal.ZERO) > 0 &&
               userId != null;
    }
    
    @Override
    public String toString() {
        return String.format("TransferRequest{from='%s', to='%s', amount=$%.2f, user=%d, type='%s'}",
                           fromAccountNumber, toAccountNumber, amount, userId, transferType);
    }
}
