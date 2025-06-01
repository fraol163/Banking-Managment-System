package com.bankingsystem.utils;

import com.bankingsystem.config.AppConfig;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class ValidationUtil {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(AppConfig.PHONE_REGEX);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(AppConfig.EMAIL_REGEX);
    private static final Pattern NAME_PATTERN = Pattern.compile(AppConfig.NAME_REGEX);
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile(AppConfig.ACCOUNT_NUMBER_REGEX);
    private static final Pattern SSN_PATTERN = Pattern.compile(AppConfig.SSN_REGEX);
    
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        return cleanPhone.length() == 10 || cleanPhone.length() == 11;
    }
    
    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }
    
    public static boolean isValidAccountNumber(String accountNumber) {
        return accountNumber != null && ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches();
    }
    
    public static boolean isValidSSN(String ssn) {
        return ssn != null && SSN_PATTERN.matcher(ssn).matches();
    }
    
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < AppConfig.MIN_PASSWORD_LENGTH || 
            password.length() > AppConfig.MAX_PASSWORD_LENGTH) {
            return false;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
    
    public static boolean isValidAmount(BigDecimal amount) {
        if (amount == null) return false;
        
        if (amount.compareTo(new BigDecimal("0.01")) < 0) return false;
        if (amount.compareTo(new BigDecimal("999999.99")) > 0) return false;
        
        return amount.scale() <= 2;
    }
    
    public static boolean isValidDate(String dateString) {
        try {
            LocalDate.parse(dateString);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    public static boolean isValidAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return false;
        
        LocalDate now = LocalDate.now();
        int age = now.getYear() - dateOfBirth.getYear();
        
        if (now.getDayOfYear() < dateOfBirth.getDayOfYear()) {
            age--;
        }
        
        return age >= 18 && age <= 120;
    }
    
    public static String formatPhone(String phone) {
        if (phone == null) return null;
        
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        if (cleanPhone.length() == 10) {
            return String.format("(%s) %s-%s", 
                               cleanPhone.substring(0, 3),
                               cleanPhone.substring(3, 6),
                               cleanPhone.substring(6));
        } else if (cleanPhone.length() == 11) {
            return String.format("+%s (%s) %s-%s", 
                               cleanPhone.substring(0, 1),
                               cleanPhone.substring(1, 4),
                               cleanPhone.substring(4, 7),
                               cleanPhone.substring(7));
        }
        return phone;
    }
    
    public static String formatSSN(String ssn) {
        if (ssn == null) return null;
        
        String cleanSSN = ssn.replaceAll("[^0-9]", "");
        if (cleanSSN.length() == 9) {
            return String.format("%s-%s-%s", 
                               cleanSSN.substring(0, 3),
                               cleanSSN.substring(3, 5),
                               cleanSSN.substring(5));
        }
        return ssn;
    }
    
    public static String sanitizeInput(String input) {
        if (input == null) return null;
        
        return input.trim()
                   .replaceAll("[<>\"'&]", "")
                   .replaceAll("\\s+", " ");
    }
    
    public static boolean isValidUsername(String username) {
        if (username == null || username.length() < 3 || username.length() > 20) {
            return false;
        }
        
        return username.matches("^[a-zA-Z0-9_]+$");
    }
}
