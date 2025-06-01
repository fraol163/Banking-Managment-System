package com.bankingsystem.utils;

import com.bankingsystem.models.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Utility class for validating and synchronizing report data with actual database state
 */
public class ReportDataValidator {
    private static final Logger LOGGER = Logger.getLogger(ReportDataValidator.class.getName());
    
    /**
     * Validate that account summary report data matches actual account balances
     */
    public static boolean validateAccountSummaryData(List<AccountSummaryReport> reports) {
        boolean allValid = true;
        
        for (AccountSummaryReport report : reports) {
            try {
                // Get actual account from database
                AbstractAccount actualAccount = MockDatabaseUtil.findAccountByNumber(report.getAccountNumber());
                
                if (actualAccount == null) {
                    LOGGER.warning("Account not found in database: " + report.getAccountNumber());
                    allValid = false;
                    continue;
                }
                
                // Validate current balance
                if (report.getCurrentBalance().compareTo(actualAccount.getBalance()) != 0) {
                    LOGGER.warning(String.format("Balance mismatch for account %s: Report=%.2f, Actual=%.2f",
                        report.getAccountNumber(), report.getCurrentBalance(), actualAccount.getBalance()));
                    allValid = false;
                }
                
                // Validate customer information
                Customer customer = MockDatabaseUtil.findCustomerById(actualAccount.getCustomerId());
                if (customer != null) {
                    String expectedCustomerName = customer.getFirstName() + " " + customer.getLastName();
                    if (!expectedCustomerName.equals(report.getCustomerName())) {
                        LOGGER.warning(String.format("Customer name mismatch for account %s: Report='%s', Actual='%s'",
                            report.getAccountNumber(), report.getCustomerName(), expectedCustomerName));
                        allValid = false;
                    }
                }
                
                // Validate account type
                String expectedAccountType = actualAccount.getClass().getSimpleName().replace("Account", "");
                if (!expectedAccountType.equals(report.getAccountType())) {
                    LOGGER.warning(String.format("Account type mismatch for account %s: Report='%s', Actual='%s'",
                        report.getAccountNumber(), report.getAccountType(), expectedAccountType));
                    allValid = false;
                }
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error validating account summary data for " + report.getAccountNumber(), e);
                allValid = false;
            }
        }
        
        return allValid;
    }
    
