package com.bankingsystem.gui;

import com.bankingsystem.models.AbstractAccount;
import com.bankingsystem.services.AccountService;
import com.bankingsystem.services.UserService;
import javax.swing.*;
import java.awt.*;

public class AccountDetailsDialog extends JDialog {
    private AbstractAccount account;
    private AccountService accountService;
    private UserService userService;
    private boolean accountUpdated = false;
    
    public AccountDetailsDialog(Window parent, AbstractAccount account, 
                              AccountService accountService, UserService userService) {
        super(parent, "Account Details", ModalityType.APPLICATION_MODAL);
        this.account = account;
        this.accountService = accountService;
        this.userService = userService;
        
        initializeComponents();
        setupDialog();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        addDetailRow(detailsPanel, gbc, 0, "Account ID:", account.getAccountId().toString());
        addDetailRow(detailsPanel, gbc, 1, "Account Number:", account.getAccountNumber());
        addDetailRow(detailsPanel, gbc, 2, "Customer ID:", account.getCustomerId().toString());
        addDetailRow(detailsPanel, gbc, 3, "Account Type:", getAccountTypeName());
        addDetailRow(detailsPanel, gbc, 4, "Balance:", String.format("$%.2f", account.getBalance()));
        addDetailRow(detailsPanel, gbc, 5, "Status:", account.getStatus());
        addDetailRow(detailsPanel, gbc, 6, "Minimum Balance:", String.format("$%.2f", account.getMinimumBalance()));
        addDetailRow(detailsPanel, gbc, 7, "Interest Rate:", String.format("%.2f%%", account.getInterestRate().multiply(new java.math.BigDecimal("100"))));
        addDetailRow(detailsPanel, gbc, 8, "Created Date:", account.getCreatedDate() != null ? account.getCreatedDate().toString() : "");
        
        add(detailsPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        panel.add(labelComponent, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        panel.add(valueComponent, gbc);
    }
    
    private String getAccountTypeName() {
        String className = account.getClass().getSimpleName();
        return className.replace("Account", "");
    }
    
    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(getParent());
        setResizable(false);
    }
    
    public boolean isAccountUpdated() {
        return accountUpdated;
    }
}
