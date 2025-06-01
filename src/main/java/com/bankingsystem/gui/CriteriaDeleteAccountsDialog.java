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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CriteriaDeleteAccountsDialog extends JDialog {
    private static final Logger LOGGER = Logger.getLogger(CriteriaDeleteAccountsDialog.class.getName());

    private AccountService accountService;
    private UserService userService;
    private boolean accountsDeleted = false;

    private JComboBox<String> statusComboBox;
    private JComboBox<String> accountTypeComboBox;
    private JTextField minBalanceField;
    private JTextField maxBalanceField;
    private JTextField startDateField;
    private JTextField endDateField;
    private JButton previewButton;
    private JButton deleteButton;
    private JButton cancelButton;

    private JTable previewTable;
    private DefaultTableModel previewTableModel;
    private JLabel statusLabel;
    private JProgressBar progressBar;

    public CriteriaDeleteAccountsDialog(Window parent, AccountService accountService, UserService userService) {
        super(parent, "Delete Accounts by Criteria", ModalityType.APPLICATION_MODAL);
        this.accountService = accountService;
        this.userService = userService;

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupDialog();
    }

    private void initializeComponents() {
        statusComboBox = new JComboBox<>(new String[]{"Any", "Active", "Closed", "Suspended"});
        accountTypeComboBox = new JComboBox<>(new String[]{"Any", "Savings", "Checking", "Business"});

        minBalanceField = new JTextField("0.00", 10);
        maxBalanceField = new JTextField("999999.99", 10);

        LocalDateTime now = LocalDateTime.now();
        startDateField = new JTextField(now.minusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE), 12);
        endDateField = new JTextField(now.format(DateTimeFormatter.ISO_LOCAL_DATE), 12);

        previewButton = new JButton("Preview Matching Accounts");
        deleteButton = new JButton("Delete Matching Accounts");
        cancelButton = new JButton("Cancel");

        previewButton.setBackground(new Color(70, 130, 180));
        previewButton.setForeground(Color.WHITE);
        previewButton.setFocusPainted(false);

        deleteButton.setBackground(new Color(220, 20, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setEnabled(false);

        String[] columnNames = {"Account ID", "Account Number", "Customer ID", "Type", "Balance", "Status", "Created Date"};
        previewTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        previewTable = new JTable(previewTableModel);
        previewTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        previewTable.setRowHeight(25);
        previewTable.getTableHeader().setReorderingAllowed(false);

        statusLabel = new JLabel("Configure criteria and click Preview to see matching accounts");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Delete Accounts by Criteria - Configure Deletion Filters");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setForeground(Color.RED);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JLabel warningLabel = new JLabel("WARNING: This will permanently delete accounts matching the specified criteria!");
        warningLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        warningLabel.setForeground(Color.RED);
        headerPanel.add(warningLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        JPanel criteriaPanel = new JPanel(new GridBagLayout());
        criteriaPanel.setBorder(BorderFactory.createTitledBorder("Deletion Criteria"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        criteriaPanel.add(new JLabel("Account Status:"), gbc);
        gbc.gridx = 1;
        criteriaPanel.add(statusComboBox, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        criteriaPanel.add(new JLabel("Account Type:"), gbc);
        gbc.gridx = 3;
        criteriaPanel.add(accountTypeComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        criteriaPanel.add(new JLabel("Min Balance:"), gbc);
        gbc.gridx = 1;
        criteriaPanel.add(minBalanceField, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        criteriaPanel.add(new JLabel("Max Balance:"), gbc);
        gbc.gridx = 3;
        criteriaPanel.add(maxBalanceField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        criteriaPanel.add(new JLabel("Created From:"), gbc);
        gbc.gridx = 1;
        criteriaPanel.add(startDateField, gbc);

        gbc.gridx = 2; gbc.gridy = 2;
        criteriaPanel.add(new JLabel("Created To:"), gbc);
        gbc.gridx = 3;
        criteriaPanel.add(endDateField, gbc);

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("Matching Accounts Preview"));

        JScrollPane scrollPane = new JScrollPane(previewTable);
        scrollPane.setPreferredSize(new Dimension(700, 300));
        previewPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(previewButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(criteriaPanel, BorderLayout.NORTH);
        centerPanel.add(previewPanel, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(progressBar, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        previewButton.addActionListener(e -> previewMatchingAccounts());
        deleteButton.addActionListener(e -> deleteMatchingAccounts());
        cancelButton.addActionListener(e -> dispose());
    }

    private void previewMatchingAccounts() {
        SwingWorker<List<AbstractAccount>, Void> worker = new SwingWorker<List<AbstractAccount>, Void>() {
            @Override
            protected List<AbstractAccount> doInBackground() throws Exception {
                return getMatchingAccounts();
            }

            @Override
            protected void done() {
                try {
                    List<AbstractAccount> matchingAccounts = get();
                    updatePreviewTable(matchingAccounts);

                    deleteButton.setEnabled(matchingAccounts.size() > 0);

                    if (matchingAccounts.size() > 0) {
                        statusLabel.setText(String.format("Found %d accounts matching criteria - Review and click Delete to proceed", matchingAccounts.size()));
                        statusLabel.setForeground(Color.ORANGE);
                    } else {
                        statusLabel.setText("No accounts match the specified criteria");
                        statusLabel.setForeground(Color.GRAY);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to preview matching accounts", e);
                    statusLabel.setText("Failed to preview accounts: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        };

        worker.execute();
    }

    private List<AbstractAccount> getMatchingAccounts() throws SQLException {
        List<AbstractAccount> allAccounts = accountService.getAllAccounts();
        List<AbstractAccount> matchingAccounts = new ArrayList<>();

        String selectedStatus = (String) statusComboBox.getSelectedItem();
        String selectedType = (String) accountTypeComboBox.getSelectedItem();

        BigDecimal minBalance;
        BigDecimal maxBalance;

        try {
            minBalance = new BigDecimal(minBalanceField.getText().trim());
            maxBalance = new BigDecimal(maxBalanceField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid balance format. Please enter valid decimal numbers.");
        }

        for (AbstractAccount account : allAccounts) {
            if (matchesCriteria(account, selectedStatus, selectedType, minBalance, maxBalance)) {
                matchingAccounts.add(account);
            }
        }

        return matchingAccounts;
    }

    private boolean matchesCriteria(AbstractAccount account, String status, String type, BigDecimal minBalance, BigDecimal maxBalance) {
        if (!"Any".equals(status) && !account.getStatus().equals(status)) {
            return false;
        }

        if (!"Any".equals(type)) {
            String accountTypeName = getAccountTypeName(account);
            if (!accountTypeName.equals(type)) {
                return false;
            }
        }

        if (account.getBalance().compareTo(minBalance) < 0 || account.getBalance().compareTo(maxBalance) > 0) {
            return false;
        }

        return true;
    }

    private String getAccountTypeName(AbstractAccount account) {
        String className = account.getClass().getSimpleName();
        return className.replace("Account", "");
    }

    private void updatePreviewTable(List<AbstractAccount> accounts) {
        previewTableModel.setRowCount(0);

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
            previewTableModel.addRow(row);
        }
    }

    private void deleteMatchingAccounts() {
        if (previewTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                "No accounts to delete. Please preview matching accounts first.",
                "No Accounts",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Integer> accountIds = new ArrayList<>();
        StringBuilder accountDetails = new StringBuilder();

        for (int i = 0; i < previewTableModel.getRowCount(); i++) {
            Integer accountId = (Integer) previewTableModel.getValueAt(i, 0);
            String accountNumber = (String) previewTableModel.getValueAt(i, 1);
            String accountType = (String) previewTableModel.getValueAt(i, 3);
            String balance = (String) previewTableModel.getValueAt(i, 4);

            accountIds.add(accountId);
            accountDetails.append(String.format("• %s (%s) - %s\n", accountNumber, accountType, balance));
        }

        String criteriaDescription = buildCriteriaDescription();

        String[] options = {"Cancel", "I Understand - Delete All Matching"};
        int firstConfirm = JOptionPane.showOptionDialog(this,
            String.format("WARNING: CRITERIA-BASED PERMANENT ACCOUNT DELETION\n\n" +
                         "You are about to PERMANENTLY DELETE %d accounts matching:\n%s\n" +
                         "Accounts to be deleted:\n%s\n" +
                         "This action:\n" +
                         "• Cannot be undone\n" +
                         "• Will remove ALL matching accounts and their transactions\n" +
                         "• Will affect all reports containing these accounts\n" +
                         "• May impact customer records and audit trails\n\n" +
                         "Are you absolutely sure you want to proceed?",
                         accountIds.size(), criteriaDescription, accountDetails.toString()),
            "Confirm Criteria-Based Permanent Deletion",
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
                         "Permanently delete %d accounts matching the specified criteria?",
                         accountIds.size()),
            "Final Confirmation Required",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.ERROR_MESSAGE);

        if (secondConfirm == JOptionPane.OK_OPTION) {
            performCriteriaDeletion(accountIds, criteriaDescription);
        }
    }

    private String buildCriteriaDescription() {
        StringBuilder criteria = new StringBuilder();

        String status = (String) statusComboBox.getSelectedItem();
        if (!"Any".equals(status)) {
            criteria.append("Status: ").append(status).append("; ");
        }

        String type = (String) accountTypeComboBox.getSelectedItem();
        if (!"Any".equals(type)) {
            criteria.append("Type: ").append(type).append("; ");
        }

        criteria.append("Balance: $").append(minBalanceField.getText())
                .append(" - $").append(maxBalanceField.getText()).append("; ");

        criteria.append("Created: ").append(startDateField.getText())
                .append(" to ").append(endDateField.getText());

        return criteria.toString();
    }

    private void performCriteriaDeletion(List<Integer> accountIds, String criteriaDescription) {
        progressBar.setVisible(true);
        progressBar.setMaximum(accountIds.size());
        progressBar.setValue(0);
        progressBar.setString("Preparing criteria-based deletion...");

        previewButton.setEnabled(false);
        deleteButton.setEnabled(false);

        SwingWorker<Integer, String> worker = new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() throws Exception {
                int deletedCount = 0;
                int currentIndex = 0;

                for (Integer accountId : accountIds) {
                    currentIndex++;

                    try {
                        AbstractAccount account = accountService.getAccountById(accountId);
                        if (account != null) {
                            publish(String.format("Deleting account %s (%d/%d)...",
                                   account.getAccountNumber(), currentIndex, accountIds.size()));

                            boolean success = accountService.deleteAccountPermanently(accountId, true);
                            if (success) {
                                deletedCount++;
                                LOGGER.warning(String.format("Criteria deletion: Account %s permanently deleted by %s (Criteria: %s)",
                                             account.getAccountNumber(), userService.getCurrentUser().getUsername(), criteriaDescription));
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Failed to delete account ID: " + accountId, e);
                    }

                    final int index = currentIndex;
                    SwingUtilities.invokeLater(() -> progressBar.setValue(index));

                    Thread.sleep(150);
                }

                return deletedCount;
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    progressBar.setString(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                try {
                    int deletedCount = get();

                    progressBar.setString("Criteria-based deletion completed");
                    statusLabel.setText(String.format("Successfully deleted %d out of %d matching accounts",
                                       deletedCount, accountIds.size()));
                    statusLabel.setForeground(deletedCount > 0 ? Color.GREEN : Color.RED);

                    accountsDeleted = deletedCount > 0;

                    JOptionPane.showMessageDialog(CriteriaDeleteAccountsDialog.this,
                        String.format("Criteria-based deletion completed.\n\n" +
                                     "Criteria: %s\n" +
                                     "Successfully deleted: %d accounts\n" +
                                     "Total matching: %d accounts",
                                     criteriaDescription, deletedCount, accountIds.size()),
                        "Criteria-Based Deletion Complete",
                        JOptionPane.INFORMATION_MESSAGE);

                    if (deletedCount > 0) {
                        previewMatchingAccounts();
                    }

                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Criteria-based deletion failed", e);
                    statusLabel.setText("Criteria-based deletion failed: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);

                    JOptionPane.showMessageDialog(CriteriaDeleteAccountsDialog.this,
                        "Criteria-based deletion failed:\n" + e.getMessage(),
                        "Deletion Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setVisible(false);
                    previewButton.setEnabled(true);
                    deleteButton.setEnabled(previewTableModel.getRowCount() > 0);
                }
            }
        };

        worker.execute();
    }

    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(getParent());
        setResizable(true);
    }

    public boolean isAccountsDeleted() {
        return accountsDeleted;
    }
}
