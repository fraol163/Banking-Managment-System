package com.bankingsystem.gui;

import com.bankingsystem.models.Transaction;
import com.bankingsystem.models.AbstractAccount;
import com.bankingsystem.models.Customer;
import com.bankingsystem.services.TransactionService;
import com.bankingsystem.services.AccountService;
import com.bankingsystem.services.CustomerService;
import com.bankingsystem.utils.ValidationUtil;
import com.bankingsystem.utils.UserFriendlyValidation;
import com.bankingsystem.utils.ErrorHandler;
import com.bankingsystem.utils.ApprovalWorkflowManager;
import com.bankingsystem.utils.MockDatabaseUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TransferDialog extends JDialog {
    private static final Logger LOGGER = Logger.getLogger(TransferDialog.class.getName());
    
    private TransactionService transactionService;
    private AccountService accountService;
    private CustomerService customerService;
    private Integer userId;
    private boolean transactionCompleted = false;

    private JComboBox<AccountItem> fromAccountComboBox;
    private JComboBox<AccountItem> toAccountComboBox;
    private JTextField amountField;
    private JTextField descriptionField;
    private JButton transferButton;
    private JButton cancelButton;
    private JButton refreshAccountsButton;
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
                return customerName; // For "No accounts available" message
            }
            String accountType = account.getClass().getSimpleName().replace("Account", "");
            return String.format("%s - %s - %s - $%.2f",
                account.getAccountNumber(),
                customerName,
                accountType,
                account.getBalance());
        }
    }
    
    public TransferDialog(Window parent, TransactionService transactionService, Integer userId) {
        super(parent, "Transfer Money", ModalityType.APPLICATION_MODAL);
        this.transactionService = transactionService;
        this.userId = userId;
        this.accountService = new AccountService();
        this.customerService = new CustomerService();

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadAccounts();
        setupDialog();
    }
    
    private void initializeComponents() {
        fromAccountComboBox = new JComboBox<>();
        fromAccountComboBox.setPreferredSize(new Dimension(300, 25));

        toAccountComboBox = new JComboBox<>();
        toAccountComboBox.setPreferredSize(new Dimension(300, 25));

        amountField = new JTextField(15);
        descriptionField = new JTextField(20);

        transferButton = new JButton("Transfer");
        cancelButton = new JButton("Cancel");
        refreshAccountsButton = new JButton("Refresh");
        statusLabel = new JLabel(" ");

        transferButton.setBackground(new Color(70, 130, 180));
        transferButton.setForeground(Color.WHITE);
        transferButton.setFocusPainted(false);

        refreshAccountsButton.setBackground(new Color(70, 130, 180));
        refreshAccountsButton.setForeground(Color.WHITE);
        refreshAccountsButton.setFocusPainted(false);

        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        descriptionField.setText("Account transfer");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("From Account:"), gbc);

        gbc.gridx = 1;
        JPanel fromAccountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fromAccountPanel.add(fromAccountComboBox);
        fromAccountPanel.add(Box.createHorizontalStrut(5));
        fromAccountPanel.add(refreshAccountsButton);
        formPanel.add(fromAccountPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("To Account:"), gbc);

        gbc.gridx = 1;
        formPanel.add(toAccountComboBox, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Amount:"), gbc);
        
        gbc.gridx = 1;
        formPanel.add(amountField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        formPanel.add(descriptionField, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(transferButton);
        buttonPanel.add(cancelButton);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        transferButton.addActionListener(new TransferActionListener());
        cancelButton.addActionListener(e -> dispose());
        refreshAccountsButton.addActionListener(e -> loadAccounts());

        amountField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                clearStatus();
            }
        });
    }

    private void loadAccounts() {
        try {
            fromAccountComboBox.removeAllItems();
            toAccountComboBox.removeAllItems();

            java.util.List<AbstractAccount> accounts = MockDatabaseUtil.getAllAccounts();

            if (accounts.isEmpty()) {
                fromAccountComboBox.addItem(new AccountItem(null, "No accounts available"));
                toAccountComboBox.addItem(new AccountItem(null, "No accounts available"));
                transferButton.setEnabled(false);
                showError("No accounts available. Please create accounts first or load sample data.");
                return;
            }

            for (AbstractAccount account : accounts) {
                Customer customer = MockDatabaseUtil.findCustomerById(account.getCustomerId());
                String customerName = customer != null ? customer.getFullName() : "Unknown Customer";
                AccountItem item = new AccountItem(account, customerName);
                fromAccountComboBox.addItem(item);
                toAccountComboBox.addItem(item);
            }

            transferButton.setEnabled(true);
            clearStatus();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load accounts", e);
            showError("Failed to load accounts: " + e.getMessage());
        }
    }
    
    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(getParent());
        setResizable(false);
    }
    
    private void clearStatus() {
        statusLabel.setText(" ");
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.RED);
    }
    
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.GREEN);
    }
    
    public boolean isTransactionCompleted() {
        return transactionCompleted;
    }
    
    private class TransferActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            AccountItem fromItem = (AccountItem) fromAccountComboBox.getSelectedItem();
            AccountItem toItem = (AccountItem) toAccountComboBox.getSelectedItem();
            String amountText = amountField.getText().trim();
            String description = descriptionField.getText().trim();

            if (fromItem == null || fromItem.getAccount() == null) {
                ErrorHandler.showValidationError(TransferDialog.this,
                    "Please select a source account from the dropdown.\n\n" +
                    "If no accounts are available, please create accounts first or load sample data.");
                return;
            }

            if (toItem == null || toItem.getAccount() == null) {
                ErrorHandler.showValidationError(TransferDialog.this,
                    "Please select a destination account from the dropdown.\n\n" +
                    "If no accounts are available, please create accounts first or load sample data.");
                return;
            }

            String fromAccount = fromItem.getAccount().getAccountNumber();
            String toAccount = toItem.getAccount().getAccountNumber();

            if (fromAccount.equals(toAccount)) {
                ErrorHandler.showValidationError(TransferDialog.this,
                    "Source and destination accounts cannot be the same.\n\n" +
                    "Please select different accounts for the transfer.\n\n" +
                    "Source account: " + fromAccount + "\n" +
                    "Destination account: " + toAccount);
                return;
            }

            if (amountText.isEmpty()) {
                ErrorHandler.showValidationError(TransferDialog.this,
                    "Transfer amount is required.\n\n" + UserFriendlyValidation.getTransferLimits());
                return;
            }

            try {
                BigDecimal amount = new BigDecimal(amountText);

                // Use enhanced validation with user-friendly messages
                UserFriendlyValidation.ValidationResult result = UserFriendlyValidation.validateTransferAmount(amount);
                if (!result.isValid()) {
                    ErrorHandler.showValidationError(TransferDialog.this, result.getMessage());
                    return;
                }
                
                transferButton.setEnabled(false);
                transferButton.setText("Processing...");
                
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        // Use enhanced approval workflow
                        ApprovalWorkflowManager workflowManager = new ApprovalWorkflowManager();
                        ApprovalWorkflowManager.ApprovalResult approvalResult =
                            workflowManager.handleTransferApproval(fromAccount, toAccount, amount, description, userId, TransferDialog.this);

                        if (!approvalResult.requiresApproval()) {
                            // Process transaction directly
                            List<Transaction> transactions = transactionService.transfer(fromAccount, toAccount, amount, description, userId);

                            SwingUtilities.invokeLater(() -> {
                                if (!transactions.isEmpty()) {
                                    ErrorHandler.showTransactionSuccess(TransferDialog.this, "Transfer",
                                        amount.doubleValue(), transactions.get(0).getReferenceNumber());
                                    transactionCompleted = true;

                                    Timer timer = new Timer(2000, evt -> dispose());
                                    timer.setRepeats(false);
                                    timer.start();
                                }
                            });

                        } else if (approvalResult.canSelfApprove()) {
                            // Self-approved transaction - process transfer
                            List<Transaction> transactions = transactionService.transfer(fromAccount, toAccount, amount, description, userId);

                            SwingUtilities.invokeLater(() -> {
                                if (!transactions.isEmpty()) {
                                    ErrorHandler.showTransactionSuccess(TransferDialog.this, "Transfer (Self-Approved)",
                                        amount.doubleValue(), transactions.get(0).getReferenceNumber());
                                    transactionCompleted = true;

                                    Timer timer = new Timer(2000, evt -> dispose());
                                    timer.setRepeats(false);
                                    timer.start();
                                }
                            });
                        }
                        // If approval is required and cannot self-approve, the workflow manager already showed the notification

                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get(); // Check for exceptions
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "Transfer operation failed", ex);

                            // Handle specific transfer exceptions with user-friendly messages
                            String userMessage;
                            if (ex.getMessage() != null && ex.getMessage().contains("requires manager approval")) {
                                // This should be handled by the approval workflow, but just in case
                                userMessage = "This transfer requires manager approval due to the amount or your permission level.\n\n" +
                                            "An approval request should have been created automatically.";
                            } else if (ex.getMessage() != null && ex.getMessage().contains("Insufficient funds")) {
                                userMessage = "Insufficient funds in the source account for this transfer.\n\n" +
                                            "Please check the account balance and try again with a smaller amount.";
                            } else {
                                userMessage = ErrorHandler.getUserFriendlyMessage(ex);
                            }

                            ErrorHandler.showTransactionError(TransferDialog.this,
                                "Transfer failed.\n\n" + userMessage);
                        } finally {
                            transferButton.setEnabled(true);
                            transferButton.setText("Transfer");
                        }
                    }
                };
                
                worker.execute();
                
            } catch (NumberFormatException ex) {
                ErrorHandler.showValidationError(TransferDialog.this,
                    "Invalid amount format.\n\n" +
                    "Please enter a valid number (e.g., 1500.00).\n" +
                    "Amount cannot have more than 2 decimal places.");
            }
        }
    }
}
