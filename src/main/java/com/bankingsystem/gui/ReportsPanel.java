package com.bankingsystem.gui;

import com.bankingsystem.models.*;
import com.bankingsystem.services.ReportService;
import com.bankingsystem.services.UserService;
import com.bankingsystem.utils.ReportDataValidator;
import com.bankingsystem.utils.ErrorHandler;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ReportsPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(ReportsPanel.class.getName());
    private UserService userService;
    private ReportService reportService;

    private JTabbedPane reportTabs;
    private JTable userActivityTable;
    private JTable accountSummaryTable;
    private JTable transactionAnalysisTable;
    private DefaultTableModel userActivityModel;
    private DefaultTableModel accountSummaryModel;
    private DefaultTableModel transactionAnalysisModel;

    private JTextField startDateField;
    private JTextField endDateField;
    private JLabel statusLabel;

    public ReportsPanel(UserService userService) {
        this.userService = userService;
        this.reportService = new ReportService(userService);
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        reportTabs = new JTabbedPane();

        startDateField = new JTextField(LocalDate.now().minusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        endDateField = new JTextField(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        initializeUserActivityTab();
        initializeAccountSummaryTab();
        initializeTransactionAnalysisTab();
    }

    private void initializeUserActivityTab() {
        if (reportService.canGenerateUserActivityReport()) {
            String[] columnNames = {"User ID", "Username", "Role", "Last Login", "Login Count",
                                   "Transaction Count", "Accounts Managed", "Activity Summary"};
            userActivityModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            userActivityTable = new JTable(userActivityModel);
            userActivityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            userActivityTable.setRowHeight(25);
            userActivityTable.getTableHeader().setReorderingAllowed(false);

            JScrollPane scrollPane = new JScrollPane(userActivityTable);
            scrollPane.setBorder(BorderFactory.createTitledBorder("User Activity Report"));

            JPanel userActivityPanel = new JPanel(new BorderLayout());
            userActivityPanel.add(scrollPane, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton generateButton = new JButton("Generate Report");
            JButton exportButton = new JButton("Export CSV");
            JButton deleteButton = new JButton("Delete Report Data");

            generateButton.addActionListener(e -> generateUserActivityReport());
            exportButton.addActionListener(e -> exportUserActivityReport());
            deleteButton.addActionListener(e -> deleteUserActivityReportData());

            deleteButton.setBackground(new Color(220, 20, 60));
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFocusPainted(false);

            if (!reportService.canDeleteUserActivityReports()) {
                deleteButton.setEnabled(false);
                deleteButton.setToolTipText("You do not have permission to delete user activity reports");
            }

            buttonPanel.add(generateButton);
            buttonPanel.add(exportButton);
            buttonPanel.add(deleteButton);

            if (reportService.canPermanentlyDeleteReports()) {
                JButton permanentDeleteButton = new JButton("Delete Permanently");
                permanentDeleteButton.setBackground(new Color(139, 0, 0));
                permanentDeleteButton.setForeground(Color.WHITE);
                permanentDeleteButton.setFocusPainted(false);
                permanentDeleteButton.addActionListener(e -> deleteUserActivityReportDataPermanently());
                buttonPanel.add(permanentDeleteButton);
            }

            userActivityPanel.add(buttonPanel, BorderLayout.SOUTH);

            reportTabs.addTab("User Activity", userActivityPanel);
        }
    }

    private void initializeAccountSummaryTab() {
        if (reportService.canGenerateAccountSummaryReport()) {
            String[] columnNames = {"Account Number", "Type", "Customer", "Current Balance",
                                   "Total Deposits", "Total Withdrawals", "Transaction Count",
                                   "Status", "Last Activity"};
            accountSummaryModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            accountSummaryTable = new JTable(accountSummaryModel);
            accountSummaryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            accountSummaryTable.setRowHeight(25);
            accountSummaryTable.getTableHeader().setReorderingAllowed(false);

            JScrollPane scrollPane = new JScrollPane(accountSummaryTable);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Account Summary Report"));

            JPanel accountSummaryPanel = new JPanel(new BorderLayout());
            accountSummaryPanel.add(scrollPane, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton generateButton = new JButton("Generate Report");
            JButton exportButton = new JButton("Export CSV");
            JButton deleteButton = new JButton("Delete Report Data");

            generateButton.addActionListener(e -> generateAccountSummaryReport());
            exportButton.addActionListener(e -> exportAccountSummaryReport());
            deleteButton.addActionListener(e -> deleteAccountSummaryReportData());

            deleteButton.setBackground(new Color(220, 20, 60));
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFocusPainted(false);

            if (!reportService.canDeleteAccountSummaryReports()) {
                deleteButton.setEnabled(false);
                deleteButton.setToolTipText("You do not have permission to delete account summary reports");
            }

            buttonPanel.add(generateButton);
            buttonPanel.add(exportButton);
            buttonPanel.add(deleteButton);

            if (reportService.canPermanentlyDeleteReports()) {
                JButton permanentDeleteButton = new JButton("Delete Permanently");
                permanentDeleteButton.setBackground(new Color(139, 0, 0));
                permanentDeleteButton.setForeground(Color.WHITE);
                permanentDeleteButton.setFocusPainted(false);
                permanentDeleteButton.addActionListener(e -> deleteAccountSummaryReportDataPermanently());
                buttonPanel.add(permanentDeleteButton);
            }

            JButton deleteAccountButton = new JButton("Delete Account");
            deleteAccountButton.setBackground(new Color(255, 140, 0));
            deleteAccountButton.setForeground(Color.WHITE);
            deleteAccountButton.setFocusPainted(false);
            deleteAccountButton.addActionListener(e -> deleteSelectedAccount());

            if (!reportService.canDeleteAccountSummaryReports()) {
                deleteAccountButton.setEnabled(false);
                deleteAccountButton.setToolTipText("You do not have permission to delete accounts");
            }

            buttonPanel.add(deleteAccountButton);

            if (reportService.canDeleteAccountPermanently()) {
                JButton deleteAccountPermanentlyButton = new JButton("Delete Account Permanently");
                deleteAccountPermanentlyButton.setBackground(new Color(178, 34, 34));
                deleteAccountPermanentlyButton.setForeground(Color.WHITE);
                deleteAccountPermanentlyButton.setFocusPainted(false);
                deleteAccountPermanentlyButton.addActionListener(e -> deleteSelectedAccountPermanently());
                buttonPanel.add(deleteAccountPermanentlyButton);
            }

            accountSummaryPanel.add(buttonPanel, BorderLayout.SOUTH);

            reportTabs.addTab("Account Summary", accountSummaryPanel);
        }
    }

    private void initializeTransactionAnalysisTab() {
        if (reportService.canGenerateTransactionAnalysisReport()) {
            String[] columnNames = {"Transaction Type", "Count", "Total Amount", "Average Amount",
                                   "Min Amount", "Max Amount", "Pending", "Completed", "Cancelled"};
            transactionAnalysisModel = new DefaultTableModel(columnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            transactionAnalysisTable = new JTable(transactionAnalysisModel);
            transactionAnalysisTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            transactionAnalysisTable.setRowHeight(25);
            transactionAnalysisTable.getTableHeader().setReorderingAllowed(false);

            JScrollPane scrollPane = new JScrollPane(transactionAnalysisTable);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Transaction Analysis Report"));

            JPanel transactionAnalysisPanel = new JPanel(new BorderLayout());
            transactionAnalysisPanel.add(scrollPane, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton generateButton = new JButton("Generate Report");
            JButton exportButton = new JButton("Export CSV");
            JButton deleteButton = new JButton("Delete Report Data");

            generateButton.addActionListener(e -> generateTransactionAnalysisReport());
            exportButton.addActionListener(e -> exportTransactionAnalysisReport());
            deleteButton.addActionListener(e -> deleteTransactionAnalysisReportData());

            deleteButton.setBackground(new Color(220, 20, 60));
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFocusPainted(false);

            if (!reportService.canDeleteTransactionAnalysisReports()) {
                deleteButton.setEnabled(false);
                deleteButton.setToolTipText("You do not have permission to delete transaction analysis reports");
            }

            buttonPanel.add(generateButton);
            buttonPanel.add(exportButton);
            buttonPanel.add(deleteButton);

            if (reportService.canPermanentlyDeleteReports()) {
                JButton permanentDeleteButton = new JButton("Delete Permanently");
                permanentDeleteButton.setBackground(new Color(139, 0, 0));
                permanentDeleteButton.setForeground(Color.WHITE);
                permanentDeleteButton.setFocusPainted(false);
                permanentDeleteButton.addActionListener(e -> deleteTransactionAnalysisReportDataPermanently());
                buttonPanel.add(permanentDeleteButton);
            }

            transactionAnalysisPanel.add(buttonPanel, BorderLayout.SOUTH);

            reportTabs.addTab("Transaction Analysis", transactionAnalysisPanel);
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Reports and Analytics");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        datePanel.add(new JLabel("From:"));
        datePanel.add(startDateField);
        datePanel.add(new JLabel("To:"));
        datePanel.add(endDateField);

        // Add refresh button for data synchronization
        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.setToolTipText("Refresh data and check for recent changes");
        refreshButton.addActionListener(e -> refreshReportData());
        datePanel.add(refreshButton);

        headerPanel.add(datePanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        if (reportTabs.getTabCount() > 0) {
            add(reportTabs, BorderLayout.CENTER);
        } else {
            JPanel noAccessPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(20, 20, 20, 20);

            JLabel messageLabel = new JLabel("No reports available for your role.");
            messageLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            messageLabel.setForeground(Color.GRAY);
            noAccessPanel.add(messageLabel, gbc);

            add(noAccessPanel, BorderLayout.CENTER);
        }

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        startDateField.setColumns(10);
        endDateField.setColumns(10);
    }

    private void generateUserActivityReport() {
        SwingWorker<List<UserActivityReport>, Void> worker = new SwingWorker<List<UserActivityReport>, Void>() {
            @Override
            protected List<UserActivityReport> doInBackground() throws Exception {
                // Validate date inputs
                LocalDate startDate, endDate;
                try {
                    startDate = LocalDate.parse(startDateField.getText());
                    endDate = LocalDate.parse(endDateField.getText());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid date format. Please use YYYY-MM-DD format.");
                }

                if (startDate.isAfter(endDate)) {
                    throw new IllegalArgumentException("Start date cannot be after end date.");
                }

                // Refresh data before generating report
                ReportDataValidator.synchronizeReportData();

                List<UserActivityReport> reports = reportService.generateUserActivityReport(startDate, endDate);

                // Validate report data accuracy
                boolean isValid = ReportDataValidator.validateUserActivityData(reports, startDate, endDate);
                if (!isValid) {
                    LOGGER.warning("User activity report data validation failed - some data may be inaccurate");
                }

                return reports;
            }

            @Override
            protected void done() {
                try {
                    List<UserActivityReport> reports = get();
                    updateUserActivityTable(reports);

                    String dataStatus = ReportDataValidator.getDataFreshnessStatus();
                    statusLabel.setText(String.format("Generated user activity report with %d entries. %s",
                        reports.size(), dataStatus));
                    statusLabel.setForeground(Color.BLUE);

                    if (reports.isEmpty()) {
                        statusLabel.setText("No user activity data found for the selected date range. " + dataStatus);
                        statusLabel.setForeground(Color.ORANGE);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to generate user activity report", e);
                    String userMessage = ErrorHandler.getUserFriendlyMessage(e);
                    ErrorHandler.showError(ReportsPanel.this, "Report Generation Error",
                        "Failed to generate user activity report.\n\n" + userMessage);
                    statusLabel.setText("Report generation failed: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        };

        worker.execute();
    }

    private void generateAccountSummaryReport() {
        SwingWorker<List<AccountSummaryReport>, Void> worker = new SwingWorker<List<AccountSummaryReport>, Void>() {
            @Override
            protected List<AccountSummaryReport> doInBackground() throws Exception {
                // Validate date inputs
                LocalDate startDate, endDate;
                try {
                    startDate = LocalDate.parse(startDateField.getText());
                    endDate = LocalDate.parse(endDateField.getText());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid date format. Please use YYYY-MM-DD format.");
                }

                if (startDate.isAfter(endDate)) {
                    throw new IllegalArgumentException("Start date cannot be after end date.");
                }

                // Refresh data before generating report
                ReportDataValidator.synchronizeReportData();

                List<AccountSummaryReport> reports = reportService.generateAccountSummaryReport(startDate, endDate);

                // Validate report data accuracy
                boolean isValid = ReportDataValidator.validateAccountSummaryData(reports);
                if (!isValid) {
                    LOGGER.warning("Account summary report data validation failed - some data may be inaccurate");
                }

                return reports;
            }

            @Override
            protected void done() {
                try {
                    List<AccountSummaryReport> reports = get();
                    updateAccountSummaryTable(reports);

                    String dataStatus = ReportDataValidator.getDataFreshnessStatus();
                    statusLabel.setText(String.format("Generated account summary report with %d entries. %s",
                        reports.size(), dataStatus));
                    statusLabel.setForeground(Color.BLUE);

                    if (reports.isEmpty()) {
                        statusLabel.setText("No account data found for the selected date range. " + dataStatus);
                        statusLabel.setForeground(Color.ORANGE);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to generate account summary report", e);
                    String userMessage = ErrorHandler.getUserFriendlyMessage(e);
                    ErrorHandler.showError(ReportsPanel.this, "Report Generation Error",
                        "Failed to generate account summary report.\n\n" + userMessage);
                    statusLabel.setText("Report generation failed: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        };

        worker.execute();
    }

    private void generateTransactionAnalysisReport() {
        SwingWorker<List<TransactionAnalysisReport>, Void> worker = new SwingWorker<List<TransactionAnalysisReport>, Void>() {
            @Override
            protected List<TransactionAnalysisReport> doInBackground() throws Exception {
                // Validate date inputs
                LocalDate startDate, endDate;
                try {
                    startDate = LocalDate.parse(startDateField.getText());
                    endDate = LocalDate.parse(endDateField.getText());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid date format. Please use YYYY-MM-DD format.");
                }

                if (startDate.isAfter(endDate)) {
                    throw new IllegalArgumentException("Start date cannot be after end date.");
                }

                // Refresh data before generating report
                ReportDataValidator.synchronizeReportData();

                List<TransactionAnalysisReport> reports = reportService.generateTransactionAnalysisReport(startDate, endDate);

                // Validate report data accuracy
                boolean isValid = ReportDataValidator.validateTransactionAnalysisData(reports, startDate, endDate);
                if (!isValid) {
                    LOGGER.warning("Transaction analysis report data validation failed - some data may be inaccurate");
                }

                return reports;
            }

            @Override
            protected void done() {
                try {
                    List<TransactionAnalysisReport> reports = get();
                    updateTransactionAnalysisTable(reports);

                    String dataStatus = ReportDataValidator.getDataFreshnessStatus();
                    statusLabel.setText(String.format("Generated transaction analysis report with %d entries. %s",
                        reports.size(), dataStatus));
                    statusLabel.setForeground(Color.BLUE);

                    if (reports.isEmpty()) {
                        statusLabel.setText("No transaction data found for the selected date range. " + dataStatus);
                        statusLabel.setForeground(Color.ORANGE);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to generate transaction analysis report", e);
                    String userMessage = ErrorHandler.getUserFriendlyMessage(e);
                    ErrorHandler.showError(ReportsPanel.this, "Report Generation Error",
                        "Failed to generate transaction analysis report.\n\n" + userMessage);
                    statusLabel.setText("Report generation failed: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        };

        worker.execute();
    }

    /**
     * Refresh report data and check for recent changes
     */
    private void refreshReportData() {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                ReportDataValidator.synchronizeReportData();
                return ReportDataValidator.getDataFreshnessStatus();
            }

            @Override
            protected void done() {
                try {
                    String dataStatus = get();
                    statusLabel.setText("Data refreshed successfully. " + dataStatus);
                    statusLabel.setForeground(Color.GREEN);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to refresh report data", e);
                    statusLabel.setText("Data refresh failed: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        };

        statusLabel.setText("Refreshing data...");
        statusLabel.setForeground(Color.BLUE);
        worker.execute();
    }

    private void updateUserActivityTable(List<UserActivityReport> reports) {
        userActivityModel.setRowCount(0);

        for (UserActivityReport report : reports) {
            Object[] row = {
                report.getUserId(),
                report.getUsername(),
                report.getRole(),
                report.getLastLogin() != null ? report.getLastLogin().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Never",
                report.getLoginCount(),
                report.getTransactionCount(),
                report.getAccountsManaged(),
                report.getActivitySummary() != null ? report.getActivitySummary() : ""
            };
            userActivityModel.addRow(row);
        }
    }

    private void updateAccountSummaryTable(List<AccountSummaryReport> reports) {
        accountSummaryModel.setRowCount(0);

        for (AccountSummaryReport report : reports) {
            Object[] row = {
                report.getAccountNumber(),
                report.getAccountType(),
                report.getCustomerName(),
                String.format("$%.2f", report.getCurrentBalance()),
                String.format("$%.2f", report.getTotalDeposits()),
                String.format("$%.2f", report.getTotalWithdrawals()),
                report.getTransactionCount(),
                report.getStatus(),
                report.getLastActivityDate() != null ? report.getLastActivityDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "None"
            };
            accountSummaryModel.addRow(row);
        }
    }

    private void updateTransactionAnalysisTable(List<TransactionAnalysisReport> reports) {
        transactionAnalysisModel.setRowCount(0);

        for (TransactionAnalysisReport report : reports) {
            Object[] row = {
                report.getTransactionType(),
                report.getTransactionCount(),
                String.format("$%.2f", report.getTotalAmount()),
                String.format("$%.2f", report.getAverageAmount()),
                String.format("$%.2f", report.getMinimumAmount()),
                String.format("$%.2f", report.getMaximumAmount()),
                report.getApprovalsPending(),
                report.getApprovalsCompleted(),
                report.getTransactionsCancelled()
            };
            transactionAnalysisModel.addRow(row);
        }
    }

    private void exportUserActivityReport() {
        if (userActivityModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export. Please generate a report first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<UserActivityReport> reports = getCurrentUserActivityReports();
            String filepath = reportService.exportUserActivityReportToCSV(reports);
            JOptionPane.showMessageDialog(this, "Report exported successfully to:\n" + filepath, "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            statusLabel.setText("Report exported to: " + filepath);
            statusLabel.setForeground(Color.GREEN);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to export user activity report", e);
            JOptionPane.showMessageDialog(this, "Failed to export report:\n" + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Export failed: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void exportAccountSummaryReport() {
        if (accountSummaryModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export. Please generate a report first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<AccountSummaryReport> reports = getCurrentAccountSummaryReports();
            String filepath = reportService.exportAccountSummaryReportToCSV(reports);
            JOptionPane.showMessageDialog(this, "Report exported successfully to:\n" + filepath, "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            statusLabel.setText("Report exported to: " + filepath);
            statusLabel.setForeground(Color.GREEN);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to export account summary report", e);
            JOptionPane.showMessageDialog(this, "Failed to export report:\n" + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Export failed: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void exportTransactionAnalysisReport() {
        if (transactionAnalysisModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export. Please generate a report first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<TransactionAnalysisReport> reports = getCurrentTransactionAnalysisReports();
            String filepath = reportService.exportTransactionAnalysisReportToCSV(reports);
            JOptionPane.showMessageDialog(this, "Report exported successfully to:\n" + filepath, "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            statusLabel.setText("Report exported to: " + filepath);
            statusLabel.setForeground(Color.GREEN);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to export transaction analysis report", e);
            JOptionPane.showMessageDialog(this, "Failed to export report:\n" + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Export failed: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private List<UserActivityReport> getCurrentUserActivityReports() throws SQLException {
        LocalDate startDate = LocalDate.parse(startDateField.getText());
        LocalDate endDate = LocalDate.parse(endDateField.getText());
        return reportService.generateUserActivityReport(startDate, endDate);
    }

    private List<AccountSummaryReport> getCurrentAccountSummaryReports() throws SQLException {
        LocalDate startDate = LocalDate.parse(startDateField.getText());
        LocalDate endDate = LocalDate.parse(endDateField.getText());
        return reportService.generateAccountSummaryReport(startDate, endDate);
    }

    private List<TransactionAnalysisReport> getCurrentTransactionAnalysisReports() throws SQLException {
        LocalDate startDate = LocalDate.parse(startDateField.getText());
        LocalDate endDate = LocalDate.parse(endDateField.getText());
        return reportService.generateTransactionAnalysisReport(startDate, endDate);
    }

    private void deleteUserActivityReportData() {
        if (userActivityModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No report data to delete. Please generate a report first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete the User Activity Report data for the selected date range?\n" +
            "This will perform a soft delete that can be recovered later.",
            "Confirm Soft Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                LocalDate startDate = LocalDate.parse(startDateField.getText());
                LocalDate endDate = LocalDate.parse(endDateField.getText());

                boolean success = reportService.deleteUserActivityReportData(startDate, endDate, false);
                if (success) {
                    userActivityModel.setRowCount(0);
                    statusLabel.setText("User activity report data deleted successfully (soft delete)");
                    statusLabel.setForeground(Color.ORANGE);

                    JOptionPane.showMessageDialog(this,
                        "User activity report data has been soft deleted.\n" +
                        "Data can be recovered if needed.",
                        "Deletion Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    statusLabel.setText("No data was deleted for the specified date range");
                    statusLabel.setForeground(Color.GRAY);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to delete user activity report data", e);
                statusLabel.setText("Failed to delete report data: " + e.getMessage());
                statusLabel.setForeground(Color.RED);

                JOptionPane.showMessageDialog(this,
                    "Failed to delete user activity report data:\n" + e.getMessage(),
                    "Deletion Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteUserActivityReportDataPermanently() {
        if (userActivityModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No report data to delete. Please generate a report first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] options = {"Cancel", "I Understand - Delete Permanently"};
        int firstConfirm = JOptionPane.showOptionDialog(this,
            "WARNING: PERMANENT DELETION\n\n" +
            "You are about to PERMANENTLY DELETE User Activity Report data\n" +
            "for the selected date range.\n\n" +
            "This action:\n" +
            "• Cannot be undone\n" +
            "• Will remove user activity data from the system\n" +
            "• May affect audit trails and compliance records\n\n" +
            "Are you absolutely sure you want to proceed?",
            "Confirm Permanent Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            options,
            options[0]);

        if (firstConfirm != 1) {
            return;
        }

        int secondConfirm = JOptionPane.showConfirmDialog(this,
            "FINAL CONFIRMATION\n\n" +
            "This is your last chance to cancel this irreversible action.\n" +
            "Permanently delete User Activity Report data?",
            "Final Confirmation Required",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.ERROR_MESSAGE);

        if (secondConfirm == JOptionPane.OK_OPTION) {
            try {
                LocalDate startDate = LocalDate.parse(startDateField.getText());
                LocalDate endDate = LocalDate.parse(endDateField.getText());

                boolean success = reportService.deleteUserActivityReportData(startDate, endDate, true);
                if (success) {
                    userActivityModel.setRowCount(0);
                    statusLabel.setText("User activity report data permanently deleted");
                    statusLabel.setForeground(Color.RED);

                    JOptionPane.showMessageDialog(this,
                        "User activity report data has been permanently deleted from the system.",
                        "Permanent Deletion Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    statusLabel.setText("No data was permanently deleted for the specified date range");
                    statusLabel.setForeground(Color.GRAY);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to permanently delete user activity report data", e);
                statusLabel.setText("Failed to permanently delete report data: " + e.getMessage());
                statusLabel.setForeground(Color.RED);

                JOptionPane.showMessageDialog(this,
                    "Failed to permanently delete user activity report data:\n" + e.getMessage(),
                    "Deletion Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteAccountSummaryReportData() {
        if (accountSummaryModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No report data to delete. Please generate a report first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete the Account Summary Report data for the selected date range?\n" +
            "This will perform a soft delete that can be recovered later.\n" +
            "Note: Accounts with active transactions will be skipped.",
            "Confirm Soft Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                LocalDate startDate = LocalDate.parse(startDateField.getText());
                LocalDate endDate = LocalDate.parse(endDateField.getText());

                boolean success = reportService.deleteAccountSummaryReportData(startDate, endDate, false);
                if (success) {
                    generateAccountSummaryReport();
                    statusLabel.setText("Account summary report data deleted successfully (soft delete)");
                    statusLabel.setForeground(Color.ORANGE);

                    JOptionPane.showMessageDialog(this,
                        "Account summary report data has been soft deleted.\n" +
                        "Data can be recovered if needed.\n" +
                        "Accounts with active transactions were preserved.",
                        "Deletion Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    statusLabel.setText("No data was deleted for the specified date range");
                    statusLabel.setForeground(Color.GRAY);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to delete account summary report data", e);
                statusLabel.setText("Failed to delete report data: " + e.getMessage());
                statusLabel.setForeground(Color.RED);

                JOptionPane.showMessageDialog(this,
                    "Failed to delete account summary report data:\n" + e.getMessage(),
                    "Deletion Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteAccountSummaryReportDataPermanently() {
        if (accountSummaryModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No report data to delete. Please generate a report first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] options = {"Cancel", "I Understand - Delete Permanently"};
        int firstConfirm = JOptionPane.showOptionDialog(this,
            "WARNING: PERMANENT DELETION\n\n" +
            "You are about to PERMANENTLY DELETE Account Summary Report data\n" +
            "for the selected date range.\n\n" +
            "This action:\n" +
            "• Cannot be undone\n" +
            "• Will remove account and related transaction data\n" +
            "• May affect customer records and audit trails\n" +
            "• Accounts with active transactions will be skipped\n\n" +
            "Are you absolutely sure you want to proceed?",
            "Confirm Permanent Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            options,
            options[0]);

        if (firstConfirm != 1) {
            return;
        }

        int secondConfirm = JOptionPane.showConfirmDialog(this,
            "FINAL CONFIRMATION\n\n" +
            "This is your last chance to cancel this irreversible action.\n" +
            "Permanently delete Account Summary Report data?",
            "Final Confirmation Required",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.ERROR_MESSAGE);

        if (secondConfirm == JOptionPane.OK_OPTION) {
            try {
                LocalDate startDate = LocalDate.parse(startDateField.getText());
                LocalDate endDate = LocalDate.parse(endDateField.getText());

                boolean success = reportService.deleteAccountSummaryReportData(startDate, endDate, true);
                if (success) {
                    generateAccountSummaryReport();
                    statusLabel.setText("Account summary report data permanently deleted");
                    statusLabel.setForeground(Color.RED);

                    JOptionPane.showMessageDialog(this,
                        "Account summary report data has been permanently deleted from the system.\n" +
                        "Related transaction data was also removed.",
                        "Permanent Deletion Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    statusLabel.setText("No data was permanently deleted for the specified date range");
                    statusLabel.setForeground(Color.GRAY);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to permanently delete account summary report data", e);
                statusLabel.setText("Failed to permanently delete report data: " + e.getMessage());
                statusLabel.setForeground(Color.RED);

                JOptionPane.showMessageDialog(this,
                    "Failed to permanently delete account summary report data:\n" + e.getMessage(),
                    "Deletion Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteTransactionAnalysisReportData() {
        if (transactionAnalysisModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No report data to delete. Please generate a report first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete the Transaction Analysis Report data for the selected date range?\n" +
            "This will perform a soft delete that can be recovered later.",
            "Confirm Soft Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                LocalDate startDate = LocalDate.parse(startDateField.getText());
                LocalDate endDate = LocalDate.parse(endDateField.getText());

                boolean success = reportService.deleteTransactionAnalysisReportData(startDate, endDate, false);
                if (success) {
                    generateTransactionAnalysisReport();
                    statusLabel.setText("Transaction analysis report data deleted successfully (soft delete)");
                    statusLabel.setForeground(Color.ORANGE);

                    JOptionPane.showMessageDialog(this,
                        "Transaction analysis report data has been soft deleted.\n" +
                        "Data can be recovered if needed.",
                        "Deletion Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    statusLabel.setText("No data was deleted for the specified date range");
                    statusLabel.setForeground(Color.GRAY);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to delete transaction analysis report data", e);
                statusLabel.setText("Failed to delete report data: " + e.getMessage());
                statusLabel.setForeground(Color.RED);

                JOptionPane.showMessageDialog(this,
                    "Failed to delete transaction analysis report data:\n" + e.getMessage(),
                    "Deletion Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteTransactionAnalysisReportDataPermanently() {
        if (transactionAnalysisModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No report data to delete. Please generate a report first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] options = {"Cancel", "I Understand - Delete Permanently"};
        int firstConfirm = JOptionPane.showOptionDialog(this,
            "WARNING: PERMANENT DELETION\n\n" +
            "You are about to PERMANENTLY DELETE Transaction Analysis Report data\n" +
            "for the selected date range.\n\n" +
            "This action:\n" +
            "• Cannot be undone\n" +
            "• Will remove transaction data from the system\n" +
            "• May affect account balances and audit trails\n\n" +
            "Are you absolutely sure you want to proceed?",
            "Confirm Permanent Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            options,
            options[0]);

        if (firstConfirm != 1) {
            return;
        }

        int secondConfirm = JOptionPane.showConfirmDialog(this,
            "FINAL CONFIRMATION\n\n" +
            "This is your last chance to cancel this irreversible action.\n" +
            "Permanently delete Transaction Analysis Report data?",
            "Final Confirmation Required",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.ERROR_MESSAGE);

        if (secondConfirm == JOptionPane.OK_OPTION) {
            try {
                LocalDate startDate = LocalDate.parse(startDateField.getText());
                LocalDate endDate = LocalDate.parse(endDateField.getText());

                boolean success = reportService.deleteTransactionAnalysisReportData(startDate, endDate, true);
                if (success) {
                    generateTransactionAnalysisReport();
                    statusLabel.setText("Transaction analysis report data permanently deleted");
                    statusLabel.setForeground(Color.RED);

                    JOptionPane.showMessageDialog(this,
                        "Transaction analysis report data has been permanently deleted from the system.",
                        "Permanent Deletion Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    statusLabel.setText("No data was permanently deleted for the specified date range");
                    statusLabel.setForeground(Color.GRAY);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to permanently delete transaction analysis report data", e);
                statusLabel.setText("Failed to permanently delete report data: " + e.getMessage());
                statusLabel.setForeground(Color.RED);

                JOptionPane.showMessageDialog(this,
                    "Failed to permanently delete transaction analysis report data:\n" + e.getMessage(),
                    "Deletion Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedAccount() {
        int selectedRow = accountSummaryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an account to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String accountNumber = (String) accountSummaryModel.getValueAt(selectedRow, 0);
        String customerName = (String) accountSummaryModel.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Are you sure you want to delete account %s (%s)?\n" +
                         "This will perform a soft delete that can be recovered later.\n" +
                         "The account will be marked as deleted but data will be preserved.",
                         accountNumber, customerName),
            "Confirm Account Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = reportService.deleteAccountByNumber(accountNumber, false);
                if (success) {
                    generateAccountSummaryReport();
                    statusLabel.setText(String.format("Account %s deleted successfully (soft delete)", accountNumber));
                    statusLabel.setForeground(Color.ORANGE);

                    JOptionPane.showMessageDialog(this,
                        String.format("Account %s has been soft deleted.\n" +
                                     "The account is marked as deleted but can be recovered if needed.",
                                     accountNumber),
                        "Account Deletion Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    statusLabel.setText("Failed to delete account - account not found or has restrictions");
                    statusLabel.setForeground(Color.RED);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to delete account", e);
                statusLabel.setText("Failed to delete account: " + e.getMessage());
                statusLabel.setForeground(Color.RED);

                JOptionPane.showMessageDialog(this,
                    "Failed to delete account:\n" + e.getMessage(),
                    "Account Deletion Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedAccountPermanently() {
        int selectedRow = accountSummaryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an account to delete permanently.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String accountNumber = (String) accountSummaryModel.getValueAt(selectedRow, 0);
        String customerName = (String) accountSummaryModel.getValueAt(selectedRow, 2);

        String[] options = {"Cancel", "I Understand - Delete Permanently"};
        int firstConfirm = JOptionPane.showOptionDialog(this,
            String.format("WARNING: PERMANENT ACCOUNT DELETION\n\n" +
                         "You are about to PERMANENTLY DELETE account %s (%s).\n\n" +
                         "This action:\n" +
                         "• Cannot be undone\n" +
                         "• Will remove the account and ALL related transactions\n" +
                         "• May affect customer records and audit trails\n" +
                         "• Will impact all reports containing this account\n\n" +
                         "Are you absolutely sure you want to proceed?",
                         accountNumber, customerName),
            "Confirm Permanent Account Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            options,
            options[0]);

        if (firstConfirm != 1) {
            return;
        }

        int secondConfirm = JOptionPane.showConfirmDialog(this,
            String.format("FINAL CONFIRMATION\n\n" +
                         "This is your last chance to cancel this irreversible action.\n" +
                         "Permanently delete account %s and all its data?",
                         accountNumber),
            "Final Confirmation Required",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.ERROR_MESSAGE);

        if (secondConfirm == JOptionPane.OK_OPTION) {
            try {
                boolean success = reportService.deleteAccountByNumber(accountNumber, true);
                if (success) {
                    generateAccountSummaryReport();
                    statusLabel.setText(String.format("Account %s permanently deleted", accountNumber));
                    statusLabel.setForeground(Color.RED);

                    JOptionPane.showMessageDialog(this,
                        String.format("Account %s has been permanently deleted from the system.\n" +
                                     "All related transaction data has also been removed.",
                                     accountNumber),
                        "Permanent Account Deletion Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    statusLabel.setText("Failed to permanently delete account - account not found or has restrictions");
                    statusLabel.setForeground(Color.RED);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to permanently delete account", e);
                statusLabel.setText("Failed to permanently delete account: " + e.getMessage());
                statusLabel.setForeground(Color.RED);

                JOptionPane.showMessageDialog(this,
                    "Failed to permanently delete account:\n" + e.getMessage(),
                    "Account Deletion Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}