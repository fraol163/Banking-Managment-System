package com.bankingsystem.gui;

import com.bankingsystem.utils.SettingsManager;
import com.bankingsystem.utils.ThemeManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import java.util.logging.Level;

public class SettingsDialog extends JDialog {
    private static final Logger LOGGER = Logger.getLogger(SettingsDialog.class.getName());
    
    private SettingsManager settingsManager;
    private boolean settingsChanged = false;
    
    // UI Components
    private JComboBox<String> themeComboBox;
    private JComboBox<String> lookAndFeelComboBox;
    private JSpinner sessionTimeoutSpinner;
    private JCheckBox autoSaveCheckBox;
    private JCheckBox confirmDeleteCheckBox;
    private JCheckBox showBalanceInListCheckBox;
    private JComboBox<String> dateFormatComboBox;
    private JComboBox<String> currencyFormatComboBox;
    private JSpinner maxTransactionHistorySpinner;
    private JCheckBox enableSoundCheckBox;
    private JButton saveButton;
    private JButton cancelButton;
    private JButton resetButton;
    
    public SettingsDialog(Window parent) {
        super(parent, "Settings", ModalityType.APPLICATION_MODAL);
        this.settingsManager = SettingsManager.getInstance();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadCurrentSettings();
        setupDialog();
    }
    
