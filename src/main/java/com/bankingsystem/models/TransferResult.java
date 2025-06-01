package com.bankingsystem.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransferResult {
    private boolean success;
    private String message;
    private String referenceNumber;
    private List<Transaction> transactions;
    private LocalDateTime completionTime;
    private String errorCode;
    private BigDecimal transferAmount;
    private String fromAccountNumber;
    private String toAccountNumber;
    
    public TransferResult() {
        this.completionTime = LocalDateTime.now();
    }
    
    public TransferResult(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }
    
    public TransferResult(boolean success, String message, String referenceNumber, List<Transaction> transactions) {
        this(success, message);
        this.referenceNumber = referenceNumber;
        this.transactions = transactions;
        
        if (transactions != null && !transactions.isEmpty()) {
            Transaction debitTransaction = transactions.get(0);
            this.transferAmount = debitTransaction.getAmount().abs();
            this.fromAccountNumber = getAccountNumberFromTransaction(debitTransaction);
            
            if (transactions.size() > 1) {
                Transaction creditTransaction = transactions.get(1);
                this.toAccountNumber = getAccountNumberFromTransaction(creditTransaction);
            }
        }
    }
    
    public static TransferResult failure(String message, String errorCode) {
        TransferResult result = new TransferResult(false, message);
        result.setErrorCode(errorCode);
        return result;
    }
    
    public static TransferResult success(String message, String referenceNumber, List<Transaction> transactions) {
        return new TransferResult(true, message, referenceNumber, transactions);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getReferenceNumber() {
        return referenceNumber;
    }
    
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
    
    public LocalDateTime getCompletionTime() {
        return completionTime;
    }
    
    public void setCompletionTime(LocalDateTime completionTime) {
        this.completionTime = completionTime;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public BigDecimal getTransferAmount() {
        return transferAmount;
    }
    
    public void setTransferAmount(BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
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
    
    public boolean hasTransactions() {
        return transactions != null && !transactions.isEmpty();
    }
    
    public int getTransactionCount() {
        return transactions != null ? transactions.size() : 0;
    }
    
    private String getAccountNumberFromTransaction(Transaction transaction) {
        if (transaction != null && transaction.getDescription() != null) {
            String desc = transaction.getDescription();
            if (desc.contains("to ")) {
                return desc.substring(desc.lastIndexOf("to ") + 3);
            } else if (desc.contains("from ")) {
                return desc.substring(desc.lastIndexOf("from ") + 5);
            }
        }
        return "Unknown";
    }
    
    @Override
    public String toString() {
        if (success) {
            return String.format("TransferResult{success=true, amount=$%.2f, from='%s', to='%s', ref='%s'}",
                               transferAmount, fromAccountNumber, toAccountNumber, referenceNumber);
        } else {
            return String.format("TransferResult{success=false, message='%s', errorCode='%s'}",
                               message, errorCode);
        }
    }
}
