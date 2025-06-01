package com.bankingsystem.gui;

import com.bankingsystem.models.Transaction;
import com.bankingsystem.models.AbstractAccount;
import com.bankingsystem.models.Customer;
import com.bankingsystem.services.TransactionService;
import com.bankingsystem.services.AccountService;
import com.bankingsystem.services.UserService;
import com.bankingsystem.utils.MockDatabaseUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TransactionPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(TransactionPanel.class.getName());

    private TransactionService transactionService;
    private AccountService accountService;
    private UserService userService;

    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JComboBox<AccountItem> accountFilterComboBox;
    private JButton depositButton;
    private JButton withdrawButton;
    private JButton transferButton;
    private JButton refreshButton;
    private JButton refreshAccountsButton;
    private JButton viewHistoryButton;
    private JLabel statusLabel;

    // Inner class for account dropdown items
    private static class AccountItem {
        private final AbstractAccount account;
        private final String customerName;

        public AccountItem(AbstractAccount account, String customerName) {
            this.account = account;
            this.customerName = customerName;
        }

        public AbstractAccount getAccount() {
            return account;
        }

        @Override
        public String toString() {
            if (account == null) {
                return customerName; // For "All Accounts" or "No accounts available"
            }
            String accountType = account.getClass().getSimpleName().replace("Account", "");
            return String.format("%s - %s - %s - $%.2f",
                account.getAccountNumber(),
                customerName,
                accountType,
                account.getBalance());
        }
    }

    public TransactionPanel(TransactionService transactionService, AccountService accountService, UserService userService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.userService = userService;

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadAccounts();
        loadRecentTransactions();
    }

    private void initializeComponents() {
        String[] columnNames = {"Transaction ID", "Account ID", "Type", "Amount", "Balance After", "Description", "Reference", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionTable = new JTable(tableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setRowHeight(25);
        transactionTable.getTableHeader().setReorderingAllowed(false);

        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(5).setPreferredWidth(200);
        transactionTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        transactionTable.getColumnModel().getColumn(7).setPreferredWidth(150);

        accountFilterComboBox = new JComboBox<>();
        accountFilterComboBox.setPreferredSize(new Dimension(300, 25));
        accountFilterComboBox.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        depositButton = new JButton("Deposit");
        withdrawButton = new JButton("Withdraw");
        transferButton = new JButton("Transfer");
        refreshButton = new JButton("Refresh");
        refreshAccountsButton = new JButton("Refresh Accounts");
        viewHistoryButton = new JButton("View History");

        depositButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        withdrawButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        transferButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        refreshButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        viewHistoryButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        depositButton.setBackground(new Color(34, 139, 34));
        depositButton.setForeground(Color.WHITE);
        depositButton.setFocusPainted(false);

        withdrawButton.setBackground(new Color(220, 20, 60));
        withdrawButton.setForeground(Color.WHITE);
        withdrawButton.setFocusPainted(false);

        transferButton.setBackground(new Color(70, 130, 180));
        transferButton.setForeground(Color.WHITE);
        transferButton.setFocusPainted(false);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        if (!userService.hasPermission("PROCESS_TRANSACTION")) {
            depositButton.setEnabled(false);
            withdrawButton.setEnabled(false);
            transferButton.setEnabled(false);
            depositButton.setToolTipText("You do not have permission to process transactions");
            withdrawButton.setToolTipText("You do not have permission to process transactions");
            transferButton.setToolTipText("You do not have permission to process transactions");
        }

        updateTransactionLimitLabels();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Transaction Management");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel accountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        accountPanel.add(new JLabel("Filter by Account:"));
        accountPanel.add(accountFilterComboBox);
        accountPanel.add(refreshAccountsButton);
        headerPanel.add(accountPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());

        JPanel transactionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        transactionButtonPanel.setBorder(BorderFactory.createTitledBorder("Quick Transactions"));
        transactionButtonPanel.add(depositButton);
        transactionButtonPanel.add(withdrawButton);
        transactionButtonPanel.add(transferButton);

        centerPanel.add(transactionButtonPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Recent Transactions"));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(refreshButton);
        buttonPanel.add(viewHistoryButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.WEST);
        bottomPanel.add(statusLabel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        depositButton.addActionListener(e -> showDepositDialog());
        withdrawButton.addActionListener(e -> showWithdrawDialog());
        transferButton.addActionListener(e -> showTransferDialog());
        refreshButton.addActionListener(e -> loadRecentTransactions());
        refreshAccountsButton.addActionListener(e -> loadAccounts());
        viewHistoryButton.addActionListener(e -> showTransactionHistory());

        accountFilterComboBox.addActionListener(e -> loadAccountTransactions());
    }

    private void loadAccounts() {
        try {
            accountFilterComboBox.removeAllItems();

            // Add "All Accounts" option
            accountFilterComboBox.addItem(new AccountItem(null, "All Accounts"));

            java.util.List<AbstractAccount> accounts = MockDatabaseUtil.getAllAccounts();

            for (AbstractAccount account : accounts) {
                Customer customer = MockDatabaseUtil.findCustomerById(account.getCustomerId());
                String customerName = customer != null ? customer.getFullName() : "Unknown Customer";
                accountFilterComboBox.addItem(new AccountItem(account, customerName));
            }

            if (accounts.isEmpty()) {
                statusLabel.setText("No accounts available. Load sample data or create accounts.");
                statusLabel.setForeground(Color.ORANGE);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load accounts", e);
            statusLabel.setText("Failed to load accounts: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void loadRecentTransactions() {
        SwingWorker<List<Transaction>, Void> worker = new SwingWorker<List<Transaction>, Void>() {
            @Override
            protected List<Transaction> doInBackground() throws Exception {
                List<Transaction> allTransactions = transactionService.getAllTransactions();
                return allTransactions.stream().limit(100).toList();
            }

            @Override
            protected void done() {
                try {
                    List<Transaction> transactions = get();
                    updateTable(transactions);
                    statusLabel.setText(String.format("Loaded %d recent transactions", transactions.size()));
                    statusLabel.setForeground(Color.BLUE);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load transactions", e);
                    statusLabel.setText("Failed to load transactions: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        };

        worker.execute();
    }

    private void loadAccountTransactions() {
        AccountItem selectedItem = (AccountItem) accountFilterComboBox.getSelectedItem();

        if (selectedItem == null || selectedItem.getAccount() == null) {
            // "All Accounts" selected or no selection
            loadRecentTransactions();
            return;
        }

        String accountNumber = selectedItem.getAccount().getAccountNumber();

        SwingWorker<List<Transaction>, Void> worker = new SwingWorker<List<Transaction>, Void>() {
            @Override
            protected List<Transaction> doInBackground() throws Exception {
                return transactionService.getTransactionHistory(accountNumber);
            }

            @Override
            protected void done() {
                try {
                    List<Transaction> transactions = get();
                    updateTable(transactions);
                    statusLabel.setText(String.format("Loaded %d transactions for account %s", transactions.size(), accountNumber));
                    statusLabel.setForeground(Color.BLUE);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to load account transactions", e);
                    statusLabel.setText("Failed to load account transactions: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        };

        worker.execute();
    }

    private void updateTable(List<Transaction> transactions) {
        tableModel.setRowCount(0);

        for (Transaction transaction : transactions) {
            Object[] row = {
                transaction.getTransactionId(),
                transaction.getAccountId(),
                transaction.getTransactionType(),
                String.format("$%.2f", transaction.getAmount()),
                String.format("$%.2f", transaction.getBalanceAfter()),
                transaction.getDescription(),
                transaction.getReferenceNumber(),
                transaction.getCreatedDate() != null ? transaction.getCreatedDate().toString() : ""
            };
            tableModel.addRow(row);
        }
    }

    private void showDepositDialog() {
        DepositDialog dialog = new DepositDialog(
            SwingUtilities.getWindowAncestor(this),
            transactionService,
            userService.getCurrentUser().getUserId()
        );
        dialog.setVisible(true);

        if (dialog.isTransactionCompleted()) {
            loadRecentTransactions();
            statusLabel.setText("Deposit completed successfully");
            statusLabel.setForeground(Color.GREEN);
        }
    }

    private void showWithdrawDialog() {
        WithdrawDialog dialog = new WithdrawDialog(
            SwingUtilities.getWindowAncestor(this),
            transactionService,
            userService.getCurrentUser().getUserId()
        );
        dialog.setVisible(true);

        if (dialog.isTransactionCompleted()) {
            loadRecentTransactions();
            statusLabel.setText("Withdrawal completed successfully");
            statusLabel.setForeground(Color.GREEN);
        }
    }

    private void showTransferDialog() {
        TransferDialog dialog = new TransferDialog(
            SwingUtilities.getWindowAncestor(this),
            transactionService,
            userService.getCurrentUser().getUserId()
        );
        dialog.setVisible(true);

        if (dialog.isTransactionCompleted()) {
            loadRecentTransactions();
            statusLabel.setText("Transfer completed successfully");
            statusLabel.setForeground(Color.GREEN);
        }
    }

    private void showTransactionHistory() {
        AccountItem selectedItem = (AccountItem) accountFilterComboBox.getSelectedItem();

        if (selectedItem == null || selectedItem.getAccount() == null) {
            JOptionPane.showMessageDialog(this,
                "Please select an account to view transaction history.",
                "Account Selection Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String accountNumber = selectedItem.getAccount().getAccountNumber();
        TransactionHistoryDialog dialog = new TransactionHistoryDialog(
            SwingUtilities.getWindowAncestor(this),
            transactionService,
            accountNumber
        );
        dialog.setVisible(true);
    }

    private void updateTransactionLimitLabels() {
        com.bankingsystem.models.User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            String limitText = "";
            if (currentUser.isTeller()) {
                limitText = "Daily Limit: $2,000 | Approval Required: >$1,000";
            } else if (currentUser.isManager()) {
                limitText = "Daily Limit: $10,000 | Approval Required: >$5,000";
            } else if (currentUser.isAdmin()) {
                limitText = "Daily Limit: Unlimited | No Approval Required";
            }

            if (!limitText.isEmpty()) {
                JLabel limitLabel = new JLabel(limitText);
                limitLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
                limitLabel.setForeground(Color.GRAY);

                if (getComponentCount() > 0 && getComponent(0) instanceof JPanel) {
                    JPanel headerPanel = (JPanel) getComponent(0);
                    if (headerPanel.getComponentCount() > 0) {
                        headerPanel.add(limitLabel, BorderLayout.SOUTH);
                    }
                }
            }
        }
    }
}
