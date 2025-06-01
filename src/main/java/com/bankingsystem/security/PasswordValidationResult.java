package com.bankingsystem.security;

import java.util.ArrayList;
import java.util.List;

public class PasswordValidationResult {
    private List<String> errors;
    private List<String> warnings;
    private int strengthScore;
    private PasswordStrengthLevel strengthLevel;
    private boolean isValid;
    
    public PasswordValidationResult() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.strengthScore = 0;
        this.strengthLevel = PasswordStrengthLevel.VERY_WEAK;
        this.isValid = true;
    }
    
    public void addError(String error) {
        this.errors.add(error);
        this.isValid = false;
    }
    
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
    
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    public int getStrengthScore() {
        return strengthScore;
    }
    
    public void setStrengthScore(int strengthScore) {
        this.strengthScore = strengthScore;
    }
    
    public PasswordStrengthLevel getStrengthLevel() {
        return strengthLevel;
    }
    
    public void setStrengthLevel(PasswordStrengthLevel strengthLevel) {
        this.strengthLevel = strengthLevel;
    }
    
    public boolean isValid() {
        return isValid && !hasErrors();
    }
    
    public String getErrorsAsString() {
        if (errors.isEmpty()) {
            return "";
        }
        return String.join("\n", errors);
    }
    
    public String getWarningsAsString() {
        if (warnings.isEmpty()) {
            return "";
        }
        return String.join("\n", warnings);
    }
    
    public String getAllMessagesAsString() {
        List<String> allMessages = new ArrayList<>();
        
        if (!errors.isEmpty()) {
            allMessages.add("Errors:");
            allMessages.addAll(errors);
        }
        
        if (!warnings.isEmpty()) {
            if (!allMessages.isEmpty()) {
                allMessages.add("");
            }
            allMessages.add("Warnings:");
            allMessages.addAll(warnings);
        }
        
        return String.join("\n", allMessages);
    }
    
    public String getStrengthDescription() {
        return strengthLevel.getDescription();
    }
    
    public String getStrengthColor() {
        return strengthLevel.getColor();
    }
    
    @Override
    public String toString() {
        return String.format("PasswordValidationResult{valid=%s, score=%d, level=%s, errors=%d, warnings=%d}",
                           isValid, strengthScore, strengthLevel, errors.size(), warnings.size());
    }
}
