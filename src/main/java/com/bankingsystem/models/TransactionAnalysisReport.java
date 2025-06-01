package com.bankingsystem.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionAnalysisReport {
    private String transactionType;
    private Integer transactionCount;
    private BigDecimal totalAmount;
    private BigDecimal averageAmount;
    private BigDecimal minimumAmount;
    private BigDecimal maximumAmount;
    private Integer approvalsPending;
    private Integer approvalsCompleted;
    private Integer transactionsCancelled;
    private Double averageProcessingTime;
    private LocalDateTime reportGeneratedDate;
    private String periodDescription;
    private String processingStatistics;
    
    public TransactionAnalysisReport() {
        this.reportGeneratedDate = LocalDateTime.now();
        this.transactionCount = 0;
        this.totalAmount = BigDecimal.ZERO;
        this.averageAmount = BigDecimal.ZERO;
        this.minimumAmount = BigDecimal.ZERO;
        this.maximumAmount = BigDecimal.ZERO;
        this.approvalsPending = 0;
        this.approvalsCompleted = 0;
        this.transactionsCancelled = 0;
        this.averageProcessingTime = 0.0;
    }
    
    public TransactionAnalysisReport(String transactionType) {
        this();
        this.transactionType = transactionType;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public Integer getTransactionCount() {
        return transactionCount;
    }
    
    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getAverageAmount() {
        return averageAmount;
    }
    
    public void setAverageAmount(BigDecimal averageAmount) {
        this.averageAmount = averageAmount;
    }
    
    public BigDecimal getMinimumAmount() {
        return minimumAmount;
    }
    
    public void setMinimumAmount(BigDecimal minimumAmount) {
        this.minimumAmount = minimumAmount;
    }
    
    public BigDecimal getMaximumAmount() {
        return maximumAmount;
    }
    
    public void setMaximumAmount(BigDecimal maximumAmount) {
        this.maximumAmount = maximumAmount;
    }
    
    public Integer getApprovalsPending() {
        return approvalsPending;
    }
    
    public void setApprovalsPending(Integer approvalsPending) {
        this.approvalsPending = approvalsPending;
    }
    
    public Integer getApprovalsCompleted() {
        return approvalsCompleted;
    }
    
    public void setApprovalsCompleted(Integer approvalsCompleted) {
        this.approvalsCompleted = approvalsCompleted;
    }
    
    public Integer getTransactionsCancelled() {
        return transactionsCancelled;
    }
    
    public void setTransactionsCancelled(Integer transactionsCancelled) {
        this.transactionsCancelled = transactionsCancelled;
    }
    
    public Double getAverageProcessingTime() {
        return averageProcessingTime;
    }
    
    public void setAverageProcessingTime(Double averageProcessingTime) {
        this.averageProcessingTime = averageProcessingTime;
    }
    
    public LocalDateTime getReportGeneratedDate() {
        return reportGeneratedDate;
    }
    
    public void setReportGeneratedDate(LocalDateTime reportGeneratedDate) {
        this.reportGeneratedDate = reportGeneratedDate;
    }
    
    public String getPeriodDescription() {
        return periodDescription;
    }
    
    public void setPeriodDescription(String periodDescription) {
        this.periodDescription = periodDescription;
    }
    
    public String getProcessingStatistics() {
        return processingStatistics;
    }
    
    public void setProcessingStatistics(String processingStatistics) {
        this.processingStatistics = processingStatistics;
    }
    
    public void addTransaction(BigDecimal amount) {
        this.transactionCount++;
        this.totalAmount = this.totalAmount.add(amount);
        
        if (this.transactionCount == 1) {
            this.minimumAmount = amount;
            this.maximumAmount = amount;
        } else {
            if (amount.compareTo(this.minimumAmount) < 0) {
                this.minimumAmount = amount;
            }
            if (amount.compareTo(this.maximumAmount) > 0) {
                this.maximumAmount = amount;
            }
        }
        
        this.averageAmount = this.totalAmount.divide(BigDecimal.valueOf(this.transactionCount), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    public void incrementApprovalsPending() {
        this.approvalsPending++;
    }
    
    public void incrementApprovalsCompleted() {
        this.approvalsCompleted++;
    }
    
    public void incrementTransactionsCancelled() {
        this.transactionsCancelled++;
    }
    
    @Override
    public String toString() {
        return String.format("TransactionAnalysisReport{type='%s', count=%d, total=$%.2f, avg=$%.2f}", 
                           transactionType, transactionCount, totalAmount, averageAmount);
    }
}
