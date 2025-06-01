package com.bankingsystem.gui;

import com.bankingsystem.models.AbstractAccount;
import com.bankingsystem.services.AccountService;
import com.bankingsystem.services.UserService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AccountPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(AccountPanel.class.getName());

    private AccountService accountService;
    private UserService userService;

    private JTable accountTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton createAccountButton;
    private JButton refreshButton;
    private JButton viewDetailsButton;
    private JButton deleteAccountButton;
    private JButton bulkDeleteButton;
    private JButton criteriaDeleteButton;
    private JButton transferFundsButton;
    private JLabel statusLabel;

    public AccountPanel(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadAccounts();
    }

    private void initializeComponents() {
        String[] columnNames = {"Account ID", "Account Number", "Customer ID", "Type", "Balance", "Status", "Created Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        accountTable = new JTable(tableModel);
        accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountTable.setRowHeight(25);
        accountTable.getTableHeader().setReorderingAllowed(false);

        accountTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        accountTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        accountTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        accountTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        accountTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        accountTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        accountTable.getColumnModel().getColumn(6).setPreferredWidth(150);

        searchField = new JTextField(20);
        searchField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        createAccountButton = new JButton("Create Account");
        refreshButton = new JButton("Refresh");
        viewDetailsButton = new JButton("View Details");
        deleteAccountButton = new JButton("Delete Account Permanently");
        bulkDeleteButton = new JButton("Bulk Delete Accounts");
        criteriaDeleteButton = new JButton("Delete by Criteria");
        transferFundsButton = new JButton("Transfer Funds");

        createAccountButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        refreshButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        viewDetailsButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        deleteAccountButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        bulkDeleteButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        criteriaDeleteButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        transferFundsButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        createAccountButton.setBackground(new Color(70, 130, 180));
        createAccountButton.setForeground(Color.WHITE);
        createAccountButton.setFocusPainted(false);

        deleteAccountButton.setBackground(new Color(220, 20, 60));
        deleteAccountButton.setForeground(Color.WHITE);
        deleteAccountButton.setFocusPainted(false);
        deleteAccountButton.setEnabled(false);

        bulkDeleteButton.setBackground(new Color(178, 34, 34));
        bulkDeleteButton.setForeground(Color.WHITE);
        bulkDeleteButton.setFocusPainted(false);

        criteriaDeleteButton.setBackground(new Color(139, 0, 0));
        criteriaDeleteButton.setForeground(Color.WHITE);
        criteriaDeleteButton.setFocusPainted(false);

        transferFundsButton.setBackground(new Color(34, 139, 34));
        transferFundsButton.setForeground(Color.WHITE);
        transferFundsButton.setFocusPainted(false);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        if (!userService.hasPermission("CREATE_ACCOUNT")) {
            createAccountButton.setEnabled(false);
            createAccountButton.setToolTipText("You do not have permission to create accounts");
        }

        if (!userService.hasPermission("VIEW_ALL_ACCOUNTS")) {
            viewDetailsButton.setEnabled(false);
            viewDetailsButton.setToolTipText("You do not have permission to view account details");
        }

        if (!userService.getCurrentUser().isAdmin() || !userService.hasPermission("DELETE_ACCOUNT")) {
            deleteAccountButton.setEnabled(false);
            deleteAccountButton.setToolTipText("Only Admin users can permanently delete accounts");
            bulkDeleteButton.setEnabled(false);
            bulkDeleteButton.setToolTipText("Only Admin users can perform bulk account deletions");
            criteriaDeleteButton.setEnabled(false);
            criteriaDeleteButton.setToolTipText("Only Admin users can delete accounts by criteria");
        }

        if (!userService.hasPermission("PROCESS_TRANSACTION")) {
            transferFundsButton.setEnabled(false);
            transferFundsButton.setToolTipText("You do not have permission to process transfers");
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Account Management");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        headerPanel.add(searchPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(accountTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Accounts"));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(createAccountButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(transferFundsButton);

        JPanel deleteButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        deleteButtonPanel.add(deleteAccountButton);
        deleteButtonPanel.add(bulkDeleteButton);
        deleteButtonPanel.add(criteriaDeleteButton);

        JPanel allButtonsPanel = new JPanel(new BorderLayout());
        allButtonsPanel.add(buttonPanel, BorderLayout.NORTH);
        allButtonsPanel.add(deleteButtonPanel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(allButtonsPanel, BorderLayout.WEST);
        bottomPanel.add(statusLabel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        createAccountButton.addActionListener(e -> showCreateAccountDialog());
        refreshButton.addActionListener(e -> loadAccounts());
        viewDetailsButton.addActionListener(e -> viewAccountDetails());
        deleteAccountButton.addActionListener(e -> deleteSelectedAccountPermanently());
        bulkDeleteButton.addActionListener(e -> showBulkDeleteDialog());
        criteriaDeleteButton.addActionListener(e -> showCriteriaDeleteDialog());
        transferFundsButton.addActionListener(e -> showTransferDialog());

        searchField.addActionListener(e -> searchAccounts());

        accountTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = accountTable.getSelectedRow() != -1;
                viewDetailsButton.setEnabled(hasSelection);
                if (userService.getCurrentUser().isAdmin() && userService.hasPermission("DELETE_ACCOUNT")) {
                    deleteAccountButton.setEnabled(hasSelection);
                }
            }
        });
    }

    private void loadAccounts() {
        SwingWorker<List<AbstractAccount>, Void> worker = new SwingWorker<List<AbstractAccount>, Void>() {
            @Override
            protected List<AbstractAccount> doInBackground() throws Exception {
                return accountService.getAllAccounts();
            }

            @Override
            protected void done() {
                try {
                    List<AbstractAccount> accounts = get();
                    updateTable(accounts);
                    statusLabel.setText(String.format("Loaded %d accounts", accounts.size()));
                    statusLabel.setForeground(Color.BLUE);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load accounts", e);
                    statusLabel.setText("Failed to load accounts: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        };

        worker.execute();
    }

    private void updateTable(List<AbstractAccount> accounts) {
        tableModel.setRowCount(0);

        for (AbstractAccount account : accounts) {
            Object[] row = {
                account.getAccountId(),
                account.getAccountNumber(),
                account.getCustomerId(),
                getAccountTypeName(account),
                String.format("$%.2f", account.getBalance()),
                account.getStatus(),
                account.getCreatedDate() != null ? account.getCreatedDate().toString() : ""
            };
            tableModel.addRow(row);
        }
    }

    private String getAccountTypeName(AbstractAccount account) {
        String className = account.getClass().getSimpleName();
        return className.replace("Account", "");
    }

    private void searchAccounts() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadAccounts();
            return;
        }

        SwingWorker<List<AbstractAccount>, Void> worker = new SwingWorker<List<AbstractAccount>, Void>() {
            @Override
            protected List<AbstractAccount> doInBackground() throws Exception {
                List<AbstractAccount> allAccounts = accountService.getAllAccounts();
                return allAccounts.stream()
                    .filter(account ->
                        account.getAccountNumber().contains(searchTerm) ||
                        account.getAccountId().toString().contains(searchTerm) ||
                        account.getCustomerId().toString().contains(searchTerm))
                    .toList();
            }

            @Override
            protected void done() {
                try {
                    List<AbstractAccount> accounts = get();
                    updateTable(accounts);
                    statusLabel.setText(String.format("Found %d accounts matching '%s'", accounts.size(), searchTerm));
                    statusLabel.setForeground(Color.BLUE);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to search accounts", e);
                    statusLabel.setText("Search failed: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        };

        worker.execute();
    }

    private void showCreateAccountDialog() {
        CreateAccountDialog dialog = new CreateAccountDialog(
            SwingUtilities.getWindowAncestor(this),
            accountService
        );
        dialog.setVisible(true);

        if (dialog.isAccountCreated()) {
            loadAccounts();
        }
    }

    private void viewAccountDetails() {
        int selectedRow = accountTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an account to view details.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer accountId = (Integer) tableModel.getValueAt(selectedRow, 0);

        try {
            AbstractAccount account = accountService.getAccountById(accountId);
            if (account != null) {
                AccountDetailsDialog dialog = new AccountDetailsDialog(
                    SwingUtilities.getWindowAncestor(this),
                    account,
                    accountService,
                    userService
                );
                dialog.setVisible(true);

                if (dialog.isAccountUpdated()) {
                    loadAccounts();
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load account details", e);
            JOptionPane.showMessageDialog(this,
                "Failed to load account details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedAccountPermanently() {
        int selectedRow = accountTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an account to delete permanently.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Integer accountId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String accountNumber = (String) tableModel.getValueAt(selectedRow, 1);
        String accountType = (String) tableModel.getValueAt(selectedRow, 3);
        String balance = (String) tableModel.getValueAt(selectedRow, 4);

        String[] options = {"Cancel", "I Understand - Delete Permanently"};
        int firstConfirm = JOptionPane.showOptionDialog(this,
            String.format("WARNING: PERMANENT ACCOUNT DELETION\n\n" +
                         "You are about to PERMANENTLY DELETE account:\n" +
                         "Account Number: %s\n" +
                         "Account Type: %s\n" +
                         "Current Balance: %s\n\n" +
                         "This action:\n" +
                         "• Cannot be undone\n" +
                         "• Will remove the account and ALL related transactions\n" +
                         "• Will affect all reports containing this account\n" +
                         "• May impact customer records and audit trails\n\n" +
                         "Are you absolutely sure you want to proceed?",
                         accountNumber, accountType, balance),
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
                boolean success = accountService.deleteAccountPermanently(accountId, true);
                if (success) {
                    loadAccounts();
                    statusLabel.setText(String.format("Account %s permanently deleted", accountNumber));
                    statusLabel.setForeground(Color.RED);

                    LOGGER.warning(String.format("Account permanently deleted by %s: %s (ID: %d)",
                                 userService.getCurrentUser().getUsername(), accountNumber, accountId));

                    JOptionPane.showMessageDialog(this,
                        String.format("Account %s has been permanently deleted from the system.\n" +
                                     "All related transaction data has also been removed.",
                                     accountNumber),
                        "Permanent Account Deletion Complete",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    statusLabel.setText("Failed to permanently delete account - account may have restrictions");
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

    private void showBulkDeleteDialog() {
        BulkDeleteAccountsDialog dialog = new BulkDeleteAccountsDialog(
            SwingUtilities.getWindowAncestor(this),
            accountService,
            userService
        );
        dialog.setVisible(true);

        if (dialog.isAccountsDeleted()) {
            loadAccounts();
            statusLabel.setText("Bulk account deletion completed");
            statusLabel.setForeground(Color.ORANGE);
        }
    }

    private void showCriteriaDeleteDialog() {
        CriteriaDeleteAccountsDialog dialog = new CriteriaDeleteAccountsDialog(
            SwingUtilities.getWindowAncestor(this),
            accountService,
            userService
        );
        dialog.setVisible(true);

        if (dialog.isAccountsDeleted()) {
            loadAccounts();
            statusLabel.setText("Criteria-based account deletion completed");
            statusLabel.setForeground(Color.ORANGE);
        }
    }

    private void showTransferDialog() {
        try {
            com.bankingsystem.services.TransferService transferService =
                new com.bankingsystem.services.TransferService(accountService,
                    new com.bankingsystem.services.TransactionService(), userService);

            AccountTransferDialog dialog = new AccountTransferDialog(
                SwingUtilities.getWindowAncestor(this),
                transferService,
                accountService,
                userService
            );
            dialog.setVisible(true);

            if (dialog.isTransferCompleted()) {
                loadAccounts();
                statusLabel.setText("Account transfer completed successfully");
                statusLabel.setForeground(Color.GREEN);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to open transfer dialog", e);
            statusLabel.setText("Failed to open transfer dialog: " + e.getMessage());
            statusLabel.setForeground(Color.RED);

            JOptionPane.showMessageDialog(this,
                "Failed to open transfer dialog:\n" + e.getMessage(),
                "Transfer Dialog Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
