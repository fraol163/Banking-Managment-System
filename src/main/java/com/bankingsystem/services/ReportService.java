package com.bankingsystem.services;

import com.bankingsystem.config.AppConfig;
import com.bankingsystem.dao.ReportDAO;
import com.bankingsystem.models.*;
import com.bankingsystem.utils.FileUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ReportService {
    private static final Logger LOGGER = Logger.getLogger(ReportService.class.getName());
    private ReportDAO reportDAO;
    private UserService userService;

    public ReportService(UserService userService) {
        this.reportDAO = new ReportDAO();
        this.userService = userService;
    }

    public List<UserActivityReport> generateUserActivityReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        if (!userService.hasPermission(AppConfig.PERMISSION_GENERATE_REPORTS)) {
            throw new SecurityException("Insufficient permissions to generate user activity reports");
        }

        if (!userService.getCurrentUser().isAdmin()) {
            throw new SecurityException("User activity reports are only available to Admin users");
        }

        validateDateRange(startDate, endDate);

        try {
            List<UserActivityReport> reports = reportDAO.generateUserActivityReport(startDate, endDate);
            LOGGER.info("Generated user activity report with " + reports.size() + " entries");
            return reports;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to generate user activity report", e);
            throw e;
        }
    }

    public List<AccountSummaryReport> generateAccountSummaryReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        if (!userService.hasPermission(AppConfig.PERMISSION_GENERATE_REPORTS) &&
            !userService.hasPermission(AppConfig.PERMISSION_GENERATE_CUSTOMER_REPORTS)) {
            throw new SecurityException("Insufficient permissions to generate account summary reports");
        }

        validateDateRange(startDate, endDate);

        try {
            List<AccountSummaryReport> reports = reportDAO.generateAccountSummaryReport(startDate, endDate);

            if (userService.getCurrentUser().isTeller()) {
                reports = reports.stream()
                    .filter(report -> report.getTransactionCount() > 0)
                    .collect(Collectors.toList());
            }

            LOGGER.info("Generated account summary report with " + reports.size() + " entries");
            return reports;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to generate account summary report", e);
            throw e;
        }
    }

    public List<TransactionAnalysisReport> generateTransactionAnalysisReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        if (!userService.hasPermission(AppConfig.PERMISSION_GENERATE_REPORTS) &&
            !userService.hasPermission(AppConfig.PERMISSION_GENERATE_CUSTOMER_REPORTS)) {
            throw new SecurityException("Insufficient permissions to generate transaction analysis reports");
        }

        validateDateRange(startDate, endDate);

        try {
            List<TransactionAnalysisReport> reports = reportDAO.generateTransactionAnalysisReport(startDate, endDate);
            LOGGER.info("Generated transaction analysis report with " + reports.size() + " entries");
            return reports;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to generate transaction analysis report", e);
            throw e;
        }
    }

    public String exportUserActivityReportToCSV(List<UserActivityReport> reports) throws IOException {
        if (!userService.getCurrentUser().isAdmin()) {
            throw new SecurityException("User activity report export is only available to Admin users");
        }

        StringBuilder csv = new StringBuilder();
        csv.append("User ID,Username,Role,Last Login,Login Count,Transaction Count,Accounts Managed,Users Managed,Activity Summary\n");

        for (UserActivityReport report : reports) {
            csv.append(String.format("%d,%s,%s,%s,%d,%d,%d,%d,\"%s\"\n",
                report.getUserId(),
                report.getUsername(),
                report.getRole(),
                report.getLastLogin() != null ? report.getLastLogin().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                report.getLoginCount(),
                report.getTransactionCount(),
                report.getAccountsManaged(),
                report.getUsersManaged(),
                report.getActivitySummary() != null ? report.getActivitySummary() : ""
            ));
        }

        String filename = "user_activity_report_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv";
        String filepath = FileUtil.saveReportToFile(filename, csv.toString());

        LOGGER.info("Exported user activity report to: " + filepath);
        return filepath;
    }

    public String exportAccountSummaryReportToCSV(List<AccountSummaryReport> reports) throws IOException {
        StringBuilder csv = new StringBuilder();
        csv.append("Account ID,Account Number,Account Type,Customer Name,Current Balance,Total Deposits,Total Withdrawals,Transaction Count,Status,Created Date,Last Activity\n");

        for (AccountSummaryReport report : reports) {
            csv.append(String.format("%d,%s,%s,%s,%.2f,%.2f,%.2f,%d,%s,%s,%s\n",
                report.getAccountId(),
                report.getAccountNumber(),
                report.getAccountType(),
                report.getCustomerName(),
                report.getCurrentBalance(),
                report.getTotalDeposits(),
                report.getTotalWithdrawals(),
                report.getTransactionCount(),
                report.getStatus(),
                report.getCreatedDate() != null ? report.getCreatedDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                report.getLastActivityDate() != null ? report.getLastActivityDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : ""
            ));
        }

        String filename = "account_summary_report_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv";
        String filepath = FileUtil.saveReportToFile(filename, csv.toString());

        LOGGER.info("Exported account summary report to: " + filepath);
        return filepath;
    }

    public String exportTransactionAnalysisReportToCSV(List<TransactionAnalysisReport> reports) throws IOException {
        StringBuilder csv = new StringBuilder();
        csv.append("Transaction Type,Count,Total Amount,Average Amount,Minimum Amount,Maximum Amount,Pending Approvals,Completed Approvals,Cancelled Transactions,Processing Statistics\n");

        for (TransactionAnalysisReport report : reports) {
            csv.append(String.format("%s,%d,%.2f,%.2f,%.2f,%.2f,%d,%d,%d,\"%s\"\n",
                report.getTransactionType(),
                report.getTransactionCount(),
                report.getTotalAmount(),
                report.getAverageAmount(),
                report.getMinimumAmount(),
                report.getMaximumAmount(),
                report.getApprovalsPending(),
                report.getApprovalsCompleted(),
                report.getTransactionsCancelled(),
                report.getProcessingStatistics() != null ? report.getProcessingStatistics() : ""
            ));
        }

        String filename = "transaction_analysis_report_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".csv";
        String filepath = FileUtil.saveReportToFile(filename, csv.toString());

        LOGGER.info("Exported transaction analysis report to: " + filepath);
        return filepath;
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        if (startDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the future");
        }
    }

    public boolean canGenerateUserActivityReport() {
        return userService.getCurrentUser().isAdmin() &&
               userService.hasPermission(AppConfig.PERMISSION_GENERATE_REPORTS);
    }

    public boolean canGenerateAccountSummaryReport() {
        return (userService.getCurrentUser().isAdmin() || userService.getCurrentUser().isManager()) &&
               (userService.hasPermission(AppConfig.PERMISSION_GENERATE_REPORTS) ||
                userService.hasPermission(AppConfig.PERMISSION_GENERATE_CUSTOMER_REPORTS));
    }

    public boolean canGenerateTransactionAnalysisReport() {
        return userService.hasPermission(AppConfig.PERMISSION_GENERATE_REPORTS) ||
               userService.hasPermission(AppConfig.PERMISSION_GENERATE_CUSTOMER_REPORTS);
    }

    public boolean deleteUserActivityReportData(LocalDate startDate, LocalDate endDate, boolean permanent) throws SQLException {
        if (!userService.getCurrentUser().isAdmin()) {
            throw new SecurityException("User activity report deletion is only available to Admin users");
        }

        if (!userService.hasPermission(AppConfig.PERMISSION_DELETE_USER)) {
            throw new SecurityException("Insufficient permissions to delete user activity report data");
        }

        validateDateRange(startDate, endDate);

        try {
            boolean success = reportDAO.deleteUserActivityReportData(startDate, endDate, permanent);
            if (success) {
                String operation = permanent ? "permanently deleted" : "soft deleted";
                LOGGER.warning(String.format("User activity report data %s for period %s to %s by user %s",
                             operation, startDate, endDate, userService.getCurrentUser().getUsername()));
            }
            return success;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete user activity report data", e);
            throw e;
        }
    }

    public boolean deleteAccountSummaryReportData(LocalDate startDate, LocalDate endDate, boolean permanent) throws SQLException {
        if (!userService.hasPermission(AppConfig.PERMISSION_DELETE_ACCOUNT) &&
            !userService.hasPermission(AppConfig.PERMISSION_GENERATE_REPORTS)) {
            throw new SecurityException("Insufficient permissions to delete account summary report data");
        }

        if (permanent && !userService.getCurrentUser().isAdmin()) {
            throw new SecurityException("Permanent deletion of account summary reports is only available to Admin users");
        }

        validateDateRange(startDate, endDate);

        try {
            boolean success = reportDAO.deleteAccountSummaryReportData(startDate, endDate, permanent);
            if (success) {
                String operation = permanent ? "permanently deleted" : "soft deleted";
                LOGGER.warning(String.format("Account summary report data %s for period %s to %s by user %s",
                             operation, startDate, endDate, userService.getCurrentUser().getUsername()));
            }
            return success;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete account summary report data", e);
            throw e;
        }
    }

    public boolean deleteTransactionAnalysisReportData(LocalDate startDate, LocalDate endDate, boolean permanent) throws SQLException {
        if (!userService.hasPermission(AppConfig.PERMISSION_PROCESS_TRANSACTION) &&
            !userService.hasPermission(AppConfig.PERMISSION_GENERATE_REPORTS)) {
            throw new SecurityException("Insufficient permissions to delete transaction analysis report data");
        }

        if (permanent && !userService.getCurrentUser().isAdmin()) {
            throw new SecurityException("Permanent deletion of transaction analysis reports is only available to Admin users");
        }

        validateDateRange(startDate, endDate);

        try {
            boolean success = reportDAO.deleteTransactionAnalysisReportData(startDate, endDate, permanent);
            if (success) {
                String operation = permanent ? "permanently deleted" : "soft deleted";
                LOGGER.warning(String.format("Transaction analysis report data %s for period %s to %s by user %s",
                             operation, startDate, endDate, userService.getCurrentUser().getUsername()));
            }
            return success;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete transaction analysis report data", e);
            throw e;
        }
    }

    public boolean canDeleteUserActivityReports() {
        return userService.getCurrentUser().isAdmin() &&
               userService.hasPermission(AppConfig.PERMISSION_DELETE_USER);
    }

    public boolean canDeleteAccountSummaryReports() {
        return (userService.getCurrentUser().isAdmin() || userService.getCurrentUser().isManager()) &&
               (userService.hasPermission(AppConfig.PERMISSION_DELETE_ACCOUNT) ||
                userService.hasPermission(AppConfig.PERMISSION_GENERATE_REPORTS));
    }

    public boolean canDeleteTransactionAnalysisReports() {
        return userService.hasPermission(AppConfig.PERMISSION_PROCESS_TRANSACTION) ||
               userService.hasPermission(AppConfig.PERMISSION_GENERATE_REPORTS);
    }

    public boolean canPermanentlyDeleteReports() {
        return userService.getCurrentUser().isAdmin();
    }

    public boolean deleteAccountFromReports(Integer accountId, boolean permanent) throws SQLException {
        if (!userService.hasPermission(AppConfig.PERMISSION_DELETE_ACCOUNT)) {
            throw new SecurityException("Insufficient permissions to delete accounts from reports");
        }

        if (permanent && !userService.getCurrentUser().isAdmin()) {
            throw new SecurityException("Permanent account deletion is only available to Admin users");
        }

        try {
            boolean success = reportDAO.deleteAccountFromReports(accountId, permanent);
            if (success) {
                String operation = permanent ? "permanently deleted" : "soft deleted";
                LOGGER.warning(String.format("Account %d %s from reports by user %s",
                             accountId, operation, userService.getCurrentUser().getUsername()));
            }
            return success;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete account from reports", e);
            throw e;
        }
    }

    public boolean deleteAccountByNumber(String accountNumber, boolean permanent) throws SQLException {
        if (!userService.hasPermission(AppConfig.PERMISSION_DELETE_ACCOUNT)) {
            throw new SecurityException("Insufficient permissions to delete accounts");
        }

        if (permanent && !userService.getCurrentUser().isAdmin()) {
            throw new SecurityException("Permanent account deletion is only available to Admin users");
        }

        try {
            boolean success = reportDAO.deleteAccountByNumber(accountNumber, permanent);
            if (success) {
                String operation = permanent ? "permanently deleted" : "soft deleted";
                LOGGER.warning(String.format("Account %s %s by user %s",
                             accountNumber, operation, userService.getCurrentUser().getUsername()));
            }
            return success;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete account by number", e);
            throw e;
        }
    }

    public boolean hasActiveTransactionsInReports(Integer accountId) throws SQLException {
        try {
            return reportDAO.hasActiveTransactionsInReports(accountId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to check active transactions for account", e);
            throw e;
        }
    }

    public boolean isAccountInActiveReports(Integer accountId) throws SQLException {
        try {
            return reportDAO.isAccountInActiveReports(accountId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to check if account is in active reports", e);
            throw e;
        }
    }

    public boolean canDeleteAccount(Integer accountId) throws SQLException {
        if (!userService.hasPermission(AppConfig.PERMISSION_DELETE_ACCOUNT)) {
            return false;
        }

        try {
            return !hasActiveTransactionsInReports(accountId) && !isAccountInActiveReports(accountId);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking account deletion eligibility", e);
            return false;
        }
    }

    public boolean canDeleteAccountPermanently() {
        return userService.getCurrentUser().isAdmin() &&
               userService.hasPermission(AppConfig.PERMISSION_DELETE_ACCOUNT);
    }
}