    private void initializeComponents() {
        // Theme settings
        themeComboBox = new JComboBox<>(new String[]{"Light", "Dark", "System"});
        lookAndFeelComboBox = new JComboBox<>(new String[]{"Metal", "Nimbus", "System", "Cross Platform"});
        
        // Session settings
        sessionTimeoutSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 120, 5));
        autoSaveCheckBox = new JCheckBox("Auto-save data periodically");
        
        // UI preferences
        confirmDeleteCheckBox = new JCheckBox("Confirm before deleting accounts");
        showBalanceInListCheckBox = new JCheckBox("Show account balance in lists");
        enableSoundCheckBox = new JCheckBox("Enable sound notifications");
        
        // Format settings
        dateFormatComboBox = new JComboBox<>(new String[]{"MM/dd/yyyy", "dd/MM/yyyy", "yyyy-MM-dd"});
        currencyFormatComboBox = new JComboBox<>(new String[]{"$#,##0.00", "#,##0.00 USD", "USD #,##0.00"});
        
        // Performance settings
        maxTransactionHistorySpinner = new JSpinner(new SpinnerNumberModel(1000, 100, 10000, 100));
        
        // Buttons
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        resetButton = new JButton("Reset to Defaults");
        
        // Button styling
        saveButton.setBackground(new Color(70, 130, 180));
        saveButton.setForeground(Color.WHITE);
        resetButton.setBackground(new Color(220, 20, 60));
        resetButton.setForeground(Color.WHITE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Appearance Tab
        JPanel appearancePanel = createAppearancePanel();
        tabbedPane.addTab("Appearance", appearancePanel);
        
        // Behavior Tab
        JPanel behaviorPanel = createBehaviorPanel();
        tabbedPane.addTab("Behavior", behaviorPanel);
        
        // Formats Tab
        JPanel formatsPanel = createFormatsPanel();
        tabbedPane.addTab("Formats", formatsPanel);
        
        // Performance Tab
        JPanel performancePanel = createPerformancePanel();
        tabbedPane.addTab("Performance", performancePanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createAppearancePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Theme:"), gbc);
        gbc.gridx = 1;
        panel.add(themeComboBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Look and Feel:"), gbc);
        gbc.gridx = 1;
        panel.add(lookAndFeelComboBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(showBalanceInListCheckBox, gbc);
        
        gbc.gridy = 3;
        panel.add(enableSoundCheckBox, gbc);
        
        return panel;
    }
    
    private JPanel createBehaviorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Session Timeout (minutes):"), gbc);
        gbc.gridx = 1;
        panel.add(sessionTimeoutSpinner, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(autoSaveCheckBox, gbc);
        
        gbc.gridy = 2;
        panel.add(confirmDeleteCheckBox, gbc);
        
        return panel;
    }
    
    private JPanel createFormatsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Date Format:"), gbc);
        gbc.gridx = 1;
        panel.add(dateFormatComboBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Currency Format:"), gbc);
        gbc.gridx = 1;
        panel.add(currencyFormatComboBox, gbc);
        
        return panel;
    }
    
    private JPanel createPerformancePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Max Transaction History:"), gbc);
        gbc.gridx = 1;
        panel.add(maxTransactionHistorySpinner, gbc);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        saveButton.addActionListener(new SaveActionListener());
        cancelButton.addActionListener(e -> dispose());
        resetButton.addActionListener(new ResetActionListener());
        
        // Add change listeners to detect modifications
        themeComboBox.addActionListener(e -> {
            settingsChanged = true;
            // Apply theme immediately for preview
            String selectedTheme = (String) themeComboBox.getSelectedItem();
            ThemeManager.applyTheme(selectedTheme);
        });
        lookAndFeelComboBox.addActionListener(e -> settingsChanged = true);
        sessionTimeoutSpinner.addChangeListener(e -> settingsChanged = true);
        autoSaveCheckBox.addActionListener(e -> settingsChanged = true);
        confirmDeleteCheckBox.addActionListener(e -> settingsChanged = true);
        showBalanceInListCheckBox.addActionListener(e -> settingsChanged = true);
        dateFormatComboBox.addActionListener(e -> settingsChanged = true);
        currencyFormatComboBox.addActionListener(e -> settingsChanged = true);
        maxTransactionHistorySpinner.addChangeListener(e -> settingsChanged = true);
        enableSoundCheckBox.addActionListener(e -> settingsChanged = true);
    }
    
    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);
    }
    
    private void loadCurrentSettings() {
        themeComboBox.setSelectedItem(settingsManager.getTheme());
        lookAndFeelComboBox.setSelectedItem(settingsManager.getLookAndFeel());
        sessionTimeoutSpinner.setValue(settingsManager.getSessionTimeout());
        autoSaveCheckBox.setSelected(settingsManager.isAutoSaveEnabled());
        confirmDeleteCheckBox.setSelected(settingsManager.isConfirmDeleteEnabled());
        showBalanceInListCheckBox.setSelected(settingsManager.isShowBalanceInList());
        dateFormatComboBox.setSelectedItem(settingsManager.getDateFormat());
        currencyFormatComboBox.setSelectedItem(settingsManager.getCurrencyFormat());
        maxTransactionHistorySpinner.setValue(settingsManager.getMaxTransactionHistory());
        enableSoundCheckBox.setSelected(settingsManager.isSoundEnabled());
        
        settingsChanged = false;
    }
    
    private class SaveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // Save all settings
                settingsManager.setTheme((String) themeComboBox.getSelectedItem());
                settingsManager.setLookAndFeel((String) lookAndFeelComboBox.getSelectedItem());
                settingsManager.setSessionTimeout((Integer) sessionTimeoutSpinner.getValue());
                settingsManager.setAutoSaveEnabled(autoSaveCheckBox.isSelected());
                settingsManager.setConfirmDeleteEnabled(confirmDeleteCheckBox.isSelected());
                settingsManager.setShowBalanceInList(showBalanceInListCheckBox.isSelected());
                settingsManager.setDateFormat((String) dateFormatComboBox.getSelectedItem());
                settingsManager.setCurrencyFormat((String) currencyFormatComboBox.getSelectedItem());
                settingsManager.setMaxTransactionHistory((Integer) maxTransactionHistorySpinner.getValue());
                settingsManager.setSoundEnabled(enableSoundCheckBox.isSelected());
                
                settingsManager.saveSettings();
                
                JOptionPane.showMessageDialog(SettingsDialog.this,
                    "Settings saved successfully!\nSome changes may require application restart to take effect.",
                    "Settings Saved",
                    JOptionPane.INFORMATION_MESSAGE);
                
                dispose();
                
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to save settings", ex);
                JOptionPane.showMessageDialog(SettingsDialog.this,
                    "Failed to save settings: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private class ResetActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int result = JOptionPane.showConfirmDialog(SettingsDialog.this,
                "This will reset all settings to their default values.\nAre you sure?",
                "Reset Settings",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
            if (result == JOptionPane.YES_OPTION) {
                settingsManager.resetToDefaults();
                loadCurrentSettings();
                JOptionPane.showMessageDialog(SettingsDialog.this,
                    "Settings have been reset to defaults.",
                    "Settings Reset",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
