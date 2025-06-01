package com.bankingsystem.dao;

import com.bankingsystem.models.AbstractAccount;
import com.bankingsystem.utils.MockDatabaseUtil;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AccountDAO {
    private static final Logger LOGGER = Logger.getLogger(AccountDAO.class.getName());

    public AbstractAccount findById(Integer accountId) throws SQLException {
        try {
            return MockDatabaseUtil.findAccountById(accountId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find account by ID: " + accountId, e);
            throw new SQLException("Database error", e);
        }
    }

    public AbstractAccount findByAccountNumber(String accountNumber) throws SQLException {
        try {
            return MockDatabaseUtil.findAccountByNumber(accountNumber);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find account by number: " + accountNumber, e);
            throw new SQLException("Database error", e);
        }
    }

    public List<AbstractAccount> findByCustomerId(Integer customerId) throws SQLException {
        try {
            return MockDatabaseUtil.getAccountsByCustomerId(customerId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find accounts by customer ID: " + customerId, e);
            throw new SQLException("Database error", e);
        }
    }

    public List<AbstractAccount> findAll() throws SQLException {
        try {
            return MockDatabaseUtil.getAllAccounts();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve all accounts", e);
            throw new SQLException("Database error", e);
        }
    }

    public AbstractAccount save(AbstractAccount account) throws SQLException {
        try {
            return MockDatabaseUtil.saveAccount(account);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save account", e);
            throw new SQLException("Database error", e);
        }
    }

    public boolean updateBalance(Integer accountId, BigDecimal newBalance) throws SQLException {
        try {
            return MockDatabaseUtil.updateAccountBalance(accountId, newBalance);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update account balance: " + accountId, e);
            throw new SQLException("Database error", e);
        }
    }

    public boolean delete(Integer accountId) throws SQLException {
        try {
            AbstractAccount account = MockDatabaseUtil.findAccountById(accountId);
            if (account != null) {
                account.setStatus("Closed");
                MockDatabaseUtil.saveAccount(account);
                LOGGER.info("Account closed successfully: " + accountId);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to close account: " + accountId, e);
            throw new SQLException("Database error", e);
        }
    }

    public boolean deletePermanently(Integer accountId) throws SQLException {
        try {
            AbstractAccount account = MockDatabaseUtil.findAccountById(accountId);
            if (account != null) {
                String accountNumber = account.getAccountNumber();
                boolean success = MockDatabaseUtil.deleteAccountPermanently(accountId);
                if (success) {
                    LOGGER.warning("Account permanently deleted: " + accountNumber + " (ID: " + accountId + ")");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to permanently delete account: " + accountId, e);
            throw new SQLException("Database error", e);
        }
    }

    public boolean canDeleteAccount(Integer accountId) throws SQLException {
        try {
            AbstractAccount account = MockDatabaseUtil.findAccountById(accountId);
            if (account == null) {
                LOGGER.warning("Cannot delete account - account not found: " + accountId);
                return false;
            }

            boolean canDelete = account.getBalance().compareTo(java.math.BigDecimal.ZERO) == 0;
            if (!canDelete) {
                LOGGER.info("Cannot delete account " + accountId + " - non-zero balance: $" + account.getBalance());
            } else {
                LOGGER.info("Account " + accountId + " can be deleted - zero balance confirmed");
            }
            return canDelete;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to check if account can be deleted: " + accountId, e);
            throw new SQLException("Database error", e);
        }
    }

    public boolean hasTransactions(Integer accountId) throws SQLException {
        return !MockDatabaseUtil.getTransactionsByAccountId(accountId).isEmpty();
    }
}
