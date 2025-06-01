package com.bankingsystem.gui;

import com.bankingsystem.models.Transaction;
import com.bankingsystem.models.TransactionApproval;
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
import java.util.logging.Logger;
import java.util.logging.Level;

public class DepositDialog extends JDialog {
    private static final Logger LOGGER = Logger.getLogger(DepositDialog.class.getName());
    
    private TransactionService transactionService;
    private AccountService accountService;
    private CustomerService customerService;
    private Integer userId;
    private boolean transactionCompleted = false;

    private JComboBox<AccountItem> accountComboBox;
    private JTextField amountField;
    private JTextField descriptionField;
    private JButton depositButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private JButton refreshAccountsButton;
    
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
            String accountType = account.getClass().getSimpleName().replace("Account", "");
            return String.format("%s - %s - %s - $%.2f",
                account.getAccountNumber(),
                customerName,
                accountType,
                account.getBalance());
        }
    }

    public DepositDialog(Window parent, TransactionService transactionService, Integer userId) {
        super(parent, "Deposit Money", ModalityType.APPLICATION_MODAL);
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
        accountComboBox = new JComboBox<>();
        accountComboBox.setPreferredSize(new Dimension(300, 25));

        amountField = new JTextField(15);
        descriptionField = new JTextField(20);

        depositButton = new JButton("Deposit");
        cancelButton = new JButton("Cancel");
        refreshAccountsButton = new JButton("Refresh");
        statusLabel = new JLabel(" ");

        depositButton.setBackground(new Color(34, 139, 34));
        depositButton.setForeground(Color.WHITE);
        depositButton.setFocusPainted(false);

        refreshAccountsButton.setBackground(new Color(70, 130, 180));
        refreshAccountsButton.setForeground(Color.WHITE);
        refreshAccountsButton.setFocusPainted(false);

        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        descriptionField.setText("Cash deposit");
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
        formPanel.add(new JLabel("Select Account:"), gbc);

        gbc.gridx = 1;
        JPanel accountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        accountPanel.add(accountComboBox);
        accountPanel.add(Box.createHorizontalStrut(5));
        accountPanel.add(refreshAccountsButton);
        formPanel.add(accountPanel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Amount:"), gbc);
        
        gbc.gridx = 1;
        formPanel.add(amountField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        formPanel.add(descriptionField, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(depositButton);
        buttonPanel.add(cancelButton);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        depositButton.addActionListener(new DepositActionListener());
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
            accountComboBox.removeAllItems();

            java.util.List<AbstractAccount> accounts = MockDatabaseUtil.getAllAccounts();

            if (accounts.isEmpty()) {
                accountComboBox.addItem(new AccountItem(null, "No accounts available"));
                depositButton.setEnabled(false);
                showError("No accounts available. Please create accounts first or load sample data.");
                return;
            }

            for (AbstractAccount account : accounts) {
                Customer customer = MockDatabaseUtil.findCustomerById(account.getCustomerId());
                String customerName = customer != null ? customer.getFullName() : "Unknown Customer";
                accountComboBox.addItem(new AccountItem(account, customerName));
            }

            depositButton.setEnabled(true);
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
    
    private class DepositActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            AccountItem selectedItem = (AccountItem) accountComboBox.getSelectedItem();
            String amountText = amountField.getText().trim();
            String description = descriptionField.getText().trim();

            if (selectedItem == null || selectedItem.getAccount() == null) {
                ErrorHandler.showValidationError(DepositDialog.this,
                    "Please select an account from the dropdown.\n\n" +
                    "If no accounts are available, please create accounts first or load sample data.");
                return;
            }

            String accountNumber = selectedItem.getAccount().getAccountNumber();

            if (amountText.isEmpty()) {
                ErrorHandler.showValidationError(DepositDialog.this,
                    "Deposit amount is required.\n\n" + UserFriendlyValidation.getTransactionLimits());
                return;
            }
            
            try {
                BigDecimal amount = new BigDecimal(amountText);

                // Use enhanced validation with user-friendly messages
                UserFriendlyValidation.ValidationResult result = UserFriendlyValidation.validateTransactionAmount(amount, "Deposit");
                if (!result.isValid()) {
                    ErrorHandler.showValidationError(DepositDialog.this, result.getMessage());
                    return;
                }
                
                depositButton.setEnabled(false);
                depositButton.setText("Processing...");
                
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        // Use enhanced approval workflow
                        ApprovalWorkflowManager workflowManager = new ApprovalWorkflowManager();
                        ApprovalWorkflowManager.ApprovalResult approvalResult =
                            workflowManager.handleDepositApproval(accountNumber, amount, description, userId, DepositDialog.this);

                        if (!approvalResult.requiresApproval()) {
                            // Process transaction directly
                            Transaction transaction = transactionService.deposit(accountNumber, amount, description, userId);

                            SwingUtilities.invokeLater(() -> {
                                ErrorHandler.showTransactionSuccess(DepositDialog.this, "Deposit",
                                    amount.doubleValue(), transaction.getReferenceNumber());
                                transactionCompleted = true;

                                Timer timer = new Timer(2000, evt -> dispose());
                                timer.setRepeats(false);
                                timer.start();
                            });

                        } else if (approvalResult.canSelfApprove()) {
                            // Self-approved transaction
                            Transaction transaction = transactionService.processApprovedDeposit(approvalResult.getApprovalRequest());

                            SwingUtilities.invokeLater(() -> {
                                ErrorHandler.showTransactionSuccess(DepositDialog.this, "Deposit (Self-Approved)",
                                    amount.doubleValue(), transaction.getReferenceNumber());
                                transactionCompleted = true;

                                Timer timer = new Timer(2000, evt -> dispose());
                                timer.setRepeats(false);
                                timer.start();
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
                            LOGGER.log(Level.SEVERE, "Deposit operation failed", ex);
                            String userMessage = ErrorHandler.getUserFriendlyMessage(ex);
                            ErrorHandler.showTransactionError(DepositDialog.this,
                                "Deposit failed.\n\n" + userMessage);
                        } finally {
                            depositButton.setEnabled(true);
                            depositButton.setText("Deposit");
                        }
                    }
                };
                
                worker.execute();
                
            } catch (NumberFormatException ex) {
                ErrorHandler.showValidationError(DepositDialog.this,
                    "Invalid amount format.\n\n" +
                    "Please enter a valid number (e.g., 1000.00).\n" +
                    "Amount cannot have more than 2 decimal places.");
            }
        }
    }

    private void showApprovalRequired(TransactionApproval approval) {
        String message = String.format(
            "<html><div style='text-align: center;'>" +
            "<b>Manager Approval Required</b><br><br>" +
            "Transaction Amount: $%.2f<br>" +
            "Approval Request ID: %d<br><br>" +
            "Your transaction has been submitted for approval.<br>" +
            "A manager will need to approve this transaction<br>" +
            "before it can be processed.<br><br>" +
            "<i>Status: %s</i>" +
            "</div></html>",
            approval.getAmount(),
            approval.getApprovalId(),
            approval.getApprovalStatus()
        );

        statusLabel.setText(message);
        statusLabel.setForeground(new Color(255, 140, 0)); // Orange color

        // Change button text to indicate approval is pending
        depositButton.setText("Approval Pending");
        depositButton.setEnabled(false);

        // Auto-close after showing approval message
        Timer timer = new Timer(5000, evt -> dispose());
        timer.setRepeats(false);
        timer.start();

        LOGGER.info(String.format("Approval request created: ID %d for $%.2f",
                                approval.getApprovalId(), approval.getAmount()));
    }
}
