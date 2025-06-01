package com.bankingsystem.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class AbstractTransaction {
    protected Integer transactionId;
    protected Integer accountId;
    protected Integer transactionTypeId;
    protected BigDecimal amount;
    protected BigDecimal balanceBefore;
    protected BigDecimal balanceAfter;
    protected String description;
    protected String referenceNumber;
    protected String status;
    protected LocalDateTime createdDate;
    protected Integer createdBy;

    public AbstractTransaction() {
        this.createdDate = LocalDateTime.now();
        this.status = "Completed";
    }

    public AbstractTransaction(Integer accountId, Integer transactionTypeId, BigDecimal amount,
                             BigDecimal balanceBefore, BigDecimal balanceAfter, String description,
                             String referenceNumber, Integer createdBy) {
        this();
        this.accountId = accountId;
        this.transactionTypeId = transactionTypeId;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.referenceNumber = referenceNumber;
        this.createdBy = createdBy;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Integer getTransactionTypeId() {
        return transactionTypeId;
    }

    public void setTransactionTypeId(Integer transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
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

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public abstract String getTransactionType();

    public abstract boolean isReversible();

    @Override
    public String toString() {
        return String.format("Transaction{id=%d, account=%d, amount=$%.2f, type='%s', ref='%s'}",
                           transactionId, accountId, amount, getTransactionType(), referenceNumber);
    }
}
