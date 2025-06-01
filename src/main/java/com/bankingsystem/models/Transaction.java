package com.bankingsystem.models;

import com.bankingsystem.config.AppConfig;
import java.math.BigDecimal;

public class Transaction extends AbstractTransaction {
    private String transactionTypeName;
    
    public Transaction() {
        super();
    }
    
    public Transaction(Integer accountId, Integer transactionTypeId, BigDecimal amount,
                      BigDecimal balanceBefore, BigDecimal balanceAfter, String description,
                      String referenceNumber, Integer createdBy) {
        super(accountId, transactionTypeId, amount, balanceBefore, balanceAfter, 
              description, referenceNumber, createdBy);
    }
    
    public String getTransactionTypeName() {
        return transactionTypeName;
    }
    
    public void setTransactionTypeName(String transactionTypeName) {
        this.transactionTypeName = transactionTypeName;
    }
    
    @Override
    public String getTransactionType() {
        return transactionTypeName != null ? transactionTypeName : "Unknown";
    }
    
    @Override
    public boolean isReversible() {
        if (transactionTypeName == null) {
            return false;
        }
        
        switch (transactionTypeName) {
            case AppConfig.TRANSACTION_TYPE_DEPOSIT:
            case AppConfig.TRANSACTION_TYPE_WITHDRAWAL:
            case AppConfig.TRANSACTION_TYPE_TRANSFER:
                return true;
            case AppConfig.TRANSACTION_TYPE_FEE:
            case AppConfig.TRANSACTION_TYPE_INTEREST:
            case AppConfig.TRANSACTION_TYPE_REVERSAL:
                return false;
            default:
                return false;
        }
    }
    
    public boolean isDebit() {
        if (transactionTypeName == null) {
            return false;
        }
        
        switch (transactionTypeName) {
            case AppConfig.TRANSACTION_TYPE_WITHDRAWAL:
            case AppConfig.TRANSACTION_TYPE_FEE:
                return true;
            case AppConfig.TRANSACTION_TYPE_DEPOSIT:
            case AppConfig.TRANSACTION_TYPE_INTEREST:
                return false;
            case AppConfig.TRANSACTION_TYPE_TRANSFER:
                return amount.compareTo(BigDecimal.ZERO) < 0;
            default:
                return false;
        }
    }
    
    public boolean isCredit() {
        return !isDebit();
    }
    
    public BigDecimal getAbsoluteAmount() {
        return amount.abs();
    }
}
