package com.bankingsystem.gui;

import com.bankingsystem.config.AppConfig;
import com.bankingsystem.models.User;
import com.bankingsystem.services.UserService;
import com.bankingsystem.utils.ValidationUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class EditUserDialog extends JDialog {
    private static final Logger LOGGER = Logger.getLogger(EditUserDialog.class.getName());
    
    private User user;
    private UserService userService;
    private boolean userUpdated = false;
    
    private JTextField usernameField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JComboBox<String> roleComboBox;
    private JCheckBox activeCheckBox;
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    public EditUserDialog(Window parent, User user, UserService userService) {
        super(parent, "Edit User", ModalityType.APPLICATION_MODAL);
        this.user = user;
        this.userService = userService;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        populateFields();
        setupDialog();
    }
    
    private void initializeComponents() {
        usernameField = new JTextField(20);
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        emailField = new JTextField(20);
        
        String[] roles = {AppConfig.ROLE_ADMIN, AppConfig.ROLE_MANAGER, AppConfig.ROLE_TELLER};
        roleComboBox = new JComboBox<>(roles);
        
        activeCheckBox = new JCheckBox("Active User");
        
        saveButton = new JButton("Save Changes");
        cancelButton = new JButton("Cancel");
        
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        setupPermissionBasedAccess();
    }
    
    private void setupPermissionBasedAccess() {
        User currentUser = userService.getCurrentUser();
        
        if (!currentUser.isAdmin()) {
            roleComboBox.setEnabled(false);
            roleComboBox.setToolTipText("Only Admin users can change user roles");
            
            if (!currentUser.hasPermission(AppConfig.PERMISSION_UPDATE_USER)) {
                usernameField.setEditable(false);
                firstNameField.setEditable(false);
                lastNameField.setEditable(false);
                emailField.setEditable(false);
                activeCheckBox.setEnabled(false);
                saveButton.setEnabled(false);
                saveButton.setToolTipText("You do not have permission to edit users");
            }
        }
        
        if (user.getUserId().equals(currentUser.getUserId())) {
            activeCheckBox.setEnabled(false);
            activeCheckBox.setToolTipText("Cannot deactivate your own account");
            
            if (!currentUser.isAdmin()) {
                roleComboBox.setEnabled(false);
                roleComboBox.setToolTipText("Cannot change your own role");
            }
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(firstNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(lastNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(roleComboBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        mainPanel.add(activeCheckBox, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.WEST);
        add(statusPanel, BorderLayout.NORTH);
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveUser();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    private void populateFields() {
        usernameField.setText(user.getUsername());
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());
        roleComboBox.setSelectedItem(user.getRole());
        activeCheckBox.setSelected(user.isActive());
    }
    
    private void saveUser() {
        try {
            if (!validateInput()) {
                return;
            }
            
            String originalUsername = user.getUsername();
            String originalRole = user.getRole();
            
            user.setUsername(usernameField.getText().trim());
            user.setFirstName(firstNameField.getText().trim());
            user.setLastName(lastNameField.getText().trim());
            user.setEmail(emailField.getText().trim());
            user.setRole((String) roleComboBox.getSelectedItem());
            user.setActive(activeCheckBox.isSelected());
            
            User updatedUser = userService.updateUser(user);
            
            if (updatedUser != null) {
                userUpdated = true;
                statusLabel.setText("User updated successfully");
                statusLabel.setForeground(Color.GREEN);
                
                String changes = buildChangeLog(originalUsername, originalRole);
                LOGGER.info(String.format("User updated by %s: %s", 
                           userService.getCurrentUser().getUsername(), changes));
                
                JOptionPane.showMessageDialog(this,
                    "User information has been updated successfully.",
                    "Update Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                
                dispose();
            } else {
                statusLabel.setText("Failed to update user");
                statusLabel.setForeground(Color.RED);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while updating user", e);
            statusLabel.setText("Database error: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            
            JOptionPane.showMessageDialog(this,
                "Database error occurred while updating user:\n" + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
                
        } catch (IllegalArgumentException e) {
            statusLabel.setText("Validation error: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            
            JOptionPane.showMessageDialog(this,
                "Validation error:\n" + e.getMessage(),
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
                
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while updating user", e);
            statusLabel.setText("Unexpected error: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            
            JOptionPane.showMessageDialog(this,
                "An unexpected error occurred:\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateInput() {
        String username = usernameField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        
        if (username.isEmpty()) {
            showValidationError("Username cannot be empty");
            usernameField.requestFocus();
            return false;
        }
        
        if (!ValidationUtil.isValidUsername(username)) {
            showValidationError("Username must be 3-20 characters, alphanumeric and underscores only");
            usernameField.requestFocus();
            return false;
        }
        
        if (firstName.isEmpty()) {
            showValidationError("First name cannot be empty");
            firstNameField.requestFocus();
            return false;
        }
        
        if (!ValidationUtil.isValidName(firstName)) {
            showValidationError("First name contains invalid characters");
            firstNameField.requestFocus();
            return false;
        }
        
        if (lastName.isEmpty()) {
            showValidationError("Last name cannot be empty");
            lastNameField.requestFocus();
            return false;
        }
        
        if (!ValidationUtil.isValidName(lastName)) {
            showValidationError("Last name contains invalid characters");
            lastNameField.requestFocus();
            return false;
        }
        
        if (email.isEmpty()) {
            showValidationError("Email cannot be empty");
            emailField.requestFocus();
            return false;
        }
        
        if (!ValidationUtil.isValidEmail(email)) {
            showValidationError("Invalid email format");
            emailField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showValidationError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.RED);
    }
    
    private String buildChangeLog(String originalUsername, String originalRole) {
        StringBuilder changes = new StringBuilder();
        changes.append("User ID ").append(user.getUserId()).append(" - ");
        
        if (!originalUsername.equals(user.getUsername())) {
            changes.append("Username: ").append(originalUsername).append(" -> ").append(user.getUsername()).append("; ");
        }
        
        if (!originalRole.equals(user.getRole())) {
            changes.append("Role: ").append(originalRole).append(" -> ").append(user.getRole()).append("; ");
        }
        
        changes.append("Name: ").append(user.getFullName()).append("; ");
        changes.append("Email: ").append(user.getEmail()).append("; ");
        changes.append("Status: ").append(user.isActive() ? "Active" : "Inactive");
        
        return changes.toString();
    }
    
    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(getParent());
        setResizable(false);
    }
    
    public boolean isUserUpdated() {
        return userUpdated;
    }
}
