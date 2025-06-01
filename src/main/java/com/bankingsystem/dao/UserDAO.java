package com.bankingsystem.dao;

import com.bankingsystem.models.User;
import com.bankingsystem.utils.MockDatabaseUtil;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class UserDAO {
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    public User findByUsername(String username) throws SQLException {
        try {
            return MockDatabaseUtil.findUserByUsername(username);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find user by username: " + username, e);
            throw new SQLException("Database error", e);
        }
    }

    public User findById(Integer userId) throws SQLException {
        try {
            return MockDatabaseUtil.findUserById(userId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find user by ID: " + userId, e);
            throw new SQLException("Database error", e);
        }
    }

    public List<User> findAll() throws SQLException {
        try {
            return MockDatabaseUtil.getAllUsers();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve all users", e);
            throw new SQLException("Database error", e);
        }
    }

    public User save(User user) throws SQLException {
        try {
            return MockDatabaseUtil.saveUser(user);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save user", e);
            throw new SQLException("Database error", e);
        }
    }

    public void updateLastLogin(Integer userId) throws SQLException {
        try {
            User user = MockDatabaseUtil.findUserById(userId);
            if (user != null) {
                user.setLastLogin(LocalDateTime.now());
                MockDatabaseUtil.saveUser(user);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to update last login for user ID: " + userId, e);
        }
    }

    public boolean delete(Integer userId) throws SQLException {
        try {
            User user = MockDatabaseUtil.findUserById(userId);
            if (user != null) {
                user.setActive(false);
                MockDatabaseUtil.saveUser(user);
                LOGGER.info("User deactivated successfully: " + userId);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to deactivate user: " + userId, e);
            throw new SQLException("Database error", e);
        }
    }

    public boolean deletePermanently(Integer userId) throws SQLException {
        try {
            User user = MockDatabaseUtil.findUserById(userId);
            if (user != null) {
                String username = user.getUsername();
                boolean success = MockDatabaseUtil.deleteUserPermanently(userId);
                if (success) {
                    LOGGER.warning("User permanently deleted: " + username + " (ID: " + userId + ")");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to permanently delete user: " + userId, e);
            throw new SQLException("Database error", e);
        }
    }

    public boolean canDeleteUser(Integer userId, Integer currentUserId) throws SQLException {
        if (userId.equals(currentUserId)) {
            return false;
        }

        User user = MockDatabaseUtil.findUserById(userId);
        if (user == null) {
            return false;
        }

        return true;
    }
}
