package com.bankingsystem.gui;

import com.bankingsystem.models.User;
import com.bankingsystem.services.UserService;
import com.bankingsystem.services.AccountService;
import com.bankingsystem.services.TransactionService;
import com.bankingsystem.services.ApprovalService;
import com.bankingsystem.utils.MockDatabaseUtil;
import com.bankingsystem.utils.DatabaseBackupUtil;
import com.bankingsystem.utils.SettingsManager;
import com.bankingsystem.utils.ThemeManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DashboardFrame extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(DashboardFrame.class.getName());

    private UserService userService;
    private AccountService accountService;
    private TransactionService transactionService;
    private User currentUser;

    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private JLabel userInfoLabel;
    private JLabel sessionTimeLabel;
    private Timer sessionTimer;

    private AccountPanel accountPanel;
    private TransactionPanel transactionPanel;
    private CustomerPanel customerPanel;
    private ReportsPanel reportsPanel;

    public DashboardFrame(UserService userService) {
        this.userService = userService;
        this.accountService = new AccountService();
        this.transactionService = new TransactionService();
        this.currentUser = userService.getCurrentUser();

        // Apply saved theme
        SettingsManager settings = SettingsManager.getInstance();
        ThemeManager.applyTheme(settings.getTheme());

        initializeComponents();
        setupLayout();
        setupMenuBar();
        setupEventHandlers();
        setupFrame();
        startSessionTimer();
    }

    private void initializeComponents() {
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        userInfoLabel = new JLabel();
        sessionTimeLabel = new JLabel();

        updateUserInfo();

        accountPanel = new AccountPanel(accountService, userService);
        transactionPanel = new TransactionPanel(transactionService, accountService, userService);
        customerPanel = new CustomerPanel(userService);
        reportsPanel = new ReportsPanel(userService);

        mainContentPanel.add(accountPanel, "ACCOUNTS");
        mainContentPanel.add(transactionPanel, "TRANSACTIONS");
        mainContentPanel.add(customerPanel, "USERS");
        mainContentPanel.add(reportsPanel, "REPORTS");
        mainContentPanel.add(new SystemAdminPanel(userService), "SYSTEM");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel sidebarPanel = createSidebarPanel();
        add(sidebarPanel, BorderLayout.WEST);

        add(mainContentPanel, BorderLayout.CENTER);

        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("Banking Management System");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);

        userInfoLabel.setForeground(Color.WHITE);
        userInfoLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        userPanel.add(userInfoLabel);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(70, 130, 180));
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> logout());
        userPanel.add(logoutButton);

        headerPanel.add(userPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(240, 240, 240));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidebarPanel.setPreferredSize(new Dimension(200, 0));

        if (currentUser.hasPermission("VIEW_ALL_ACCOUNTS") || currentUser.hasPermission("VIEW_ACCOUNT")) {
            JButton accountsButton = createSidebarButton("Accounts", "ACCOUNTS");
            sidebarPanel.add(accountsButton);
            sidebarPanel.add(Box.createVerticalStrut(5));
        }

        if (currentUser.hasPermission("PROCESS_TRANSACTION")) {
            JButton transactionsButton = createSidebarButton("Transactions", "TRANSACTIONS");
            sidebarPanel.add(transactionsButton);
            sidebarPanel.add(Box.createVerticalStrut(5));
        }

        if (currentUser.hasPermission("VIEW_ALL_USERS") || currentUser.hasPermission("CREATE_USER")) {
            JButton usersButton = createSidebarButton("User Management", "USERS");
            sidebarPanel.add(usersButton);
            sidebarPanel.add(Box.createVerticalStrut(5));
        }

        if (currentUser.hasPermission("GENERATE_REPORTS") || currentUser.hasPermission("GENERATE_CUSTOMER_REPORTS")) {
            JButton reportsButton = createSidebarButton("Reports", "REPORTS");
            sidebarPanel.add(reportsButton);
            sidebarPanel.add(Box.createVerticalStrut(5));
        }

        // Add Approvals button for managers and admins
        if (currentUser.hasPermission("APPROVE_TRANSACTIONS") || currentUser.isManager() || currentUser.isAdmin()) {
            JButton approvalsButton = createApprovalsButton();
            sidebarPanel.add(approvalsButton);
            sidebarPanel.add(Box.createVerticalStrut(5));
        }

        if (currentUser.hasPermission("MANAGE_SYSTEM_CONFIG") || currentUser.hasPermission("BACKUP_DATABASE")) {
            JButton systemButton = createSidebarButton("System Admin", "SYSTEM");
            sidebarPanel.add(systemButton);
            sidebarPanel.add(Box.createVerticalStrut(5));
        }

        sidebarPanel.add(Box.createVerticalGlue());

        // Add help buttons section
        sidebarPanel.add(Box.createVerticalStrut(10));
        JLabel helpLabel = new JLabel("Quick Help");
        helpLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        helpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarPanel.add(helpLabel);
        sidebarPanel.add(Box.createVerticalStrut(5));

        JButton featuresHelpButton = createHelpButton("System Features", "features");
        sidebarPanel.add(featuresHelpButton);
        sidebarPanel.add(Box.createVerticalStrut(3));

        JButton dataHelpButton = createHelpButton("Data Management", "data");
        sidebarPanel.add(dataHelpButton);
        sidebarPanel.add(Box.createVerticalStrut(3));

        JButton userHelpButton = createHelpButton("User Guide", "guide");
        sidebarPanel.add(userHelpButton);
        sidebarPanel.add(Box.createVerticalStrut(10));

        return sidebarPanel;
    }

    private JButton createSidebarButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 40));
        button.setPreferredSize(new Dimension(180, 40));
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        button.addActionListener(e -> cardLayout.show(mainContentPanel, cardName));

        return button;
    }

    private JButton createApprovalsButton() {
        JButton button = new JButton("Approvals");
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 40));
        button.setPreferredSize(new Dimension(180, 40));
        button.setBackground(new Color(255, 140, 0)); // Orange background for approvals
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        button.addActionListener(e -> showApprovalsDialog());

        return button;
    }

    private JButton createHelpButton(String text, String helpType) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 30));
        button.setPreferredSize(new Dimension(180, 30));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        switch (helpType) {
            case "features":
                button.addActionListener(e -> showSystemFeaturesHelp());
                break;
            case "data":
                button.addActionListener(e -> showDataManagementHelp());
                break;
            case "guide":
                button.addActionListener(e -> showUserGuideHelp());
                break;
        }

        return button;
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(250, 250, 250));
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        sessionTimeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        sessionTimeLabel.setForeground(Color.GRAY);
        statusPanel.add(sessionTimeLabel, BorderLayout.EAST);

        return statusPanel;
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> logout());
        fileMenu.add(exitItem);

        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(e -> showSettings());
        toolsMenu.add(settingsItem);

        toolsMenu.addSeparator();
        JMenuItem loadSampleDataItem = new JMenuItem("Load Sample Data");
        loadSampleDataItem.addActionListener(e -> loadSampleData());
        toolsMenu.add(loadSampleDataItem);

        toolsMenu.addSeparator();
        JMenuItem exportBackupItem = new JMenuItem("Export Database Backup");
        exportBackupItem.addActionListener(e -> exportDatabaseBackup());
        toolsMenu.add(exportBackupItem);

        JMenuItem importBackupItem = new JMenuItem("Import Database Backup");
        importBackupItem.addActionListener(e -> importDatabaseBackup());
        toolsMenu.add(importBackupItem);

        JMenuItem backupInfoItem = new JMenuItem("Backup Information");
        backupInfoItem.addActionListener(e -> DatabaseBackupUtil.showBackupInfo(this));
        toolsMenu.add(backupInfoItem);

        JMenu helpMenu = new JMenu("Help");

        JMenuItem systemFeaturesItem = new JMenuItem("System Features Overview");
        systemFeaturesItem.addActionListener(e -> showSystemFeaturesHelp());
        helpMenu.add(systemFeaturesItem);

        JMenuItem dataManagementItem = new JMenuItem("Data Management Options");
        dataManagementItem.addActionListener(e -> showDataManagementHelp());
        helpMenu.add(dataManagementItem);

        JMenuItem userGuideItem = new JMenuItem("User Guide & Help");
        userGuideItem.addActionListener(e -> showUserGuideHelp());
        helpMenu.add(userGuideItem);

        helpMenu.addSeparator();
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void setupEventHandlers() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });
    }

    private void setupFrame() {
        setTitle("Banking Management System - Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
    }

    private void startSessionTimer() {
        sessionTimer = new Timer(60000, e -> {
            if (!userService.isSessionValid()) {
                JOptionPane.showMessageDialog(this,
                    "Your session has expired. Please log in again.",
                    "Session Expired",
                    JOptionPane.WARNING_MESSAGE);
                logout();
            } else {
                updateSessionTime();
            }
        });
        sessionTimer.start();
    }

    private void updateUserInfo() {
        if (currentUser != null) {
            userInfoLabel.setText(String.format("Welcome, %s (%s)",
                                               currentUser.getFullName(),
                                               currentUser.getRole()));
        }
    }

    private void updateSessionTime() {
        sessionTimeLabel.setText("Session active");
    }

    private void logout() {
        if (sessionTimer != null) {
            sessionTimer.stop();
        }

        userService.logout();
        LOGGER.info("User logged out from dashboard");

        dispose();

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    private void showSettings() {
        SettingsDialog settingsDialog = new SettingsDialog(this);
        settingsDialog.setVisible(true);
    }

    private void showAbout() {
        String aboutText = """
            Banking Management System v1.0.0

            A comprehensive banking application built with Java Swing.

            Features:
            - Account Management
            - Transaction Processing
            - Customer Management
            - Reporting and Analytics

            Developed with Java 11 and SQLite database.
            """;

        JOptionPane.showMessageDialog(this, aboutText, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showApprovalsDialog() {
        try {
            ApprovalService approvalService = new ApprovalService();
            ApprovalDialog dialog = new ApprovalDialog(this, approvalService, currentUser.getUserId());
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to open approvals dialog: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSampleData() {
        int result = JOptionPane.showConfirmDialog(this,
            "This will load sample customers and accounts into the database.\n" +
            "If sample data already exists, this operation will be skipped.\n\n" +
            "Do you want to continue?",
            "Load Sample Data",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            try {
                MockDatabaseUtil.loadSampleData();

                // Refresh any open panels to show the new data
                if (accountPanel != null) {
                    // Trigger a refresh of the account panel if it has a refresh method
                    SwingUtilities.invokeLater(() -> {
                        // Force repaint of panels
                        mainContentPanel.revalidate();
                        mainContentPanel.repaint();
                    });
                }

                JOptionPane.showMessageDialog(this,
                    "Sample data loaded successfully!\n" +
                    "Database now contains:\n" +
                    "- 2 sample customers (John Smith, Jane Johnson)\n" +
                    "- 3 sample accounts with different types and balances\n\n" +
                    "You can now test account and transaction operations.",
                    "Sample Data Loaded",
                    JOptionPane.INFORMATION_MESSAGE);

                LOGGER.info("Sample data loaded via menu option by user: " + currentUser.getUsername());

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to load sample data", e);
                JOptionPane.showMessageDialog(this,
                    "Failed to load sample data: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportDatabaseBackup() {
        try {
            boolean success = DatabaseBackupUtil.exportDatabase(this);
            if (success) {
                LOGGER.info("Database backup exported successfully by user: " + currentUser.getUsername());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to export database backup", e);
            JOptionPane.showMessageDialog(this,
                "Failed to export database backup: " + e.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importDatabaseBackup() {
        try {
            boolean success = DatabaseBackupUtil.importDatabase(this);
            if (success) {
                LOGGER.info("Database backup imported successfully by user: " + currentUser.getUsername());

                // Refresh all panels to show imported data
                SwingUtilities.invokeLater(() -> {
                    mainContentPanel.revalidate();
                    mainContentPanel.repaint();
                });

                JOptionPane.showMessageDialog(this,
                    "Database imported successfully!\n" +
                    "All panels will be refreshed to show the imported data.",
                    "Import Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to import database backup", e);
            JOptionPane.showMessageDialog(this,
                "Failed to import database backup: " + e.getMessage(),
                "Import Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSystemFeaturesHelp() {
        String featuresContent = """
            <html>
            <head><style>
            body { font-family: Arial, sans-serif; margin: 10px; }
            h2 { color: #2E5984; margin-top: 15px; margin-bottom: 8px; }
            h3 { color: #4A90E2; margin-top: 12px; margin-bottom: 5px; }
            ul { margin-left: 20px; }
            li { margin-bottom: 3px; }
            .highlight { background-color: #E8F4FD; padding: 8px; border-left: 3px solid #4A90E2; margin: 8px 0; }
            </style></head>
            <body>

            <h2>🏦 Banking Management System - Features Overview</h2>

            <h3>📊 Account Management</h3>
            <ul>
                <li><strong>Create Accounts:</strong> Support for Savings, Checking, and Business accounts</li>
                <li><strong>View Account Details:</strong> Balance, transaction history, account status</li>
                <li><strong>Account Operations:</strong> Activate, deactivate, and permanently delete accounts</li>
                <li><strong>Account Search:</strong> Find accounts by number, customer ID, or account type</li>
                <li><strong>Balance Management:</strong> Real-time balance updates with transaction processing</li>
            </ul>

            <h3>💰 Transaction Processing</h3>
            <ul>
                <li><strong>Deposits:</strong> Cash and check deposits with automatic balance updates</li>
                <li><strong>Withdrawals:</strong> Cash withdrawals with insufficient funds protection</li>
                <li><strong>Transfers:</strong> Account-to-account transfers within the system</li>
                <li><strong>Transaction History:</strong> Complete audit trail with reference numbers</li>
                <li><strong>Real-time Processing:</strong> Immediate balance updates and confirmations</li>
            </ul>

            <h3>👥 Customer Management</h3>
            <ul>
                <li><strong>Customer Registration:</strong> Complete customer profile creation</li>
                <li><strong>Customer Information:</strong> Personal details, contact information, SSN</li>
                <li><strong>Customer Search:</strong> Find customers by name, email, or SSN</li>
                <li><strong>Account Relationships:</strong> Link multiple accounts to customers</li>
                <li><strong>Customer Updates:</strong> Modify customer information as needed</li>
            </ul>

            <div class="highlight">
            <h3>✅ Manager Approval Workflow</h3>
            <ul>
                <li><strong>Automatic Approval:</strong> Transactions within user limits process immediately</li>
                <li><strong>Approval Requests:</strong> Large transactions require manager/admin approval</li>
                <li><strong>Self-Approval:</strong> Managers and admins can approve their own transactions</li>
                <li><strong>Approval Interface:</strong> Dedicated approval dialog for managers</li>
                <li><strong>Audit Trail:</strong> Complete record of all approval decisions</li>
            </ul>
            </div>

            <h3>🔐 User Role Permissions</h3>
            <ul>
                <li><strong>Administrator:</strong> Full system access, unlimited transaction authority</li>
                <li><strong>Manager:</strong> Account management, transaction processing up to $10,000</li>
                <li><strong>Teller:</strong> Basic transactions, customer service, limited authority</li>
                <li><strong>Role-Based Security:</strong> Features restricted based on user permissions</li>
            </ul>

            <h3>📈 Reporting & Analytics</h3>
            <ul>
                <li><strong>Transaction Reports:</strong> Detailed transaction history and summaries</li>
                <li><strong>Account Reports:</strong> Account status, balances, and activity reports</li>
                <li><strong>Customer Reports:</strong> Customer information and account relationships</li>
                <li><strong>System Reports:</strong> User activity, approval statistics, system health</li>
            </ul>

            </body>
            </html>
            """;

        showHelpDialog("System Features Overview", featuresContent, 600, 500);
    }

    private void showDataManagementHelp() {
        String dataContent = """
            <html>
            <head><style>
            body { font-family: Arial, sans-serif; margin: 10px; }
            h2 { color: #2E5984; margin-top: 15px; margin-bottom: 8px; }
            h3 { color: #4A90E2; margin-top: 12px; margin-bottom: 5px; }
            ul { margin-left: 20px; }
            li { margin-bottom: 3px; }
            .highlight { background-color: #E8F4FD; padding: 8px; border-left: 3px solid #4A90E2; margin: 8px 0; }
            .command { background-color: #F5F5F5; padding: 4px 8px; border-radius: 3px; font-family: monospace; }
            .warning { background-color: #FFF3CD; padding: 8px; border-left: 3px solid #FFC107; margin: 8px 0; }
            </style></head>
            <body>

            <h2>💾 Data Management Options</h2>

            <div class="highlight">
            <h3>🔄 Default Startup Behavior (NEW)</h3>
            <p><strong>System now starts with minimal data by default:</strong></p>
            <ul>
                <li>✅ System users loaded automatically (admin, manager, teller)</li>
                <li>❌ No sample customers or accounts loaded</li>
                <li>🎯 Clean environment for testing and production use</li>
                <li>🔐 Authentication always available</li>
            </ul>
            </div>

            <h3>📥 Loading Sample Data</h3>
            <p><strong>Method 1: Command Line Options</strong></p>
            <ul>
                <li><span class="command">--sample</span> or <span class="command">-s</span> : Load sample customers and accounts</li>
                <li><span class="command">--reset</span> or <span class="command">-r</span> : Reset to initial sample data</li>
                <li><span class="command">--empty</span> or <span class="command">-e</span> : Start completely empty (no customers/accounts)</li>
                <li><span class="command">--help</span> or <span class="command">-h</span> : Show all available options</li>
            </ul>

            <p><strong>Method 2: Menu Option</strong></p>
            <ul>
                <li>Go to <strong>Tools → Load Sample Data</strong></li>
                <li>Confirmation dialog explains what will be loaded</li>
                <li>Safe operation - won't overwrite existing data</li>
                <li>Immediate feedback on success/failure</li>
            </ul>

            </body>
            </html>
            """;

        showHelpDialog("Data Management Options", dataContent, 650, 400);
    }

    private void showUserGuideHelp() {
        String guideContent = """
            <html>
            <head><style>
            body { font-family: Arial, sans-serif; margin: 10px; }
            h2 { color: #2E5984; margin-top: 15px; margin-bottom: 8px; }
            h3 { color: #4A90E2; margin-top: 12px; margin-bottom: 5px; }
            ul { margin-left: 20px; }
            li { margin-bottom: 3px; }
            .highlight { background-color: #E8F4FD; padding: 8px; border-left: 3px solid #4A90E2; margin: 8px 0; }
            .credentials { background-color: #F8F9FA; padding: 8px; border: 1px solid #DEE2E6; border-radius: 4px; margin: 8px 0; }
            .workflow { background-color: #E8F5E8; padding: 8px; border-left: 3px solid #28A745; margin: 8px 0; }
            .limits { background-color: #FFF3CD; padding: 8px; border-left: 3px solid #FFC107; margin: 8px 0; }
            </style></head>
            <body>

            <h2>📖 User Guide & Help</h2>

            <div class="credentials">
            <h3>🔑 Login Credentials</h3>
            <ul>
                <li><strong>Administrator:</strong> username = <code>admin</code>, password = <code>password</code></li>
                <li><strong>Manager:</strong> username = <code>manager</code>, password = <code>password</code></li>
                <li><strong>Teller:</strong> username = <code>teller</code>, password = <code>password</code></li>
            </ul>
            <p><em>Note: Change default passwords in production environments</em></p>
            </div>

            <div class="workflow">
            <h3>📋 Step-by-Step Workflows</h3>

            <p><strong>Creating a New Customer & Account:</strong></p>
            <ol>
                <li>Login with appropriate credentials</li>
                <li>Go to Account Management</li>
                <li>Click "Create Account"</li>
                <li>If no customers exist, click "Create Customer" first</li>
                <li>Fill in customer details and create</li>
                <li>Return to account creation and select the customer</li>
                <li>Choose account type and initial deposit</li>
                <li>Click "Create Account"</li>
            </ol>

            <p><strong>Processing a Large Transaction:</strong></p>
            <ol>
                <li>Go to Transactions panel</li>
                <li>Click "Deposit" (or other transaction type)</li>
                <li>Enter account number and amount</li>
                <li>If amount requires approval, system will create approval request</li>
                <li>Manager/Admin can click "Approvals" button to review</li>
                <li>Approve or reject with comments</li>
                <li>Approved transactions process automatically</li>
            </ol>
            </div>

            <div class="limits">
            <h3>💰 Transaction Limits & Approval Thresholds</h3>
            <ul>
                <li><strong>Teller Limit:</strong> $1,000 (requires approval above this)</li>
                <li><strong>Manager Limit:</strong> $10,000 (can self-approve up to limit)</li>
                <li><strong>Admin Limit:</strong> Unlimited (can approve all transactions)</li>
                <li><strong>Approval Threshold:</strong> Varies by role and transaction amount</li>
            </ul>
            </div>

            <h3>🔧 Troubleshooting Common Issues</h3>
            <ul>
                <li><strong>Login Failed:</strong> Check username/password, ensure caps lock is off</li>
                <li><strong>No Customers Available:</strong> Load sample data or create customers first</li>
                <li><strong>Transaction Rejected:</strong> Check account balance and transaction limits</li>
                <li><strong>Approval Pending:</strong> Contact manager/admin for approval</li>
                <li><strong>Account Not Found:</strong> Verify account number is correct</li>
                <li><strong>Insufficient Permissions:</strong> Login with appropriate role</li>
            </ul>

            <h3>⌨️ Keyboard Shortcuts & Navigation</h3>
            <ul>
                <li><strong>Tab:</strong> Navigate between form fields</li>
                <li><strong>Enter:</strong> Submit forms or confirm actions</li>
                <li><strong>Escape:</strong> Cancel dialogs or operations</li>
                <li><strong>Alt + F:</strong> Access File menu</li>
                <li><strong>Alt + T:</strong> Access Tools menu</li>
                <li><strong>Alt + H:</strong> Access Help menu</li>
            </ul>

            <h3>🎯 Quick Tips</h3>
            <ul>
                <li><strong>Start Fresh:</strong> Use --empty flag for clean testing environment</li>
                <li><strong>Load Sample Data:</strong> Use Tools → Load Sample Data for quick setup</li>
                <li><strong>Check Permissions:</strong> Different features available based on user role</li>
                <li><strong>Approval Workflow:</strong> Orange "Approvals" button for managers</li>
                <li><strong>Real-time Updates:</strong> Balances update immediately after transactions</li>
                <li><strong>Reference Numbers:</strong> Every transaction gets unique reference for tracking</li>
            </ul>

            </body>
            </html>
            """;

        showHelpDialog("User Guide & Help", guideContent, 700, 600);
    }

    private void showHelpDialog(String title, String content, int width, int height) {
        JDialog helpDialog = new JDialog(this, title, true);
        helpDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Create HTML content pane
        JEditorPane editorPane = new JEditorPane("text/html", content);
        editorPane.setEditable(false);
        editorPane.setCaretPosition(0); // Scroll to top

        // Add scroll pane
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Create close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> helpDialog.dispose());
        closeButton.setPreferredSize(new Dimension(100, 30));

        // Layout
        helpDialog.setLayout(new BorderLayout());
        helpDialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);
        helpDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Set size and position
        helpDialog.setSize(width, height);
        helpDialog.setLocationRelativeTo(this);
        helpDialog.setResizable(true);

        // Show dialog
        helpDialog.setVisible(true);
    }
}
