package com.bankingsystem.dao;

import com.bankingsystem.models.*;
import com.bankingsystem.utils.MockDatabaseUtil;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ReportDAO {
    private static final Logger LOGGER = Logger.getLogger(ReportDAO.class.getName());

    public List<UserActivityReport> generateUserActivityReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        try {
            List<UserActivityReport> reports = new ArrayList<>();
            List<User> users = MockDatabaseUtil.getAllUsers();

            for (User user : users) {
                UserActivityReport report = new UserActivityReport(user.getUserId(), user.getUsername(), user.getRole());

                if (user.getLastLogin() != null) {
                    LocalDate loginDate = user.getLastLogin().toLocalDate();
                    if (!loginDate.isBefore(startDate) && !loginDate.isAfter(endDate)) {
                        report.setLastLogin(user.getLastLogin());
                        report.incrementLoginCount();
                    }
                }

                List<Transaction> userTransactions = MockDatabaseUtil.getAllTransactions().stream()
                    .filter(t -> user.getUserId().equals(t.getCreatedBy()))
                    .filter(t -> !t.getCreatedDate().toLocalDate().isBefore(startDate) &&
                               !t.getCreatedDate().toLocalDate().isAfter(endDate))
                    .collect(Collectors.toList());

                report.setTransactionCount(userTransactions.size());

                List<AbstractAccount> userAccounts = MockDatabaseUtil.getAllAccounts().stream()
                    .filter(a -> a.getCreatedDate() != null &&
                               !a.getCreatedDate().toLocalDate().isBefore(startDate) &&
                               !a.getCreatedDate().toLocalDate().isAfter(endDate))
                    .collect(Collectors.toList());

                report.setAccountsManaged(userAccounts.size());

                String activitySummary = String.format("Processed %d transactions, managed %d accounts",
                                                     report.getTransactionCount(), report.getAccountsManaged());
                report.setActivitySummary(activitySummary);

                reports.add(report);
            }

            LOGGER.info("Generated user activity report for " + reports.size() + " users");
            return reports;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to generate user activity report", e);
            throw new SQLException("Report generation failed", e);
        }
    }

    public List<AccountSummaryReport> generateAccountSummaryReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        try {
            List<AccountSummaryReport> reports = new ArrayList<>();
            List<AbstractAccount> accounts = MockDatabaseUtil.getAllAccounts();

            LOGGER.info(String.format("Generating account summary report for %d accounts between %s and %s",
                accounts.size(), startDate, endDate));

            for (AbstractAccount account : accounts) {
                AccountSummaryReport report = new AccountSummaryReport(
                    account.getAccountId(),
                    account.getAccountNumber(),
                    account.getClass().getSimpleName().replace("Account", ""),
                    getCustomerName(account.getCustomerId())
                );

                report.setCurrentBalance(account.getBalance());
                report.setStatus(account.getStatus());
                report.setCreatedDate(account.getCreatedDate());

                List<Transaction> accountTransactions = MockDatabaseUtil.getTransactionsByAccountId(account.getAccountId())
                    .stream()
                    .filter(t -> !t.getCreatedDate().toLocalDate().isBefore(startDate) &&
                               !t.getCreatedDate().toLocalDate().isAfter(endDate))
                    .collect(Collectors.toList());

                BigDecimal totalDeposits = BigDecimal.ZERO;
                BigDecimal totalWithdrawals = BigDecimal.ZERO;
                LocalDateTime lastActivity = null;

                // Calculate transaction totals based on transaction type, not amount sign
                for (Transaction transaction : accountTransactions) {
                    String transactionType = transaction.getTransactionType();
                    BigDecimal amount = transaction.getAmount().abs(); // Always use absolute value

                    if ("Deposit".equals(transactionType) || "Transfer In".equals(transactionType)) {
                        totalDeposits = totalDeposits.add(amount);
                    } else if ("Withdrawal".equals(transactionType) || "Transfer Out".equals(transactionType)) {
                        totalWithdrawals = totalWithdrawals.add(amount);
                    }

                    if (lastActivity == null || transaction.getCreatedDate().isAfter(lastActivity)) {
                        lastActivity = transaction.getCreatedDate();
                    }
                }

                report.setTotalDeposits(totalDeposits);
                report.setTotalWithdrawals(totalWithdrawals);
                report.setTransactionCount(accountTransactions.size());
                report.setLastActivityDate(lastActivity);

                reports.add(report);
            }

            LOGGER.info("Generated account summary report for " + reports.size() + " accounts");
            return reports;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to generate account summary report", e);
            throw new SQLException("Report generation failed", e);
        }
    }

    public List<TransactionAnalysisReport> generateTransactionAnalysisReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        try {
            Map<String, TransactionAnalysisReport> reportMap = new HashMap<>();
            List<Transaction> transactions = MockDatabaseUtil.getAllTransactions().stream()
                .filter(t -> !t.getCreatedDate().toLocalDate().isBefore(startDate) &&
                           !t.getCreatedDate().toLocalDate().isAfter(endDate))
                .collect(Collectors.toList());

            for (Transaction transaction : transactions) {
                String transactionType = transaction.getTransactionType();

                TransactionAnalysisReport report = reportMap.computeIfAbsent(transactionType,
                    k -> new TransactionAnalysisReport(k));

                report.addTransaction(transaction.getAmount().abs());

                if ("Cancelled".equals(transaction.getStatus())) {
                    report.incrementTransactionsCancelled();
                } else if ("Completed".equals(transaction.getStatus())) {
                    report.incrementApprovalsCompleted();
                } else {
                    report.incrementApprovalsPending();
                }
            }

            List<TransactionAnalysisReport> reports = new ArrayList<>(reportMap.values());

            for (TransactionAnalysisReport report : reports) {
                String periodDesc = String.format("Period: %s to %s", startDate, endDate);
                report.setPeriodDescription(periodDesc);

                String processingStats = String.format("Completed: %d, Pending: %d, Cancelled: %d",
                    report.getApprovalsCompleted(), report.getApprovalsPending(), report.getTransactionsCancelled());
                report.setProcessingStatistics(processingStats);
            }

            LOGGER.info("Generated transaction analysis report for " + reports.size() + " transaction types");
            return reports;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to generate transaction analysis report", e);
            throw new SQLException("Report generation failed", e);
        }
    }

    private String getCustomerName(Integer customerId) {
        try {
            Customer customer = MockDatabaseUtil.findCustomerById(customerId);
            if (customer != null) {
                return customer.getFirstName() + " " + customer.getLastName();
            }
            return "Unknown Customer";
        } catch (Exception e) {
            return "Unknown Customer";
        }
    }

    public boolean deleteUserActivityReportData(LocalDate startDate, LocalDate endDate, boolean permanent) throws SQLException {
        try {
            List<User> users = MockDatabaseUtil.getAllUsers();
            int deletedCount = 0;

            for (User user : users) {
                if (user.getLastLogin() != null) {
                    LocalDate loginDate = user.getLastLogin().toLocalDate();
                    if (!loginDate.isBefore(startDate) && !loginDate.isAfter(endDate)) {
                        if (permanent) {
                            user.setLastLogin(null);
                            MockDatabaseUtil.saveUser(user);
                        } else {
                            user.setActive(false);
                            MockDatabaseUtil.saveUser(user);
                        }
                        deletedCount++;
                    }
                }
            }

            LOGGER.warning(String.format("User activity report data deletion: %d users affected for period %s to %s",
                         deletedCount, startDate, endDate));
            return deletedCount > 0;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to delete user activity report data", e);
            throw new SQLException("Report data deletion failed", e);
        }
    }

    public boolean deleteAccountSummaryReportData(LocalDate startDate, LocalDate endDate, boolean permanent) throws SQLException {
        try {
            List<AbstractAccount> accounts = MockDatabaseUtil.getAllAccounts();
            int deletedCount = 0;

            for (AbstractAccount account : accounts) {
                if (account.getCreatedDate() != null) {
                    LocalDate createdDate = account.getCreatedDate().toLocalDate();
                    if (!createdDate.isBefore(startDate) && !createdDate.isAfter(endDate)) {

                        if (hasActiveTransactions(account.getAccountId())) {
                            LOGGER.warning(String.format("Skipping account %s deletion due to active transactions",
                                         account.getAccountNumber()));
                            continue;
                        }

                        if (permanent) {
                            List<Transaction> accountTransactions = MockDatabaseUtil.getTransactionsByAccountId(account.getAccountId());
                            for (Transaction transaction : accountTransactions) {
                                MockDatabaseUtil.deleteTransactionPermanently(transaction.getTransactionId());
                            }
                            MockDatabaseUtil.deleteAccountPermanently(account.getAccountId());
                        } else {
                            account.setStatus("Deleted");
                            MockDatabaseUtil.saveAccount(account);
                        }
                        deletedCount++;
                    }
                }
            }

            LOGGER.warning(String.format("Account summary report data deletion: %d accounts affected for period %s to %s",
                         deletedCount, startDate, endDate));
            return deletedCount > 0;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to delete account summary report data", e);
            throw new SQLException("Report data deletion failed", e);
        }
    }

    public boolean deleteTransactionAnalysisReportData(LocalDate startDate, LocalDate endDate, boolean permanent) throws SQLException {
        try {
            List<Transaction> transactions = MockDatabaseUtil.getAllTransactions();
            int deletedCount = 0;

            for (Transaction transaction : transactions) {
                if (transaction.getCreatedDate() != null) {
                    LocalDate transactionDate = transaction.getCreatedDate().toLocalDate();
                    if (!transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate)) {
                        if (permanent) {
                            MockDatabaseUtil.deleteTransactionPermanently(transaction.getTransactionId());
                        } else {
                            transaction.setStatus("Deleted");
                            MockDatabaseUtil.saveTransaction(transaction);
                        }
                        deletedCount++;
                    }
                }
            }

            LOGGER.warning(String.format("Transaction analysis report data deletion: %d transactions affected for period %s to %s",
                         deletedCount, startDate, endDate));
            return deletedCount > 0;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to delete transaction analysis report data", e);
            throw new SQLException("Report data deletion failed", e);
        }
    }

    private boolean hasActiveTransactions(Integer accountId) {
        try {
            List<Transaction> transactions = MockDatabaseUtil.getTransactionsByAccountId(accountId);
            return transactions.stream()
                .anyMatch(t -> "Completed".equals(t.getStatus()) || "Pending".equals(t.getStatus()));
        } catch (Exception e) {
            return true;
        }
    }

    public boolean restoreUserActivityReportData(LocalDate startDate, LocalDate endDate) throws SQLException {
        try {
            List<User> users = MockDatabaseUtil.getAllUsers();
            int restoredCount = 0;

            for (User user : users) {
                if (!user.isActive() && user.getLastLogin() != null) {
                    LocalDate loginDate = user.getLastLogin().toLocalDate();
                    if (!loginDate.isBefore(startDate) && !loginDate.isAfter(endDate)) {
                        user.setActive(true);
                        MockDatabaseUtil.saveUser(user);
                        restoredCount++;
                    }
                }
            }

            LOGGER.info(String.format("User activity report data restoration: %d users restored for period %s to %s",
                       restoredCount, startDate, endDate));
            return restoredCount > 0;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to restore user activity report data", e);
            throw new SQLException("Report data restoration failed", e);
        }
    }

    public boolean restoreAccountSummaryReportData(LocalDate startDate, LocalDate endDate) throws SQLException {
        try {
            List<AbstractAccount> accounts = MockDatabaseUtil.getAllAccounts();
            int restoredCount = 0;

            for (AbstractAccount account : accounts) {
                if ("Deleted".equals(account.getStatus()) && account.getCreatedDate() != null) {
                    LocalDate createdDate = account.getCreatedDate().toLocalDate();
                    if (!createdDate.isBefore(startDate) && !createdDate.isAfter(endDate)) {
                        account.setStatus("Active");
                        MockDatabaseUtil.saveAccount(account);
                        restoredCount++;
                    }
                }
            }

            LOGGER.info(String.format("Account summary report data restoration: %d accounts restored for period %s to %s",
                       restoredCount, startDate, endDate));
            return restoredCount > 0;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to restore account summary report data", e);
            throw new SQLException("Report data restoration failed", e);
        }
    }

    public boolean restoreTransactionAnalysisReportData(LocalDate startDate, LocalDate endDate) throws SQLException {
        try {
            List<Transaction> transactions = MockDatabaseUtil.getAllTransactions();
            int restoredCount = 0;

            for (Transaction transaction : transactions) {
                if ("Deleted".equals(transaction.getStatus()) && transaction.getCreatedDate() != null) {
                    LocalDate transactionDate = transaction.getCreatedDate().toLocalDate();
                    if (!transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate)) {
                        transaction.setStatus("Completed");
                        MockDatabaseUtil.saveTransaction(transaction);
                        restoredCount++;
                    }
                }
            }

            LOGGER.info(String.format("Transaction analysis report data restoration: %d transactions restored for period %s to %s",
                       restoredCount, startDate, endDate));
            return restoredCount > 0;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to restore transaction analysis report data", e);
            throw new SQLException("Report data restoration failed", e);
        }
    }

    public boolean deleteAccountFromReports(Integer accountId, boolean permanent) throws SQLException {
        try {
            AbstractAccount account = MockDatabaseUtil.findAccountById(accountId);
            if (account == null) {
                return false;
            }

            if (hasActiveTransactionsInReports(accountId)) {
                LOGGER.warning(String.format("Cannot delete account %d - has active transactions", accountId));
                throw new SQLException("Cannot delete account with active transactions");
            }

            if (permanent) {
                List<Transaction> accountTransactions = MockDatabaseUtil.getTransactionsByAccountId(accountId);
                for (Transaction transaction : accountTransactions) {
                    MockDatabaseUtil.deleteTransactionPermanently(transaction.getTransactionId());
                }
                MockDatabaseUtil.deleteAccountPermanently(accountId);
            } else {
                account.setStatus("Deleted");
                MockDatabaseUtil.saveAccount(account);
            }

            LOGGER.warning(String.format("Account %d deleted from reports (permanent: %b)", accountId, permanent));
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to delete account from reports", e);
            throw new SQLException("Account deletion from reports failed", e);
        }
    }

    public boolean deleteAccountByNumber(String accountNumber, boolean permanent) throws SQLException {
        try {
            AbstractAccount account = MockDatabaseUtil.findAccountByNumber(accountNumber);
            if (account == null) {
                return false;
            }

            return deleteAccountFromReports(account.getAccountId(), permanent);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to delete account by number", e);
            throw new SQLException("Account deletion by number failed", e);
        }
    }

    public boolean hasActiveTransactionsInReports(Integer accountId) throws SQLException {
        try {
            List<Transaction> transactions = MockDatabaseUtil.getTransactionsByAccountId(accountId);
            return transactions.stream()
                .anyMatch(t -> "Completed".equals(t.getStatus()) || "Pending".equals(t.getStatus()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to check active transactions for account", e);
            throw new SQLException("Active transaction check failed", e);
        }
    }

    public boolean isAccountInActiveReports(Integer accountId) throws SQLException {
        try {
            AbstractAccount account = MockDatabaseUtil.findAccountById(accountId);
            if (account == null) {
                return false;
            }

            return !"Deleted".equals(account.getStatus()) && !"Closed".equals(account.getStatus());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to check if account is in active reports", e);
            throw new SQLException("Active report check failed", e);
        }
    }

    public boolean restoreAccountFromReports(Integer accountId) throws SQLException {
        try {
            AbstractAccount account = MockDatabaseUtil.findAccountById(accountId);
            if (account != null && "Deleted".equals(account.getStatus())) {
                account.setStatus("Active");
                MockDatabaseUtil.saveAccount(account);

                LOGGER.info(String.format("Account %d restored in reports", accountId));
                return true;
            }
            return false;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to restore account in reports", e);
            throw new SQLException("Account restoration in reports failed", e);
        }
    }
}
