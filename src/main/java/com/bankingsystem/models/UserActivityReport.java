package com.bankingsystem.models;

import java.time.LocalDateTime;

public class UserActivityReport {
    private Integer userId;
    private String username;
    private String role;
    private LocalDateTime lastLogin;
    private Integer loginCount;
    private Integer transactionCount;
    private Integer accountsManaged;
    private Integer usersManaged;
    private LocalDateTime reportGeneratedDate;
    private String activitySummary;
    
    public UserActivityReport() {
        this.reportGeneratedDate = LocalDateTime.now();
    }
    
    public UserActivityReport(Integer userId, String username, String role) {
        this();
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.loginCount = 0;
        this.transactionCount = 0;
        this.accountsManaged = 0;
        this.usersManaged = 0;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public Integer getLoginCount() {
        return loginCount;
    }
    
    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }
    
    public Integer getTransactionCount() {
        return transactionCount;
    }
    
    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }
    
    public Integer getAccountsManaged() {
        return accountsManaged;
    }
    
    public void setAccountsManaged(Integer accountsManaged) {
        this.accountsManaged = accountsManaged;
    }
    
    public Integer getUsersManaged() {
        return usersManaged;
    }
    
    public void setUsersManaged(Integer usersManaged) {
        this.usersManaged = usersManaged;
    }
    
    public LocalDateTime getReportGeneratedDate() {
        return reportGeneratedDate;
    }
    
    public void setReportGeneratedDate(LocalDateTime reportGeneratedDate) {
        this.reportGeneratedDate = reportGeneratedDate;
    }
    
    public String getActivitySummary() {
        return activitySummary;
    }
    
    public void setActivitySummary(String activitySummary) {
        this.activitySummary = activitySummary;
    }
    
    public void incrementLoginCount() {
        this.loginCount++;
        this.lastLogin = LocalDateTime.now();
    }
    
    public void incrementTransactionCount() {
        this.transactionCount++;
    }
    
    public void incrementAccountsManaged() {
        this.accountsManaged++;
    }
    
    public void incrementUsersManaged() {
        this.usersManaged++;
    }
    
    @Override
    public String toString() {
        return String.format("UserActivityReport{user='%s', role='%s', logins=%d, transactions=%d}", 
                           username, role, loginCount, transactionCount);
    }
}
