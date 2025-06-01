package com.bankingsystem.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class PasswordPolicy {
    private int minLength;
    private int maxLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireNumbers;
    private boolean requireSpecialChars;
    private int passwordExpiryDays;
    private int passwordHistoryCount;
    private Set<String> commonPasswords;
    private Set<String> specialCharacters;
    
    public PasswordPolicy() {
        setDefaultPolicy();
        initializeCommonPasswords();
        initializeSpecialCharacters();
    }
    
    private void setDefaultPolicy() {
        this.minLength = 8;
        this.maxLength = 128;
        this.requireUppercase = true;
        this.requireLowercase = true;
        this.requireNumbers = true;
        this.requireSpecialChars = true;
        this.passwordExpiryDays = 90;
        this.passwordHistoryCount = 5;
    }
    
    private void initializeCommonPasswords() {
        this.commonPasswords = new HashSet<>(Arrays.asList(
            "password", "123456", "password123", "admin", "qwerty", "letmein",
            "welcome", "monkey", "1234567890", "abc123", "111111", "123123",
            "password1", "1234", "12345", "dragon", "master", "hello",
            "login", "welcome123", "admin123", "root", "toor", "pass",
            "test", "guest", "info", "adm", "mysql", "user", "administrator",
            "oracle", "ftp", "pi", "puppet", "ansible", "ec2-user", "vagrant",
            "azureuser", "demo", "ubuntu", "debian", "fedora", "centos"
        ));
    }
    
    private void initializeSpecialCharacters() {
        this.specialCharacters = new HashSet<>(Arrays.asList(
            "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "_", "=", "+",
            "[", "]", "{", "}", "\\", "|", ";", ":", "'", "\"", ",", ".", "<", ">",
            "/", "?", "~", "`"
        ));
    }
    
    public PasswordValidationResult validatePassword(String password) {
        PasswordValidationResult result = new PasswordValidationResult();
        
        if (password == null || password.isEmpty()) {
            result.addError("Password cannot be empty");
            return result;
        }
        
        validateLength(password, result);
        validateCharacterRequirements(password, result);
        validateCommonPasswords(password, result);
        validateSequentialCharacters(password, result);
        validateRepeatingCharacters(password, result);
        
        result.setStrengthScore(calculateStrengthScore(password));
        result.setStrengthLevel(determineStrengthLevel(result.getStrengthScore()));
        
        return result;
    }
    
    private void validateLength(String password, PasswordValidationResult result) {
        if (password.length() < minLength) {
            result.addError(String.format("Password must be at least %d characters long", minLength));
        }
        if (password.length() > maxLength) {
            result.addError(String.format("Password must not exceed %d characters", maxLength));
        }
    }
    
    private void validateCharacterRequirements(String password, PasswordValidationResult result) {
        if (requireUppercase && !Pattern.compile("[A-Z]").matcher(password).find()) {
            result.addError("Password must contain at least one uppercase letter");
        }
        
        if (requireLowercase && !Pattern.compile("[a-z]").matcher(password).find()) {
            result.addError("Password must contain at least one lowercase letter");
        }
        
        if (requireNumbers && !Pattern.compile("[0-9]").matcher(password).find()) {
            result.addError("Password must contain at least one number");
        }
        
        if (requireSpecialChars && !containsSpecialCharacter(password)) {
            result.addError("Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)");
        }
    }
    
    private boolean containsSpecialCharacter(String password) {
        for (char c : password.toCharArray()) {
            if (specialCharacters.contains(String.valueOf(c))) {
                return true;
            }
        }
        return false;
    }
    
    private void validateCommonPasswords(String password, PasswordValidationResult result) {
        String lowerPassword = password.toLowerCase();
        if (commonPasswords.contains(lowerPassword)) {
            result.addError("Password is too common and easily guessable");
        }
        
        for (String common : commonPasswords) {
            if (lowerPassword.contains(common) && common.length() >= 4) {
                result.addError("Password contains common words or patterns");
                break;
            }
        }
    }
    
    private void validateSequentialCharacters(String password, PasswordValidationResult result) {
        String sequences = "abcdefghijklmnopqrstuvwxyz0123456789";
        String reverseSequences = new StringBuilder(sequences).reverse().toString();
        
        for (int i = 0; i <= sequences.length() - 4; i++) {
            String seq = sequences.substring(i, i + 4);
            String revSeq = reverseSequences.substring(i, i + 4);
            
            if (password.toLowerCase().contains(seq) || password.toLowerCase().contains(revSeq)) {
                result.addWarning("Password contains sequential characters");
                break;
            }
        }
    }
    
    private void validateRepeatingCharacters(String password, PasswordValidationResult result) {
        int maxRepeating = 0;
        int currentRepeating = 1;
        
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) == password.charAt(i - 1)) {
                currentRepeating++;
            } else {
                maxRepeating = Math.max(maxRepeating, currentRepeating);
                currentRepeating = 1;
            }
        }
        maxRepeating = Math.max(maxRepeating, currentRepeating);
        
        if (maxRepeating >= 3) {
            result.addWarning("Password contains too many repeating characters");
        }
    }
    
    private int calculateStrengthScore(String password) {
        int score = 0;
        
        score += Math.min(password.length() * 2, 50);
        
        if (Pattern.compile("[a-z]").matcher(password).find()) score += 5;
        if (Pattern.compile("[A-Z]").matcher(password).find()) score += 5;
        if (Pattern.compile("[0-9]").matcher(password).find()) score += 5;
        if (containsSpecialCharacter(password)) score += 10;
        
        long uniqueChars = password.chars().distinct().count();
        score += (int) (uniqueChars * 2);
        
        if (password.length() >= 12) score += 10;
        if (password.length() >= 16) score += 10;
        
        return Math.min(score, 100);
    }
    
    private PasswordStrengthLevel determineStrengthLevel(int score) {
        if (score < 30) return PasswordStrengthLevel.VERY_WEAK;
        if (score < 50) return PasswordStrengthLevel.WEAK;
        if (score < 70) return PasswordStrengthLevel.MODERATE;
        if (score < 85) return PasswordStrengthLevel.STRONG;
        return PasswordStrengthLevel.VERY_STRONG;
    }
    
    public String generatePasswordRequirementsText() {
        StringBuilder requirements = new StringBuilder();
        requirements.append("Password Requirements:\n");
        requirements.append(String.format("• Length: %d-%d characters\n", minLength, maxLength));
        
        if (requireUppercase) requirements.append("• At least one uppercase letter (A-Z)\n");
        if (requireLowercase) requirements.append("• At least one lowercase letter (a-z)\n");
        if (requireNumbers) requirements.append("• At least one number (0-9)\n");
        if (requireSpecialChars) requirements.append("• At least one special character (!@#$%^&*)\n");
        
        requirements.append("• Cannot be a common password\n");
        requirements.append("• Cannot contain sequential characters (abc, 123)\n");
        requirements.append("• Cannot contain excessive repeating characters\n");
        
        return requirements.toString();
    }
    
    public int getMinLength() { return minLength; }
    public void setMinLength(int minLength) { this.minLength = minLength; }
    
    public int getMaxLength() { return maxLength; }
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
    
    public boolean isRequireUppercase() { return requireUppercase; }
    public void setRequireUppercase(boolean requireUppercase) { this.requireUppercase = requireUppercase; }
    
    public boolean isRequireLowercase() { return requireLowercase; }
    public void setRequireLowercase(boolean requireLowercase) { this.requireLowercase = requireLowercase; }
    
    public boolean isRequireNumbers() { return requireNumbers; }
    public void setRequireNumbers(boolean requireNumbers) { this.requireNumbers = requireNumbers; }
    
    public boolean isRequireSpecialChars() { return requireSpecialChars; }
    public void setRequireSpecialChars(boolean requireSpecialChars) { this.requireSpecialChars = requireSpecialChars; }
    
    public int getPasswordExpiryDays() { return passwordExpiryDays; }
    public void setPasswordExpiryDays(int passwordExpiryDays) { this.passwordExpiryDays = passwordExpiryDays; }
    
    public int getPasswordHistoryCount() { return passwordHistoryCount; }
    public void setPasswordHistoryCount(int passwordHistoryCount) { this.passwordHistoryCount = passwordHistoryCount; }
}
