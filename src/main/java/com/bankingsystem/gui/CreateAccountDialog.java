package com.bankingsystem.gui;

import com.bankingsystem.models.AbstractAccount;
import com.bankingsystem.services.AccountService;
import com.bankingsystem.services.CustomerService;
import com.bankingsystem.utils.ValidationUtil;
import com.bankingsystem.utils.UserFriendlyValidation;
import com.bankingsystem.utils.ErrorHandler;
import com.bankingsystem.utils.MockDatabaseUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CreateAccountDialog extends JDialog {
    private static final Logger LOGGER = Logger.getLogger(CreateAccountDialog.class.getName());
    
    private AccountService accountService;
    private CustomerService customerService;
    private boolean accountCreated = false;
    
    private JTextField customerIdField;
    private JComboBox<String> accountTypeCombo;
    private JTextField initialDepositField;
    private JButton createButton;
    private JButton cancelButton;
    private JButton createCustomerButton;
    private JLabel statusLabel;
    private JLabel availableCustomersLabel;
    
    public CreateAccountDialog(Window parent, AccountService accountService) {
        super(parent, "Create New Account", ModalityType.APPLICATION_MODAL);
        this.accountService = accountService;
        this.customerService = new CustomerService();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupDialog();
    }
    
    private void initializeComponents() {
        customerIdField = new JTextField(15);
        accountTypeCombo = new JComboBox<>(new String[]{"Savings", "Checking", "Business"});
        initialDepositField = new JTextField(15);

        createButton = new JButton("Create Account");
        cancelButton = new JButton("Cancel");
        createCustomerButton = new JButton("Create Customer");
        statusLabel = new JLabel(" ");

        // Create label showing available customer IDs
        updateAvailableCustomersLabel();

        createButton.setBackground(new Color(70, 130, 180));
        createButton.setForeground(Color.WHITE);
        createButton.setFocusPainted(false);

        createCustomerButton.setBackground(new Color(34, 139, 34));
        createCustomerButton.setForeground(Color.WHITE);
        createCustomerButton.setFocusPainted(false);

        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    }

    private void updateAvailableCustomersLabel() {
        java.util.List<Integer> customerIds = MockDatabaseUtil.getAvailableCustomerIds();
        if (customerIds.isEmpty()) {
            availableCustomersLabel = new JLabel("<html><font color='red'>No customers available. Please create customers first.</font></html>");
        } else {
            StringBuilder sb = new StringBuilder("<html><font color='blue'>Available Customer IDs: ");
            for (int i = 0; i < customerIds.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(customerIds.get(i));
            }
            sb.append("</font></html>");
            availableCustomersLabel = new JLabel(sb.toString());
        }
        availableCustomersLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
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
        formPanel.add(new JLabel("Customer ID:"), gbc);
        
        gbc.gridx = 1;
        formPanel.add(customerIdField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Account Type:"), gbc);
        
        gbc.gridx = 1;
        formPanel.add(accountTypeCombo, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Initial Deposit:"), gbc);
        
        gbc.gridx = 1;
        formPanel.add(initialDepositField, gbc);

        // Add available customers info
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(availableCustomersLabel, gbc);

        add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(createCustomerButton);
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        createButton.addActionListener(new CreateAccountActionListener());
        cancelButton.addActionListener(e -> dispose());
        createCustomerButton.addActionListener(e -> showCreateCustomerDialog());
        
        customerIdField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                clearStatus();
            }
        });
        
        initialDepositField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                clearStatus();
                validateInitialDepositRealTime();
            }
        });
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

    private void showInfo(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.BLUE);
    }

    private void validateInitialDepositRealTime() {
        String depositText = initialDepositField.getText().trim();

        if (depositText.isEmpty()) {
            showInfo(UserFriendlyValidation.getInitialDepositLimits());
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(depositText);
            UserFriendlyValidation.ValidationResult result = UserFriendlyValidation.validateInitialDeposit(amount);

            if (!result.isValid()) {
                showError(result.getMessage());
            } else {
                showSuccess("✓ Valid initial deposit amount");
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid number (e.g., 1000.00)");
        }
    }
    
    public boolean isAccountCreated() {
        return accountCreated;
    }

    private void showCreateCustomerDialog() {
        CreateCustomerDialog dialog = new CreateCustomerDialog(this, customerService);
        dialog.setVisible(true);

        if (dialog.isCustomerCreated()) {
            // Refresh the available customers label
            updateAvailableCustomersLabel();
            // Update the layout to reflect changes
            revalidate();
            repaint();
            showSuccess("Customer created! You can now create an account for them.");
        }
    }
    
    private class CreateAccountActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String customerIdText = customerIdField.getText().trim();
            String accountType = (String) accountTypeCombo.getSelectedItem();
            String initialDepositText = initialDepositField.getText().trim();
            
            if (customerIdText.isEmpty()) {
                showError("Please enter a customer ID");
                return;
            }

            // Check if any customers exist
            if (MockDatabaseUtil.getAvailableCustomerIds().isEmpty()) {
                showError("No customers available. Please create customers first.");
                return;
            }
            
            if (initialDepositText.isEmpty()) {
                ErrorHandler.showValidationError(CreateAccountDialog.this,
                    "Initial deposit amount is required.\n\n" + UserFriendlyValidation.getInitialDepositLimits());
                return;
            }
            
            try {
                Integer customerId = Integer.parseInt(customerIdText);

                // Check if customer exists before proceeding
                if (!MockDatabaseUtil.customerExists(customerId)) {
                    java.util.List<Integer> availableIds = MockDatabaseUtil.getAvailableCustomerIds();
                    String message = String.format(
                        "Customer ID %d not found.\n\n" +
                        "Available customer IDs: %s\n\n" +
                        "Please verify the customer ID or create a new customer first.",
                        customerId, availableIds
                    );
                    ErrorHandler.showDataNotFoundError(CreateAccountDialog.this, "Customer", customerIdText);
                    return;
                }

                BigDecimal initialDeposit = new BigDecimal(initialDepositText);

                // Use enhanced validation with user-friendly messages
                UserFriendlyValidation.ValidationResult result = UserFriendlyValidation.validateInitialDeposit(initialDeposit);
                if (!result.isValid()) {
                    ErrorHandler.showValidationError(CreateAccountDialog.this, result.getMessage());
                    return;
                }
                
                createButton.setEnabled(false);
                createButton.setText("Creating...");
                
                SwingWorker<AbstractAccount, Void> worker = new SwingWorker<AbstractAccount, Void>() {
                    @Override
                    protected AbstractAccount doInBackground() throws Exception {
                        return accountService.createAccount(customerId, accountType, initialDeposit);
                    }
                    
                    @Override
                    protected void done() {
                        try {
                            AbstractAccount account = get();
                            showSuccess("Account created successfully: " + account.getAccountNumber());
                            accountCreated = true;
                            
                            Timer timer = new Timer(2000, evt -> dispose());
                            timer.setRepeats(false);
                            timer.start();
                            
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "Failed to create account", ex);
                            String userMessage = ErrorHandler.getUserFriendlyMessage(ex);
                            ErrorHandler.showAccountError(CreateAccountDialog.this,
                                "Failed to create account.\n\n" + userMessage);
                        } finally {
                            createButton.setEnabled(true);
                            createButton.setText("Create Account");
                        }
                    }
                };
                
                worker.execute();
                
            } catch (NumberFormatException ex) {
                String message = "Invalid input format.\n\n";
                if (customerIdText.isEmpty() || !customerIdText.matches("\\d+")) {
                    message += "Customer ID must be a valid number.\n";
                }
                if (!initialDepositText.isEmpty() && !initialDepositText.matches("\\d+(\\.\\d{1,2})?")) {
                    message += "Initial deposit must be a valid amount (e.g., 1000.00).\n";
                }
                message += "\nPlease correct the input and try again.";
                ErrorHandler.showValidationError(CreateAccountDialog.this, message);
            }
        }
    }
}
