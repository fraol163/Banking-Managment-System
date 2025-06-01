package com.bankingsystem.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class AbstractAccount {
    protected Integer accountId;
    protected String accountNumber;
    protected Integer customerId;
    protected Integer accountTypeId;
    protected BigDecimal balance;
    protected String status;
    protected LocalDateTime createdDate;
    protected LocalDateTime updatedDate;
    
    public AbstractAccount() {
        this.balance = BigDecimal.ZERO;
        this.status = "Active";
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }
    
    public AbstractAccount(String accountNumber, Integer customerId, Integer accountTypeId) {
        this();
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.accountTypeId = accountTypeId;
    }
    
    public Integer getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public Integer getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }
    
    public Integer getAccountTypeId() {
        return accountTypeId;
    }
    
    public void setAccountTypeId(Integer accountTypeId) {
        this.accountTypeId = accountTypeId;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    public abstract BigDecimal getMinimumBalance();
    
    public abstract BigDecimal getInterestRate();
    
    public abstract BigDecimal getOverdraftLimit();
    
    public abstract BigDecimal getOverdraftFee();
    
    public abstract BigDecimal getDailyWithdrawalLimit();
    
    public abstract boolean canWithdraw(BigDecimal amount);
    
    public abstract BigDecimal calculateInterest();
    
    @Override
    public String toString() {
        return String.format("Account{id=%d, number='%s', balance=$%.2f, status='%s'}", 
                           accountId, accountNumber, balance, status);
    }
}
