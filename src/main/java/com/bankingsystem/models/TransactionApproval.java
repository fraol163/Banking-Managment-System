package com.bankingsystem.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionApproval {
    private Integer approvalId;
    private String transactionType;
    private String accountNumber;
    private BigDecimal amount;
    private String description;
    private Integer requestedByUserId;
    private String requestedByUserRole;
    private Integer approvedByUserId;
    private String approvalStatus; // PENDING, APPROVED, REJECTED
    private LocalDateTime requestedDate;
    private LocalDateTime approvedDate;
    private String approvalComments;
    private String rejectionReason;
    
    // Constructors
    public TransactionApproval() {
        this.requestedDate = LocalDateTime.now();
        this.approvalStatus = "PENDING";
    }
    
    public TransactionApproval(String transactionType, String accountNumber, BigDecimal amount, 
                             String description, Integer requestedByUserId, String requestedByUserRole) {
        this();
        this.transactionType = transactionType;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.description = description;
        this.requestedByUserId = requestedByUserId;
        this.requestedByUserRole = requestedByUserRole;
    }
    
    // Getters and Setters
    public Integer getApprovalId() {
        return approvalId;
    }
    
    public void setApprovalId(Integer approvalId) {
        this.approvalId = approvalId;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
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
    
    public Integer getRequestedByUserId() {
        return requestedByUserId;
    }
    
    public void setRequestedByUserId(Integer requestedByUserId) {
        this.requestedByUserId = requestedByUserId;
    }
    
    public String getRequestedByUserRole() {
        return requestedByUserRole;
    }
    
    public void setRequestedByUserRole(String requestedByUserRole) {
        this.requestedByUserRole = requestedByUserRole;
    }
    
    public Integer getApprovedByUserId() {
        return approvedByUserId;
    }
    
    public void setApprovedByUserId(Integer approvedByUserId) {
        this.approvedByUserId = approvedByUserId;
    }
    
    public String getApprovalStatus() {
        return approvalStatus;
    }
    
    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
    
    public LocalDateTime getRequestedDate() {
        return requestedDate;
    }
    
    public void setRequestedDate(LocalDateTime requestedDate) {
        this.requestedDate = requestedDate;
    }
    
    public LocalDateTime getApprovedDate() {
        return approvedDate;
    }
    
    public void setApprovedDate(LocalDateTime approvedDate) {
        this.approvedDate = approvedDate;
    }
    
    public String getApprovalComments() {
        return approvalComments;
    }
    
    public void setApprovalComments(String approvalComments) {
        this.approvalComments = approvalComments;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    // Utility methods
    public boolean isPending() {
        return "PENDING".equals(approvalStatus);
    }
    
    public boolean isApproved() {
        return "APPROVED".equals(approvalStatus);
    }
    
    public boolean isRejected() {
        return "REJECTED".equals(approvalStatus);
    }
    
    public void approve(Integer approvedByUserId, String comments) {
        this.approvalStatus = "APPROVED";
        this.approvedByUserId = approvedByUserId;
        this.approvedDate = LocalDateTime.now();
        this.approvalComments = comments;
    }
    
    public void reject(Integer rejectedByUserId, String reason) {
        this.approvalStatus = "REJECTED";
        this.approvedByUserId = rejectedByUserId;
        this.approvedDate = LocalDateTime.now();
        this.rejectionReason = reason;
    }
    
    @Override
    public String toString() {
        return String.format("TransactionApproval{id=%d, type='%s', account='%s', amount=$%.2f, status='%s', requestedBy=%d}", 
                           approvalId, transactionType, accountNumber, amount, approvalStatus, requestedByUserId);
    }
}
