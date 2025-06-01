package com.bankingsystem.gui;

import com.bankingsystem.models.Customer;
import com.bankingsystem.services.CustomerService;
import com.bankingsystem.utils.ValidationUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CreateCustomerDialog extends JDialog {
    private static final Logger LOGGER = Logger.getLogger(CreateCustomerDialog.class.getName());
    
    private CustomerService customerService;
    private boolean customerCreated = false;
    
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;
    private JTextField dateOfBirthField;
    private JTextField ssnField;
    private JButton createButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    public CreateCustomerDialog(Window parent, CustomerService customerService) {
        super(parent, "Create New Customer", ModalityType.APPLICATION_MODAL);
        this.customerService = customerService;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupDialog();
    }
    
    private void initializeComponents() {
        firstNameField = new JTextField(15);
        lastNameField = new JTextField(15);
        emailField = new JTextField(15);
        phoneField = new JTextField(15);
        addressField = new JTextField(15);
        dateOfBirthField = new JTextField(15);
        ssnField = new JTextField(15);
        
        createButton = new JButton("Create Customer");
        cancelButton = new JButton("Cancel");
        statusLabel = new JLabel(" ");
        
        createButton.setBackground(new Color(70, 130, 180));
        createButton.setForeground(Color.WHITE);
        createButton.setFocusPainted(false);
        
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        // Add placeholder text hints
        dateOfBirthField.setToolTipText("Format: YYYY-MM-DD (e.g., 1990-01-15)");
        phoneField.setToolTipText("Format: (555) 123-4567");
        ssnField.setToolTipText("Format: 123-45-6789");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // First Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(firstNameField, gbc);
        
        // Last Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(lastNameField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        formPanel.add(phoneField, gbc);
        
        // Address
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        formPanel.add(addressField, gbc);
        
        // Date of Birth
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Date of Birth:"), gbc);
        gbc.gridx = 1;
        formPanel.add(dateOfBirthField, gbc);
        
        // SSN
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("SSN:"), gbc);
        gbc.gridx = 1;
        formPanel.add(ssnField, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        createButton.addActionListener(new CreateCustomerActionListener());
        cancelButton.addActionListener(e -> dispose());
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
    
    public boolean isCustomerCreated() {
        return customerCreated;
    }
    
    private class CreateCustomerActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            String dateOfBirthText = dateOfBirthField.getText().trim();
            String ssn = ssnField.getText().trim();
            
            // Validation
            if (firstName.isEmpty() || lastName.isEmpty()) {
                showError("First name and last name are required");
                return;
            }
            
            if (email.isEmpty() || !email.contains("@")) {
                showError("Valid email address is required");
                return;
            }
            
            if (phone.isEmpty()) {
                showError("Phone number is required");
                return;
            }
            
            if (address.isEmpty()) {
                showError("Address is required");
                return;
            }
            
            if (dateOfBirthText.isEmpty()) {
                showError("Date of birth is required (YYYY-MM-DD)");
                return;
            }
            
            if (ssn.isEmpty()) {
                showError("SSN is required");
                return;
            }
            
            try {
                LocalDate dateOfBirth = LocalDate.parse(dateOfBirthText, DateTimeFormatter.ISO_LOCAL_DATE);
                
                createButton.setEnabled(false);
                createButton.setText("Creating...");
                
                SwingWorker<Customer, Void> worker = new SwingWorker<Customer, Void>() {
                    @Override
                    protected Customer doInBackground() throws Exception {
                        return customerService.createCustomer(firstName, lastName, email, phone, address, dateOfBirth, ssn);
                    }
                    
                    @Override
                    protected void done() {
                        try {
                            Customer customer = get();
                            showSuccess("Customer created successfully! ID: " + customer.getCustomerId());
                            customerCreated = true;
                            
                            Timer timer = new Timer(2000, evt -> dispose());
                            timer.setRepeats(false);
                            timer.start();
                            
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "Failed to create customer", ex);
                            showError("Failed to create customer: " + ex.getMessage());
                        } finally {
                            createButton.setEnabled(true);
                            createButton.setText("Create Customer");
                        }
                    }
                };
                
                worker.execute();
                
            } catch (DateTimeParseException ex) {
                showError("Invalid date format. Use YYYY-MM-DD (e.g., 1990-01-15)");
            } catch (Exception ex) {
                showError("Invalid input: " + ex.getMessage());
            }
        }
    }
}
