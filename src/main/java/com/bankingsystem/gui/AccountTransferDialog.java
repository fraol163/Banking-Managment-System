package com.bankingsystem.gui;

import com.bankingsystem.models.AbstractAccount;
import com.bankingsystem.models.TransferRequest;
import com.bankingsystem.models.TransferResult;
import com.bankingsystem.services.AccountService;
import com.bankingsystem.services.TransferService;
import com.bankingsystem.services.UserService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AccountTransferDialog extends JDialog {
    private static final Logger LOGGER = Logger.getLogger(AccountTransferDialog.class.getName());
    
    private TransferService transferService;
    private AccountService accountService;
    private UserService userService;
    private boolean transferCompleted = false;
    
    private JComboBox<String> fromAccountCombo;
    private JComboBox<String> toAccountCombo;
    private JTextField amountField;
    private JTextField descriptionField;
    private JButton transferButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private JLabel fromBalanceLabel;
    private JLabel toBalanceLabel;
    private JLabel dailyLimitLabel;
    
    public AccountTransferDialog(Window parent, TransferService transferService, 
                               AccountService accountService, UserService userService) {
        super(parent, "Account Transfer", ModalityType.APPLICATION_MODAL);
        this.transferService = transferService;
        this.accountService = accountService;
        this.userService = userService;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadAccounts();
        setupDialog();
    }
    
    private void initializeComponents() {
        fromAccountCombo = new JComboBox<>();
        toAccountCombo = new JComboBox<>();
        amountField = new JTextField(15);
        descriptionField = new JTextField(30);
        
        transferButton = new JButton("Transfer Funds");
        cancelButton = new JButton("Cancel");
        
        fromBalanceLabel = new JLabel("Balance: $0.00");
        toBalanceLabel = new JLabel("Balance: $0.00");
        dailyLimitLabel = new JLabel("Daily Limit: $0.00");
        statusLabel = new JLabel("Enter transfer details");
        
        fromAccountCombo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        toAccountCombo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        amountField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        descriptionField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        transferButton.setBackground(new Color(70, 130, 180));
        transferButton.setForeground(Color.WHITE);
        transferButton.setFocusPainted(false);
        transferButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        cancelButton.setBackground(new Color(128, 128, 128));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        fromBalanceLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        toBalanceLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        dailyLimitLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        
        fromBalanceLabel.setForeground(Color.BLUE);
        toBalanceLabel.setForeground(Color.BLUE);
        dailyLimitLabel.setForeground(Color.ORANGE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Account Transfer - Internal Transfer Between Accounts");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setForeground(new Color(70, 130, 180));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("From Account:"), gbc);
        gbc.gridx = 1;
        formPanel.add(fromAccountCombo, gbc);
        gbc.gridx = 2;
        formPanel.add(fromBalanceLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("To Account:"), gbc);
        gbc.gridx = 1;
        formPanel.add(toAccountCombo, gbc);
        gbc.gridx = 2;
        formPanel.add(toBalanceLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        formPanel.add(amountField, gbc);
        gbc.gridx = 2;
        formPanel.add(dailyLimitLabel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
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
        transferButton.addActionListener(e -> performTransfer());
        cancelButton.addActionListener(e -> dispose());
        
        fromAccountCombo.addActionListener(e -> updateFromAccountInfo());
        toAccountCombo.addActionListener(e -> updateToAccountInfo());
        
        amountField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                validateTransferAmount();
            }
        });
    }
    
    private void loadAccounts() {
        try {
            List<AbstractAccount> accounts = accountService.getAllAccounts();
            
            fromAccountCombo.removeAllItems();
            toAccountCombo.removeAllItems();
            
            for (AbstractAccount account : accounts) {
                if ("Active".equals(account.getStatus())) {
                    String accountDisplay = String.format("%s - %s ($%.2f)", 
                                          account.getAccountNumber(), 
                                          getAccountTypeName(account),
                                          account.getBalance());
                    fromAccountCombo.addItem(accountDisplay);
                    toAccountCombo.addItem(accountDisplay);
                }
            }
            
            if (fromAccountCombo.getItemCount() > 0) {
                updateFromAccountInfo();
                updateToAccountInfo();
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load accounts", e);
            statusLabel.setText("Failed to load accounts: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }
    
    private String getAccountTypeName(AbstractAccount account) {
        String className = account.getClass().getSimpleName();
        return className.replace("Account", "");
    }
    
    private void updateFromAccountInfo() {
        String selectedAccount = (String) fromAccountCombo.getSelectedItem();
        if (selectedAccount != null) {
            String accountNumber = selectedAccount.split(" - ")[0];
            try {
                AbstractAccount account = accountService.getAccountByNumber(accountNumber);
                fromBalanceLabel.setText(String.format("Balance: $%.2f", account.getBalance()));
                dailyLimitLabel.setText(String.format("Daily Limit: $%.2f", account.getDailyWithdrawalLimit()));
            } catch (Exception e) {
                fromBalanceLabel.setText("Balance: Error");
                dailyLimitLabel.setText("Daily Limit: Error");
            }
        }
    }
    
    private void updateToAccountInfo() {
        String selectedAccount = (String) toAccountCombo.getSelectedItem();
        if (selectedAccount != null) {
            String accountNumber = selectedAccount.split(" - ")[0];
            try {
                AbstractAccount account = accountService.getAccountByNumber(accountNumber);
                toBalanceLabel.setText(String.format("Balance: $%.2f", account.getBalance()));
            } catch (Exception e) {
                toBalanceLabel.setText("Balance: Error");
            }
        }
    }
    
    private void validateTransferAmount() {
        try {
            String amountText = amountField.getText().trim();
            if (!amountText.isEmpty()) {
                BigDecimal amount = new BigDecimal(amountText);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    statusLabel.setText("Amount must be greater than zero");
                    statusLabel.setForeground(Color.RED);
                    transferButton.setEnabled(false);
                } else {
                    statusLabel.setText("Enter transfer details");
                    statusLabel.setForeground(Color.BLACK);
                    transferButton.setEnabled(true);
                }
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid amount format");
            statusLabel.setForeground(Color.RED);
            transferButton.setEnabled(false);
        }
    }

    private void performTransfer() {
        if (!validateTransferInputs()) {
            return;
        }

        String fromAccountDisplay = (String) fromAccountCombo.getSelectedItem();
        String toAccountDisplay = (String) toAccountCombo.getSelectedItem();
        String fromAccountNumber = fromAccountDisplay.split(" - ")[0];
        String toAccountNumber = toAccountDisplay.split(" - ")[0];

        try {
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            String description = descriptionField.getText().trim();
            if (description.isEmpty()) {
                description = String.format("Transfer from %s to %s", fromAccountNumber, toAccountNumber);
            }

            if (showTransferConfirmation(fromAccountNumber, toAccountNumber, amount, description)) {
                executeTransfer(fromAccountNumber, toAccountNumber, amount, description);
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid amount format");
            statusLabel.setForeground(Color.RED);
        }
    }

    private boolean validateTransferInputs() {
        if (fromAccountCombo.getSelectedItem() == null) {
            statusLabel.setText("Please select a from account");
            statusLabel.setForeground(Color.RED);
            return false;
        }

        if (toAccountCombo.getSelectedItem() == null) {
            statusLabel.setText("Please select a to account");
            statusLabel.setForeground(Color.RED);
            return false;
        }

        String fromAccountDisplay = (String) fromAccountCombo.getSelectedItem();
        String toAccountDisplay = (String) toAccountCombo.getSelectedItem();
        String fromAccountNumber = fromAccountDisplay.split(" - ")[0];
        String toAccountNumber = toAccountDisplay.split(" - ")[0];

        if (fromAccountNumber.equals(toAccountNumber)) {
            statusLabel.setText("Cannot transfer to the same account");
            statusLabel.setForeground(Color.RED);
            return false;
        }

        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            statusLabel.setText("Please enter transfer amount");
            statusLabel.setForeground(Color.RED);
            return false;
        }

        try {
            BigDecimal amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                statusLabel.setText("Amount must be greater than zero");
                statusLabel.setForeground(Color.RED);
                return false;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid amount format");
            statusLabel.setForeground(Color.RED);
            return false;
        }

        return true;
    }

    private boolean showTransferConfirmation(String fromAccount, String toAccount, BigDecimal amount, String description) {
        try {
            AbstractAccount fromAcct = accountService.getAccountByNumber(fromAccount);
            AbstractAccount toAcct = accountService.getAccountByNumber(toAccount);

            String[] options = {"Cancel", "Confirm Transfer"};
            int firstConfirm = JOptionPane.showOptionDialog(this,
                String.format("TRANSFER CONFIRMATION\n\n" +
                             "From Account: %s (%s)\n" +
                             "Current Balance: $%.2f\n\n" +
                             "To Account: %s (%s)\n" +
                             "Current Balance: $%.2f\n\n" +
                             "Transfer Amount: $%.2f\n" +
                             "Description: %s\n\n" +
                             "After transfer:\n" +
                             "From Account Balance: $%.2f\n" +
                             "To Account Balance: $%.2f\n\n" +
                             "Do you want to proceed with this transfer?",
                             fromAccount, getAccountTypeName(fromAcct), fromAcct.getBalance(),
                             toAccount, getAccountTypeName(toAcct), toAcct.getBalance(),
                             amount, description,
                             fromAcct.getBalance().subtract(amount),
                             toAcct.getBalance().add(amount)),
                "Confirm Transfer",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

            if (firstConfirm != 1) {
                return false;
            }

            int secondConfirm = JOptionPane.showConfirmDialog(this,
                String.format("FINAL CONFIRMATION\n\n" +
                             "Transfer $%.2f from %s to %s?\n\n" +
                             "This action will be recorded in the audit log.",
                             amount, fromAccount, toAccount),
                "Final Transfer Confirmation",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);

            return secondConfirm == JOptionPane.OK_OPTION;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during transfer confirmation", e);
            return false;
        }
    }

    private void executeTransfer(String fromAccount, String toAccount, BigDecimal amount, String description) {
        transferButton.setEnabled(false);
        statusLabel.setText("Processing transfer...");
        statusLabel.setForeground(Color.BLUE);

        SwingWorker<TransferResult, Void> worker = new SwingWorker<TransferResult, Void>() {
            @Override
            protected TransferResult doInBackground() throws Exception {
                TransferRequest request = new TransferRequest(fromAccount, toAccount, amount, description,
                                                            userService.getCurrentUser().getUserId());
                return transferService.performTransfer(request);
            }

            @Override
            protected void done() {
                try {
                    TransferResult result = get();

                    if (result.isSuccess()) {
                        statusLabel.setText("Transfer completed successfully!");
                        statusLabel.setForeground(Color.GREEN);
                        transferCompleted = true;

                        JOptionPane.showMessageDialog(AccountTransferDialog.this,
                            String.format("Transfer Completed Successfully!\n\n" +
                                         "Amount: $%.2f\n" +
                                         "From: %s\n" +
                                         "To: %s\n" +
                                         "Reference: %s\n" +
                                         "Description: %s",
                                         amount, fromAccount, toAccount,
                                         result.getReferenceNumber(), description),
                            "Transfer Successful",
                            JOptionPane.INFORMATION_MESSAGE);

                        loadAccounts();
                        clearForm();

                    } else {
                        statusLabel.setText("Transfer failed: " + result.getMessage());
                        statusLabel.setForeground(Color.RED);

                        JOptionPane.showMessageDialog(AccountTransferDialog.this,
                            "Transfer Failed:\n" + result.getMessage(),
                            "Transfer Error",
                            JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Transfer execution failed", e);
                    statusLabel.setText("Transfer failed: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);

                    JOptionPane.showMessageDialog(AccountTransferDialog.this,
                        "Transfer Failed:\n" + e.getMessage(),
                        "Transfer Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    transferButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void clearForm() {
        amountField.setText("");
        descriptionField.setText("");
        statusLabel.setText("Enter transfer details");
        statusLabel.setForeground(Color.BLACK);
    }

    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(getParent());
        setResizable(false);
    }

    public boolean isTransferCompleted() {
        return transferCompleted;
    }
}