    /**
     * Validate transaction analysis report calculations
     */
    public static boolean validateTransactionAnalysisData(List<TransactionAnalysisReport> reports, 
                                                         LocalDate startDate, LocalDate endDate) {
        boolean allValid = true;
        
        try {
            // Get all transactions in the date range
            List<Transaction> allTransactions = MockDatabaseUtil.getAllTransactions().stream()
                .filter(t -> !t.getCreatedDate().toLocalDate().isBefore(startDate) &&
                           !t.getCreatedDate().toLocalDate().isAfter(endDate))
                .toList();
            
            for (TransactionAnalysisReport report : reports) {
                // Filter transactions by type
                List<Transaction> typeTransactions = allTransactions.stream()
                    .filter(t -> t.getTransactionType().equals(report.getTransactionType()))
                    .toList();
                
                // Validate transaction count
                if (report.getTransactionCount() != typeTransactions.size()) {
                    LOGGER.warning(String.format("Transaction count mismatch for type %s: Report=%d, Actual=%d",
                        report.getTransactionType(), report.getTransactionCount(), typeTransactions.size()));
                    allValid = false;
                }
                
                // Validate total amount
                BigDecimal actualTotal = typeTransactions.stream()
                    .map(t -> t.getAmount().abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                if (report.getTotalAmount().compareTo(actualTotal) != 0) {
                    LOGGER.warning(String.format("Total amount mismatch for type %s: Report=%.2f, Actual=%.2f",
                        report.getTransactionType(), report.getTotalAmount(), actualTotal));
                    allValid = false;
                }
                
                // Validate average amount
                if (typeTransactions.size() > 0) {
                    BigDecimal expectedAverage = actualTotal.divide(BigDecimal.valueOf(typeTransactions.size()), 2, BigDecimal.ROUND_HALF_UP);
                    if (report.getAverageAmount().compareTo(expectedAverage) != 0) {
                        LOGGER.warning(String.format("Average amount mismatch for type %s: Report=%.2f, Actual=%.2f",
                            report.getTransactionType(), report.getAverageAmount(), expectedAverage));
                        allValid = false;
                    }
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error validating transaction analysis data", e);
            allValid = false;
        }
        
        return allValid;
    }
    
    /**
     * Validate user activity report data
     */
    public static boolean validateUserActivityData(List<UserActivityReport> reports, 
                                                  LocalDate startDate, LocalDate endDate) {
        boolean allValid = true;
        
        try {
            List<Transaction> allTransactions = MockDatabaseUtil.getAllTransactions().stream()
                .filter(t -> !t.getCreatedDate().toLocalDate().isBefore(startDate) &&
                           !t.getCreatedDate().toLocalDate().isAfter(endDate))
                .toList();
            
            for (UserActivityReport report : reports) {
                // Get actual user from database
                User actualUser = MockDatabaseUtil.findUserById(report.getUserId());
                
                if (actualUser == null) {
                    LOGGER.warning("User not found in database: " + report.getUserId());
                    allValid = false;
                    continue;
                }
                
                // Validate username and role
                if (!actualUser.getUsername().equals(report.getUsername())) {
                    LOGGER.warning(String.format("Username mismatch for user %d: Report='%s', Actual='%s'",
                        report.getUserId(), report.getUsername(), actualUser.getUsername()));
                    allValid = false;
                }
                
                if (!actualUser.getRole().equals(report.getRole())) {
                    LOGGER.warning(String.format("Role mismatch for user %d: Report='%s', Actual='%s'",
                        report.getUserId(), report.getRole(), actualUser.getRole()));
                    allValid = false;
                }
                
                // Validate transaction count
                long actualTransactionCount = allTransactions.stream()
                    .filter(t -> report.getUserId().equals(t.getCreatedBy()))
                    .count();
                
                if (report.getTransactionCount() != actualTransactionCount) {
                    LOGGER.warning(String.format("Transaction count mismatch for user %d: Report=%d, Actual=%d",
                        report.getUserId(), report.getTransactionCount(), actualTransactionCount));
                    allValid = false;
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error validating user activity data", e);
            allValid = false;
        }
        
        return allValid;
    }
    
    /**
     * Synchronize report data with current database state
     */
    public static void synchronizeReportData() {
        try {
            LOGGER.info("Starting report data synchronization...");
            
            // Force refresh of all cached data
            MockDatabaseUtil.refreshData();
            
            // Validate data integrity
            List<AbstractAccount> accounts = MockDatabaseUtil.getAllAccounts();
            List<Transaction> transactions = MockDatabaseUtil.getAllTransactions();
            List<User> users = MockDatabaseUtil.getAllUsers();
            
            LOGGER.info(String.format("Data synchronization complete: %d accounts, %d transactions, %d users",
                accounts.size(), transactions.size(), users.size()));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during report data synchronization", e);
        }
    }
    
    /**
     * Check if reports need to be refreshed based on recent data changes
     */
    public static boolean needsRefresh(LocalDateTime lastReportGeneration) {
        try {
            // Check if any transactions were created after last report generation
            List<Transaction> recentTransactions = MockDatabaseUtil.getAllTransactions().stream()
                .filter(t -> t.getCreatedDate().isAfter(lastReportGeneration))
                .toList();
            
            if (!recentTransactions.isEmpty()) {
                LOGGER.info(String.format("Found %d transactions created after last report generation", 
                    recentTransactions.size()));
                return true;
            }
            
            // Check if any accounts were created or modified after last report generation
            List<AbstractAccount> recentAccounts = MockDatabaseUtil.getAllAccounts().stream()
                .filter(a -> a.getCreatedDate() != null && a.getCreatedDate().isAfter(lastReportGeneration))
                .toList();
            
            if (!recentAccounts.isEmpty()) {
                LOGGER.info(String.format("Found %d accounts created after last report generation", 
                    recentAccounts.size()));
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking if reports need refresh", e);
            return true; // Default to refresh on error
        }
    }
    
    /**
     * Get data freshness status for reports
     */
    public static String getDataFreshnessStatus() {
        try {
            List<Transaction> allTransactions = MockDatabaseUtil.getAllTransactions();
            List<AbstractAccount> allAccounts = MockDatabaseUtil.getAllAccounts();
            
            if (allTransactions.isEmpty() && allAccounts.isEmpty()) {
                return "No data available - Load sample data or create accounts/transactions";
            }
            
            LocalDateTime latestTransaction = allTransactions.stream()
                .map(Transaction::getCreatedDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);
            
            LocalDateTime latestAccount = allAccounts.stream()
                .map(AbstractAccount::getCreatedDate)
                .filter(date -> date != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);
            
            LocalDateTime latestActivity = null;
            if (latestTransaction != null && latestAccount != null) {
                latestActivity = latestTransaction.isAfter(latestAccount) ? latestTransaction : latestAccount;
            } else if (latestTransaction != null) {
                latestActivity = latestTransaction;
            } else if (latestAccount != null) {
                latestActivity = latestAccount;
            }
            
            if (latestActivity != null) {
                return String.format("Data current as of: %s (%d transactions, %d accounts)",
                    latestActivity.toString(), allTransactions.size(), allAccounts.size());
            } else {
                return "Data status unknown";
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting data freshness status", e);
            return "Error checking data status";
        }
    }
}
