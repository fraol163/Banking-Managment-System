package com.bankingsystem.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountSummaryReport {
    private Integer accountId;
    private String accountNumber;
    private String accountType;
    private String customerName;
    private BigDecimal currentBalance;
    private BigDecimal openingBalance;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private Integer transactionCount;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime lastActivityDate;
    private LocalDateTime reportGeneratedDate;
    private String statusHistory;
    
    public AccountSummaryReport() {
        this.reportGeneratedDate = LocalDateTime.now();
        this.currentBalance = BigDecimal.ZERO;
        this.openingBalance = BigDecimal.ZERO;
        this.totalDeposits = BigDecimal.ZERO;
        this.totalWithdrawals = BigDecimal.ZERO;
        this.transactionCount = 0;
    }
    
    public AccountSummaryReport(Integer accountId, String accountNumber, String accountType, String customerName) {
        this();
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.customerName = customerName;
        this.status = "Active";
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
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }
    
    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }
    
    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }
    
    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }
    
    public BigDecimal getTotalDeposits() {
        return totalDeposits;
    }
    
    public void setTotalDeposits(BigDecimal totalDeposits) {
        this.totalDeposits = totalDeposits;
    }
    
    public BigDecimal getTotalWithdrawals() {
        return totalWithdrawals;
    }
    
    public void setTotalWithdrawals(BigDecimal totalWithdrawals) {
        this.totalWithdrawals = totalWithdrawals;
    }
    
    public Integer getTransactionCount() {
        return transactionCount;
    }
    
    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
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
    
    public LocalDateTime getLastActivityDate() {
        return lastActivityDate;
    }
    
    public void setLastActivityDate(LocalDateTime lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }
    
    public LocalDateTime getReportGeneratedDate() {
        return reportGeneratedDate;
    }
    
    public void setReportGeneratedDate(LocalDateTime reportGeneratedDate) {
        this.reportGeneratedDate = reportGeneratedDate;
    }
    
    public String getStatusHistory() {
        return statusHistory;
    }
    
    public void setStatusHistory(String statusHistory) {
        this.statusHistory = statusHistory;
    }
    
    public void addDeposit(BigDecimal amount) {
        this.totalDeposits = this.totalDeposits.add(amount);
        this.transactionCount++;
        this.lastActivityDate = LocalDateTime.now();
    }
    
    public void addWithdrawal(BigDecimal amount) {
        this.totalWithdrawals = this.totalWithdrawals.add(amount);
        this.transactionCount++;
        this.lastActivityDate = LocalDateTime.now();
    }
    
    public BigDecimal getNetChange() {
        return totalDeposits.subtract(totalWithdrawals);
    }
    
    @Override
    public String toString() {
        return String.format("AccountSummaryReport{account='%s', type='%s', balance=$%.2f, transactions=%d}", 
                           accountNumber, accountType, currentBalance, transactionCount);
    }
}
