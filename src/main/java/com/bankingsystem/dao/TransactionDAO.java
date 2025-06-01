package com.bankingsystem.dao;

import com.bankingsystem.models.Transaction;
import com.bankingsystem.utils.MockDatabaseUtil;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TransactionDAO {
    private static final Logger LOGGER = Logger.getLogger(TransactionDAO.class.getName());

    public Transaction findById(Integer transactionId) throws SQLException {
        return null;
    }

    public Transaction findByReferenceNumber(String referenceNumber) throws SQLException {
        try {
            return MockDatabaseUtil.findTransactionByReference(referenceNumber);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find transaction by reference: " + referenceNumber, e);
            throw new SQLException("Database error", e);
        }
    }

    public List<Transaction> findByAccountId(Integer accountId) throws SQLException {
        try {
            return MockDatabaseUtil.getTransactionsByAccountId(accountId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find transactions by account ID: " + accountId, e);
            throw new SQLException("Database error", e);
        }
    }

    public List<Transaction> findByAccountIdAndDateRange(Integer accountId, LocalDate startDate, LocalDate endDate) throws SQLException {
        try {
            return MockDatabaseUtil.getTransactionsByAccountId(accountId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find transactions by date range", e);
            throw new SQLException("Database error", e);
        }
    }

    public BigDecimal getDailyWithdrawalTotal(Integer accountId, LocalDate date) throws SQLException {
        try {
            return MockDatabaseUtil.getDailyWithdrawalTotal(accountId, date);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get daily withdrawal total", e);
            throw new SQLException("Database error", e);
        }
    }

    public List<Transaction> findAll() throws SQLException {
        try {
            return MockDatabaseUtil.getAllTransactions();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve all transactions", e);
            throw new SQLException("Database error", e);
        }
    }

    public Transaction save(Transaction transaction) throws SQLException {
        try {
            return MockDatabaseUtil.saveTransaction(transaction);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save transaction", e);
            throw new SQLException("Database error", e);
        }
    }

    public Integer getTransactionTypeId(String typeName) throws SQLException {
        try {
            return MockDatabaseUtil.getTransactionTypeId(typeName);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to get transaction type ID: " + typeName, e);
            throw new SQLException("Database error", e);
        }
    }

    public boolean delete(Integer transactionId) throws SQLException {
        try {
            Transaction transaction = MockDatabaseUtil.findTransactionById(transactionId);
            if (transaction != null) {
                transaction.setStatus("Cancelled");
                MockDatabaseUtil.saveTransaction(transaction);
                LOGGER.info("Transaction cancelled successfully: " + transactionId);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to cancel transaction: " + transactionId, e);
            throw new SQLException("Database error", e);
        }
    }

    public boolean deletePermanently(Integer transactionId) throws SQLException {
        try {
            Transaction transaction = MockDatabaseUtil.findTransactionById(transactionId);
            if (transaction != null) {
                String referenceNumber = transaction.getReferenceNumber();
                boolean success = MockDatabaseUtil.deleteTransactionPermanently(transactionId);
                if (success) {
                    LOGGER.warning("Transaction permanently deleted: " + referenceNumber + " (ID: " + transactionId + ")");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to permanently delete transaction: " + transactionId, e);
            throw new SQLException("Database error", e);
        }
    }

    public boolean canDeleteTransaction(Integer transactionId) throws SQLException {
        Transaction transaction = MockDatabaseUtil.findTransactionById(transactionId);
        if (transaction == null) {
            return false;
        }

        return !"Completed".equals(transaction.getStatus()) ||
               java.time.LocalDateTime.now().minusHours(24).isBefore(transaction.getCreatedDate());
    }
}
