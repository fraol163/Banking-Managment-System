package com.bankingsystem.security;

import com.bankingsystem.config.AppConfig;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;

public class PasswordValidator {
    private static final Logger LOGGER = Logger.getLogger(PasswordValidator.class.getName());
    private static PasswordPolicy defaultPolicy = new PasswordPolicy();
    
    public static PasswordValidationResult validatePassword(String password) {
        return defaultPolicy.validatePassword(password);
    }
    
    public static PasswordValidationResult validatePassword(String password, PasswordPolicy policy) {
        if (policy == null) {
            policy = defaultPolicy;
        }
        return policy.validatePassword(password);
    }
    
    public static boolean isPasswordValid(String password) {
        PasswordValidationResult result = validatePassword(password);
        return result.isValid();
    }
    
    public static boolean isPasswordStrong(String password) {
        PasswordValidationResult result = validatePassword(password);
        return result.isValid() && result.getStrengthLevel().isStrong();
    }
    
    public static boolean isPasswordExpired(LocalDateTime lastPasswordChange) {
        if (lastPasswordChange == null) {
            return true;
        }
        
        LocalDateTime expiryDate = lastPasswordChange.plusDays(defaultPolicy.getPasswordExpiryDays());
        return LocalDateTime.now().isAfter(expiryDate);
    }
    
    public static long getDaysUntilExpiry(LocalDateTime lastPasswordChange) {
        if (lastPasswordChange == null) {
            return 0;
        }
        
        LocalDateTime expiryDate = lastPasswordChange.plusDays(defaultPolicy.getPasswordExpiryDays());
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isAfter(expiryDate)) {
            return 0;
        }
        
        return ChronoUnit.DAYS.between(now, expiryDate);
    }
    
    public static boolean isPasswordInHistory(String newPasswordHash, List<String> passwordHistory) {
        if (passwordHistory == null || passwordHistory.isEmpty()) {
            return false;
        }
        
        return passwordHistory.contains(newPasswordHash);
    }
    
    public static String generatePasswordRequirements() {
        return defaultPolicy.generatePasswordRequirementsText();
    }
    
    public static String generatePasswordStrengthAdvice(PasswordValidationResult result) {
        StringBuilder advice = new StringBuilder();
        
        advice.append("Password Strength: ").append(result.getStrengthLevel().getDescription());
        advice.append(" (").append(result.getStrengthScore()).append("/100)\n\n");
        
        if (result.hasErrors()) {
            advice.append("Issues to fix:\n");
            for (String error : result.getErrors()) {
                advice.append("• ").append(error).append("\n");
            }
            advice.append("\n");
        }
        
        if (result.hasWarnings()) {
            advice.append("Recommendations:\n");
            for (String warning : result.getWarnings()) {
                advice.append("• ").append(warning).append("\n");
            }
            advice.append("\n");
        }
        
        if (!result.getStrengthLevel().isStrong()) {
            advice.append("Tips to improve password strength:\n");
            advice.append("• Use a longer password (12+ characters)\n");
            advice.append("• Mix uppercase and lowercase letters\n");
            advice.append("• Include numbers and special characters\n");
            advice.append("• Avoid common words and patterns\n");
            advice.append("• Use unique characters throughout\n");
        }
        
        return advice.toString();
    }
    
    public static boolean shouldWarnAboutExpiry(LocalDateTime lastPasswordChange) {
        long daysUntilExpiry = getDaysUntilExpiry(lastPasswordChange);
        return daysUntilExpiry <= AppConfig.PASSWORD_EXPIRY_WARNING_DAYS && daysUntilExpiry > 0;
    }
    
    public static String getExpiryWarningMessage(LocalDateTime lastPasswordChange) {
        long daysUntilExpiry = getDaysUntilExpiry(lastPasswordChange);
        
        if (daysUntilExpiry <= 0) {
            return "Your password has expired. Please change it immediately.";
        } else if (daysUntilExpiry == 1) {
            return "Your password will expire tomorrow. Please change it soon.";
        } else {
            return String.format("Your password will expire in %d days. Please change it soon.", daysUntilExpiry);
        }
    }
    
    public static PasswordPolicy getDefaultPolicy() {
        return defaultPolicy;
    }
    
    public static void setDefaultPolicy(PasswordPolicy policy) {
        if (policy != null) {
            defaultPolicy = policy;
            LOGGER.info("Default password policy updated");
        }
    }
    
    public static PasswordPolicy createCustomPolicy(int minLength, boolean requireUppercase, 
                                                   boolean requireLowercase, boolean requireNumbers, 
                                                   boolean requireSpecialChars, int expiryDays) {
        PasswordPolicy policy = new PasswordPolicy();
        policy.setMinLength(minLength);
        policy.setRequireUppercase(requireUppercase);
        policy.setRequireLowercase(requireLowercase);
        policy.setRequireNumbers(requireNumbers);
        policy.setRequireSpecialChars(requireSpecialChars);
        policy.setPasswordExpiryDays(expiryDays);
        
        return policy;
    }
    
    public static boolean meetsMinimumRequirements(String password) {
        PasswordValidationResult result = validatePassword(password);
        return result.isValid() && result.getStrengthLevel().isAcceptable();
    }
    
    public static int calculatePasswordComplexity(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int complexity = 0;
        
        if (password.matches(".*[a-z].*")) complexity++;
        if (password.matches(".*[A-Z].*")) complexity++;
        if (password.matches(".*[0-9].*")) complexity++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) complexity++;
        
        if (password.length() >= 8) complexity++;
        if (password.length() >= 12) complexity++;
        if (password.length() >= 16) complexity++;
        
        long uniqueChars = password.chars().distinct().count();
        if (uniqueChars >= password.length() * 0.8) complexity++;
        
        return complexity;
    }
}
