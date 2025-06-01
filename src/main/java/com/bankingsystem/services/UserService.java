package com.bankingsystem.services;

import com.bankingsystem.config.AppConfig;
import com.bankingsystem.dao.UserDAO;
import com.bankingsystem.models.User;
import com.bankingsystem.security.LoginAttemptTracker;
import com.bankingsystem.security.PasswordValidator;
import com.bankingsystem.security.PasswordValidationResult;
import com.bankingsystem.utils.EncryptionUtil;
import com.bankingsystem.utils.ValidationUtil;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class UserService {
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());
    private final UserDAO userDAO;
    private User currentUser;
    private LocalDateTime sessionStartTime;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public User authenticate(String username, String password) throws SQLException {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            LOGGER.warning("Authentication failed: Empty username or password");
            return null;
        }

        String trimmedUsername = username.trim();

        if (LoginAttemptTracker.isAccountLocked(trimmedUsername)) {
            String lockoutMessage = LoginAttemptTracker.getLockoutMessage(trimmedUsername);
            LOGGER.warning("Authentication failed: Account locked - " + trimmedUsername + " - " + lockoutMessage);
            return null;
        }

        User user = userDAO.findByUsername(trimmedUsername);
        if (user == null) {
            LoginAttemptTracker.recordFailedAttempt(trimmedUsername);
            LOGGER.warning("Authentication failed: User not found - " + trimmedUsername);
            return null;
        }

        if (!user.isActive()) {
            LoginAttemptTracker.recordFailedAttempt(trimmedUsername);
            LOGGER.warning("Authentication failed: User account is inactive - " + trimmedUsername);
            return null;
        }

        if (user.isAccountLocked()) {
            LOGGER.warning("Authentication failed: User account is locked in database - " + trimmedUsername);
            return null;
        }

        if (user.isPasswordExpired()) {
            LOGGER.warning("Authentication failed: Password expired - " + trimmedUsername);
            return null;
        }

        if (!EncryptionUtil.verifyPassword(password, user.getPasswordHash())) {
            LoginAttemptTracker.recordFailedAttempt(trimmedUsername);
            LOGGER.warning("Authentication failed: Invalid password - " + trimmedUsername);
            return null;
        }

        LoginAttemptTracker.recordSuccessfulLogin(trimmedUsername);
        userDAO.updateLastLogin(user.getUserId());
        this.currentUser = user;
        this.sessionStartTime = LocalDateTime.now();

        LOGGER.info("User authenticated successfully: " + trimmedUsername);
        return user;
    }

    public void logout() {
        if (currentUser != null) {
            LOGGER.info("User logged out: " + currentUser.getUsername());
            this.currentUser = null;
            this.sessionStartTime = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isSessionValid() {
        if (currentUser == null || sessionStartTime == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        long minutesElapsed = java.time.Duration.between(sessionStartTime, now).toMinutes();

        return minutesElapsed < 30;
    }

    public void refreshSession() {
        if (currentUser != null) {
            this.sessionStartTime = LocalDateTime.now();
        }
    }

    public User createUser(String username, String password, String role, String firstName,
                          String lastName, String email) throws SQLException {

        if (!ValidationUtil.isValidUsername(username)) {
            throw new IllegalArgumentException("Invalid username format");
        }

        if (!ValidationUtil.isValidPassword(password)) {
            throw new IllegalArgumentException("Password does not meet security requirements");
        }

        if (!ValidationUtil.isValidName(firstName)) {
            throw new IllegalArgumentException("Invalid first name format");
        }

        if (!ValidationUtil.isValidName(lastName)) {
            throw new IllegalArgumentException("Invalid last name format");
        }

        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        User existingUser = userDAO.findByUsername(username);
        if (existingUser != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        String hashedPassword = EncryptionUtil.hashPassword(password);

        User newUser = new User(username, hashedPassword, role, firstName, lastName, email);
        return userDAO.save(newUser);
    }

    public User updateUser(User user) throws SQLException {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("Invalid user data");
        }

        if (!ValidationUtil.isValidUsername(user.getUsername())) {
            throw new IllegalArgumentException("Invalid username format");
        }

        if (!ValidationUtil.isValidName(user.getFirstName())) {
            throw new IllegalArgumentException("Invalid first name format");
        }

        if (!ValidationUtil.isValidName(user.getLastName())) {
            throw new IllegalArgumentException("Invalid last name format");
        }

        if (!ValidationUtil.isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        return userDAO.save(user);
    }

    public boolean changePassword(Integer userId, String currentPassword, String newPassword) throws SQLException {
        User user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }

        if (!EncryptionUtil.verifyPassword(currentPassword, user.getPasswordHash())) {
            return false;
        }

        PasswordValidationResult validation = PasswordValidator.validatePassword(newPassword);
        if (!validation.isValid()) {
            throw new IllegalArgumentException("New password does not meet security requirements: " +
                                             validation.getErrorsAsString());
        }

        String hashedNewPassword = EncryptionUtil.hashPassword(newPassword);

        if (PasswordValidator.isPasswordInHistory(hashedNewPassword, getPasswordHistoryList(user))) {
            throw new IllegalArgumentException("Cannot reuse recent passwords. Please choose a different password.");
        }

        updatePasswordHistory(user, hashedNewPassword);
        user.setPasswordHash(hashedNewPassword);
        user.setLastPasswordChange(LocalDateTime.now());
        user.setPasswordExpiryDate(LocalDateTime.now().plusDays(AppConfig.PASSWORD_EXPIRY_DAYS));

        userDAO.save(user);
        LOGGER.info("Password changed successfully for user: " + user.getUsername());
        return true;
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    public User getUserById(Integer userId) throws SQLException {
        return userDAO.findById(userId);
    }

    public User getUserByUsername(String username) throws SQLException {
        return userDAO.findByUsername(username);
    }

    public boolean deactivateUser(Integer userId) throws SQLException {
        return userDAO.delete(userId);
    }

    public boolean deleteUserPermanently(Integer userId) throws SQLException {
        if (!hasPermission(AppConfig.PERMISSION_DELETE_USER)) {
            throw new SecurityException("Insufficient permissions to permanently delete users");
        }

        if (!userDAO.canDeleteUser(userId, getCurrentUser().getUserId())) {
            throw new IllegalArgumentException("Cannot delete the currently logged-in user or invalid user");
        }

        return userDAO.deletePermanently(userId);
    }

    public boolean canDeleteUserPermanently(Integer userId) throws SQLException {
        if (!hasPermission(AppConfig.PERMISSION_DELETE_USER)) {
            return false;
        }

        return userDAO.canDeleteUser(userId, getCurrentUser().getUserId());
    }

    public boolean hasPermission(String permission) {
        if (currentUser == null || !isSessionValid()) {
            return false;
        }

        return currentUser.hasPermission(permission);
    }

    public boolean canAccessAccount(Integer accountId) {
        if (currentUser == null || !isSessionValid()) {
            return false;
        }

        return currentUser.canAccessAccount(accountId);
    }

    public boolean canProcessTransaction(String transactionType) {
        if (currentUser == null || !isSessionValid()) {
            return false;
        }

        return currentUser.canProcessTransaction(transactionType);
    }

    // Enhanced Security Methods
    public PasswordValidationResult validatePassword(String password) {
        return PasswordValidator.validatePassword(password);
    }

    public boolean lockAccount(Integer userId, int durationMinutes) throws SQLException {
        User user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }

        user.setLockedUntil(LocalDateTime.now().plusMinutes(durationMinutes));
        userDAO.save(user);

        LOGGER.warning(String.format("Account locked for %d minutes: %s", durationMinutes, user.getUsername()));
        return true;
    }

    public boolean unlockAccount(Integer userId) throws SQLException {
        User user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }

        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userDAO.save(user);

        LoginAttemptTracker.manualUnlock(user.getUsername(), getCurrentUser().getUsername());
        LOGGER.info(String.format("Account unlocked by %s: %s", getCurrentUser().getUsername(), user.getUsername()));
        return true;
    }

    public void trackLoginAttempt(String username, boolean success) {
        if (success) {
            LoginAttemptTracker.recordSuccessfulLogin(username);
        } else {
            LoginAttemptTracker.recordFailedAttempt(username);
        }
    }

    public void resetLoginAttempts(String username) {
        LoginAttemptTracker.resetFailedAttempts(username);
    }

    public boolean isAccountLocked(String username) {
        return LoginAttemptTracker.isAccountLocked(username);
    }

    public String getLockoutMessage(String username) {
        return LoginAttemptTracker.getLockoutMessage(username);
    }

    public boolean requiresCaptcha(String username) {
        return LoginAttemptTracker.requiresCaptcha(username);
    }

    public long getProgressiveDelay(String username) {
        return LoginAttemptTracker.getProgressiveDelaySeconds(username);
    }

    public boolean isPasswordExpired(Integer userId) throws SQLException {
        User user = userDAO.findById(userId);
        return user != null && user.isPasswordExpired();
    }

    public boolean shouldWarnAboutPasswordExpiry(Integer userId) throws SQLException {
        User user = userDAO.findById(userId);
        return user != null && PasswordValidator.shouldWarnAboutExpiry(user.getLastPasswordChange());
    }

    public String getPasswordExpiryWarning(Integer userId) throws SQLException {
        User user = userDAO.findById(userId);
        if (user == null) {
            return "";
        }
        return PasswordValidator.getExpiryWarningMessage(user.getLastPasswordChange());
    }

    private List<String> getPasswordHistoryList(User user) {
        String history = user.getPasswordHistory();
        if (history == null || history.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return java.util.Arrays.asList(history.split(","));
    }

    private void updatePasswordHistory(User user, String newPasswordHash) {
        List<String> history = getPasswordHistoryList(user);
        history.add(0, newPasswordHash);

        while (history.size() > AppConfig.PASSWORD_HISTORY_COUNT) {
            history.remove(history.size() - 1);
        }

        user.setPasswordHistory(String.join(",", history));
    }
}
