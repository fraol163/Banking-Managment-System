package com.bankingsystem.security;

public enum PasswordStrengthLevel {
    VERY_WEAK("Very Weak", "#FF0000", "Password is very weak and easily compromised"),
    WEAK("Weak", "#FF6600", "Password is weak and should be strengthened"),
    MODERATE("Moderate", "#FFAA00", "Password has moderate strength but could be improved"),
    STRONG("Strong", "#66AA00", "Password is strong and meets security requirements"),
    VERY_STRONG("Very Strong", "#00AA00", "Password is very strong and highly secure");
    
    private final String description;
    private final String color;
    private final String advice;
    
    PasswordStrengthLevel(String description, String color, String advice) {
        this.description = description;
        this.color = color;
        this.advice = advice;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getColor() {
        return color;
    }
    
    public String getAdvice() {
        return advice;
    }
    
    public boolean isAcceptable() {
        return this.ordinal() >= MODERATE.ordinal();
    }
    
    public boolean isStrong() {
        return this.ordinal() >= STRONG.ordinal();
    }
    
    @Override
    public String toString() {
        return description;
    }
}
