package com.bankingsystem.utils;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * User-friendly validation utility with clear error messages and input limits
 */
public class UserFriendlyValidation {
    
    // Validation constants
    public static final BigDecimal MIN_INITIAL_DEPOSIT = new BigDecimal("100.00");
    public static final BigDecimal MAX_INITIAL_DEPOSIT = new BigDecimal("50000.00");
    public static final BigDecimal MIN_TRANSACTION_AMOUNT = new BigDecimal("0.01");
    public static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("100000.00");
    public static final BigDecimal MIN_TRANSFER_AMOUNT = new BigDecimal("1.00");
    public static final BigDecimal MAX_TRANSFER_AMOUNT = new BigDecimal("50000.00");
    
    // Regex patterns
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{3}-[0-9]{3}-[0-9]{4}$");
    private static final Pattern SSN_PATTERN = Pattern.compile("^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
    
    /**
     * Validation result containing success status and user-friendly message
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        
        public static ValidationResult success() {
            return new ValidationResult(true, "");
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
    }
    
    /**
     * Validate initial deposit amount for account creation
     */
    public static ValidationResult validateInitialDeposit(BigDecimal amount) {
        if (amount == null) {
            return ValidationResult.error("Initial deposit amount is required.");
        }
        
        if (amount.compareTo(MIN_INITIAL_DEPOSIT) < 0) {
            return ValidationResult.error(String.format(
                "Initial deposit must be at least $%.2f. You entered $%.2f.",
                MIN_INITIAL_DEPOSIT, amount));
        }
        
        if (amount.compareTo(MAX_INITIAL_DEPOSIT) > 0) {
            return ValidationResult.error(String.format(
                "Initial deposit cannot exceed $%.2f. You entered $%.2f.",
                MAX_INITIAL_DEPOSIT, amount));
        }
        
        if (amount.scale() > 2) {
            return ValidationResult.error("Initial deposit cannot have more than 2 decimal places.");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validate transaction amount (deposit/withdrawal)
     */
    public static ValidationResult validateTransactionAmount(BigDecimal amount, String transactionType) {
        if (amount == null) {
            return ValidationResult.error(transactionType + " amount is required.");
        }
        
        if (amount.compareTo(MIN_TRANSACTION_AMOUNT) < 0) {
            return ValidationResult.error(String.format(
                "%s amount must be at least $%.2f. You entered $%.2f.",
                transactionType, MIN_TRANSACTION_AMOUNT, amount));
        }
        
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            return ValidationResult.error(String.format(
                "%s amount cannot exceed $%.2f. You entered $%.2f.",
                transactionType, MAX_TRANSACTION_AMOUNT, amount));
        }
        
        if (amount.scale() > 2) {
            return ValidationResult.error(transactionType + " amount cannot have more than 2 decimal places.");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validate transfer amount with specific transfer limits
     */
    public static ValidationResult validateTransferAmount(BigDecimal amount) {
        if (amount == null) {
            return ValidationResult.error("Transfer amount is required.");
        }
        
        if (amount.compareTo(MIN_TRANSFER_AMOUNT) < 0) {
            return ValidationResult.error(String.format(
                "Transfer amount must be at least $%.2f. You entered $%.2f.",
                MIN_TRANSFER_AMOUNT, amount));
        }
        
        if (amount.compareTo(MAX_TRANSFER_AMOUNT) > 0) {
            return ValidationResult.error(String.format(
                "Transfer amount cannot exceed $%.2f. You entered $%.2f.\n" +
                "For larger transfers, please contact your manager or use multiple transactions.",
                MAX_TRANSFER_AMOUNT, amount));
        }
        
        if (amount.scale() > 2) {
            return ValidationResult.error("Transfer amount cannot have more than 2 decimal places.");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validate account number format
     */
    public static ValidationResult validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return ValidationResult.error("Account number is required.");
        }
        
        String trimmed = accountNumber.trim();
        if (!ACCOUNT_NUMBER_PATTERN.matcher(trimmed).matches()) {
            return ValidationResult.error(
                "Account number must be exactly 10 digits. Example: 1234567890");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validate customer name
     */
    public static ValidationResult validateCustomerName(String firstName, String lastName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            return ValidationResult.error("First name is required.");
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            return ValidationResult.error("Last name is required.");
        }
        
        if (firstName.trim().length() < 2) {
            return ValidationResult.error("First name must be at least 2 characters long.");
        }
        
        if (lastName.trim().length() < 2) {
            return ValidationResult.error("Last name must be at least 2 characters long.");
        }
        
        if (firstName.trim().length() > 50) {
            return ValidationResult.error("First name cannot exceed 50 characters.");
        }
        
        if (lastName.trim().length() > 50) {
            return ValidationResult.error("Last name cannot exceed 50 characters.");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validate email address
     */
    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.error("Email address is required.");
        }
        
        String trimmed = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            return ValidationResult.error(
                "Please enter a valid email address. Example: user@example.com");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validate phone number
     */
    public static ValidationResult validatePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return ValidationResult.error("Phone number is required.");
        }
        
        String trimmed = phone.trim();
        if (!PHONE_PATTERN.matcher(trimmed).matches()) {
            return ValidationResult.error(
                "Phone number must be in format: 123-456-7890");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validate SSN
     */
    public static ValidationResult validateSSN(String ssn) {
        if (ssn == null || ssn.trim().isEmpty()) {
            return ValidationResult.error("Social Security Number is required.");
        }
        
        String trimmed = ssn.trim();
        if (!SSN_PATTERN.matcher(trimmed).matches()) {
            return ValidationResult.error(
                "SSN must be in format: 123-45-6789");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validate address
     */
    public static ValidationResult validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return ValidationResult.error("Address is required.");
        }
        
        if (address.trim().length() < 10) {
            return ValidationResult.error("Address must be at least 10 characters long.");
        }
        
        if (address.trim().length() > 200) {
            return ValidationResult.error("Address cannot exceed 200 characters.");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Get user-friendly limits information for display
     */
    public static String getInitialDepositLimits() {
        return String.format("Initial deposit must be between $%.2f and $%.2f", 
                           MIN_INITIAL_DEPOSIT, MAX_INITIAL_DEPOSIT);
    }
    
    public static String getTransactionLimits() {
        return String.format("Transaction amount must be between $%.2f and $%.2f", 
                           MIN_TRANSACTION_AMOUNT, MAX_TRANSACTION_AMOUNT);
    }
    
    public static String getTransferLimits() {
        return String.format("Transfer amount must be between $%.2f and $%.2f", 
                           MIN_TRANSFER_AMOUNT, MAX_TRANSFER_AMOUNT);
    }
}
