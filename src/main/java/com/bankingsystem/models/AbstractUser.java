package com.bankingsystem.models;

import java.time.LocalDateTime;

public abstract class AbstractUser {
    protected Integer userId;
    protected String username;
    protected String passwordHash;
    protected String role;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected boolean isActive;
    protected LocalDateTime lastLogin;
    protected LocalDateTime createdDate;
    protected LocalDateTime updatedDate;

    // Enhanced Security Fields
    protected LocalDateTime passwordExpiryDate;
    protected LocalDateTime lastPasswordChange;
    protected int failedLoginAttempts;
    protected LocalDateTime lockedUntil;
    protected boolean mfaEnabled;
    protected String passwordHistory;
    
    public AbstractUser() {
        this.isActive = true;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.lastPasswordChange = LocalDateTime.now();
        this.passwordExpiryDate = LocalDateTime.now().plusDays(90);
        this.failedLoginAttempts = 0;
        this.mfaEnabled = false;
        this.passwordHistory = "";
    }
    
    public AbstractUser(String username, String passwordHash, String role, 
                       String firstName, String lastName, String email) {
        this();
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
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
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
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
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public abstract boolean hasPermission(String permission);

    // Enhanced Security Getters and Setters
    public LocalDateTime getPasswordExpiryDate() {
        return passwordExpiryDate;
    }

    public void setPasswordExpiryDate(LocalDateTime passwordExpiryDate) {
        this.passwordExpiryDate = passwordExpiryDate;
    }

    public LocalDateTime getLastPasswordChange() {
        return lastPasswordChange;
    }

    public void setLastPasswordChange(LocalDateTime lastPasswordChange) {
        this.lastPasswordChange = lastPasswordChange;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    public String getPasswordHistory() {
        return passwordHistory;
    }

    public void setPasswordHistory(String passwordHistory) {
        this.passwordHistory = passwordHistory;
    }

    public boolean isPasswordExpired() {
        return passwordExpiryDate != null && LocalDateTime.now().isAfter(passwordExpiryDate);
    }

    public boolean isAccountLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', role='%s', name='%s'}",
                           userId, username, role, getFullName());
    }
}
