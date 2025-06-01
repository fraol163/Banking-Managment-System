package com.bankingsystem.security;

import com.bankingsystem.config.AppConfig;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class LoginAttemptTracker {
    private static final Logger LOGGER = Logger.getLogger(LoginAttemptTracker.class.getName());
    
    private static final ConcurrentMap<String, LoginAttemptInfo> attemptCache = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, LocalDateTime> lockoutCache = new ConcurrentHashMap<>();
    
    public static void recordFailedAttempt(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        
        String key = username.toLowerCase().trim();
        LocalDateTime now = LocalDateTime.now();
        
        LoginAttemptInfo info = attemptCache.computeIfAbsent(key, k -> new LoginAttemptInfo());
        
        synchronized (info) {
            if (info.getLastAttempt() != null && 
                ChronoUnit.MINUTES.between(info.getLastAttempt(), now) > AppConfig.LOGIN_ATTEMPT_RESET_MINUTES) {
                info.reset();
            }
            
            info.incrementFailedAttempts();
            info.setLastAttempt(now);
            
            if (info.getFailedAttempts() >= AppConfig.MAX_LOGIN_ATTEMPTS) {
                lockAccount(key, now);
                LOGGER.warning(String.format("Account locked due to %d failed login attempts: %s", 
                             info.getFailedAttempts(), username));
            } else {
                LOGGER.warning(String.format("Failed login attempt %d/%d for user: %s", 
                             info.getFailedAttempts(), AppConfig.MAX_LOGIN_ATTEMPTS, username));
            }
        }
    }
    
    public static void recordSuccessfulLogin(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        
        String key = username.toLowerCase().trim();
        
        attemptCache.remove(key);
        lockoutCache.remove(key);
        
        LOGGER.info(String.format("Successful login recorded for user: %s", username));
    }
    
    public static boolean isAccountLocked(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String key = username.toLowerCase().trim();
        LocalDateTime lockTime = lockoutCache.get(key);
        
        if (lockTime == null) {
            return false;
        }
        
        LocalDateTime unlockTime = lockTime.plusMinutes(AppConfig.ACCOUNT_LOCKOUT_DURATION_MINUTES);
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isAfter(unlockTime)) {
            unlockAccount(key);
            return false;
        }
        
        return true;
    }
    
    public static long getLockoutRemainingMinutes(String username) {
        if (username == null || username.trim().isEmpty()) {
            return 0;
        }
        
        String key = username.toLowerCase().trim();
        LocalDateTime lockTime = lockoutCache.get(key);
        
        if (lockTime == null) {
            return 0;
        }
        
        LocalDateTime unlockTime = lockTime.plusMinutes(AppConfig.ACCOUNT_LOCKOUT_DURATION_MINUTES);
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isAfter(unlockTime)) {
            return 0;
        }
        
        return ChronoUnit.MINUTES.between(now, unlockTime);
    }
    
    public static int getFailedAttemptCount(String username) {
        if (username == null || username.trim().isEmpty()) {
            return 0;
        }
        
        String key = username.toLowerCase().trim();
        LoginAttemptInfo info = attemptCache.get(key);
        
        if (info == null) {
            return 0;
        }
        
        synchronized (info) {
            LocalDateTime now = LocalDateTime.now();
            if (info.getLastAttempt() != null && 
                ChronoUnit.MINUTES.between(info.getLastAttempt(), now) > AppConfig.LOGIN_ATTEMPT_RESET_MINUTES) {
                info.reset();
                return 0;
            }
            
            return info.getFailedAttempts();
        }
    }
    
    public static long getProgressiveDelaySeconds(String username) {
        int attempts = getFailedAttemptCount(username);
        
        if (attempts <= 1) {
            return 0;
        }
        
        return Math.min((long) Math.pow(2, attempts - 2), AppConfig.MAX_PROGRESSIVE_DELAY_SECONDS);
    }
    
    public static boolean requiresCaptcha(String username) {
        return getFailedAttemptCount(username) >= AppConfig.CAPTCHA_REQUIRED_AFTER_ATTEMPTS;
    }
    
    public static void manualUnlock(String username, String adminUsername) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        
        String key = username.toLowerCase().trim();
        
        attemptCache.remove(key);
        lockoutCache.remove(key);
        
        LOGGER.warning(String.format("Account manually unlocked by admin %s for user: %s", 
                     adminUsername, username));
    }
    
    public static void resetFailedAttempts(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        
        String key = username.toLowerCase().trim();
        LoginAttemptInfo info = attemptCache.get(key);
        
        if (info != null) {
            synchronized (info) {
                info.reset();
            }
        }
    }
    
    private static void lockAccount(String username, LocalDateTime lockTime) {
        lockoutCache.put(username, lockTime);
    }
    
    private static void unlockAccount(String username) {
        attemptCache.remove(username);
        lockoutCache.remove(username);
        LOGGER.info(String.format("Account automatically unlocked after timeout: %s", username));
    }
    
    public static String getLockoutMessage(String username) {
        long remainingMinutes = getLockoutRemainingMinutes(username);
        
        if (remainingMinutes <= 0) {
            return "";
        }
        
        if (remainingMinutes == 1) {
            return "Account is locked. Try again in 1 minute.";
        } else {
            return String.format("Account is locked. Try again in %d minutes.", remainingMinutes);
        }
    }
    
    public static void cleanupExpiredEntries() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(AppConfig.LOGIN_ATTEMPT_RESET_MINUTES);
        
        attemptCache.entrySet().removeIf(entry -> {
            LoginAttemptInfo info = entry.getValue();
            synchronized (info) {
                return info.getLastAttempt() != null && info.getLastAttempt().isBefore(cutoff);
            }
        });
        
        LocalDateTime lockoutCutoff = LocalDateTime.now().minusMinutes(AppConfig.ACCOUNT_LOCKOUT_DURATION_MINUTES);
        lockoutCache.entrySet().removeIf(entry -> entry.getValue().isBefore(lockoutCutoff));
    }
    
    private static class LoginAttemptInfo {
        private int failedAttempts;
        private LocalDateTime lastAttempt;
        
        public LoginAttemptInfo() {
            this.failedAttempts = 0;
            this.lastAttempt = null;
        }
        
        public void incrementFailedAttempts() {
            this.failedAttempts++;
        }
        
        public void reset() {
            this.failedAttempts = 0;
            this.lastAttempt = null;
        }
        
        public int getFailedAttempts() {
            return failedAttempts;
        }
        
        public LocalDateTime getLastAttempt() {
            return lastAttempt;
        }
        
        public void setLastAttempt(LocalDateTime lastAttempt) {
            this.lastAttempt = lastAttempt;
        }
    }
}
