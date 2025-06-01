package com.bankingsystem.gui;

import com.bankingsystem.services.UserService;
import com.bankingsystem.utils.FileUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class SystemAdminPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(SystemAdminPanel.class.getName());
    private UserService userService;

    public SystemAdminPanel(UserService userService) {
        this.userService = userService;
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("System Administration");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        if (userService.hasPermission("BACKUP_DATABASE")) {
            JButton backupButton = new JButton("Backup Database");
            backupButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            backupButton.setBackground(new Color(70, 130, 180));
            backupButton.setForeground(Color.WHITE);
            backupButton.setFocusPainted(false);
            backupButton.addActionListener(new BackupActionListener());

            gbc.gridx = 0;
            gbc.gridy = 0;
            centerPanel.add(backupButton, gbc);
        }

        if (userService.hasPermission("VIEW_AUDIT_LOG")) {
            JButton auditButton = new JButton("View Audit Log");
            auditButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            auditButton.setBackground(new Color(34, 139, 34));
            auditButton.setForeground(Color.WHITE);
            auditButton.setFocusPainted(false);
            auditButton.addActionListener(e -> showAuditLog());

            gbc.gridx = 1;
            gbc.gridy = 0;
            centerPanel.add(auditButton, gbc);
        }

        if (userService.hasPermission("MANAGE_SYSTEM_CONFIG")) {
            JButton configButton = new JButton("System Configuration");
            configButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            configButton.setBackground(new Color(255, 140, 0));
            configButton.setForeground(Color.WHITE);
            configButton.setFocusPainted(false);
            configButton.addActionListener(e -> showSystemConfig());

            gbc.gridx = 0;
            gbc.gridy = 1;
            centerPanel.add(configButton, gbc);
        }

        JTextArea infoArea = new JTextArea(10, 40);
        infoArea.setEditable(false);
        infoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        infoArea.setText("System Administration Panel\n\n" +
                        "Available functions based on your role:\n" +
                        "- Database backup and restore operations\n" +
                        "- Audit log viewing and analysis\n" +
                        "- System configuration management\n" +
                        "- User activity monitoring\n\n" +
                        "Select an operation from the buttons above.");

        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("System Information"));

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        centerPanel.add(scrollPane, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }

    private class BackupActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String backupPath = FileUtil.backupDatabase();
                JOptionPane.showMessageDialog(SystemAdminPanel.this,
                    "Database backup completed successfully!\nBackup saved to: " + backupPath,
                    "Backup Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Database backup failed", ex);
                JOptionPane.showMessageDialog(SystemAdminPanel.this,
                    "Database backup failed: " + ex.getMessage(),
                    "Backup Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAuditLog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Audit Log", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setText("Audit Log Viewer\n\n" +
                       "Recent system activities:\n" +
                       "- User login/logout events\n" +
                       "- Transaction processing\n" +
                       "- Account management operations\n" +
                       "- System configuration changes\n\n" +
                       "Note: This is a demonstration view.\n" +
                       "In a production system, this would show actual audit data.");

        JScrollPane scrollPane = new JScrollPane(logArea);
        dialog.add(scrollPane);

        dialog.setVisible(true);
    }

    private void showSystemConfig() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "System Configuration", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JTextArea configArea = new JTextArea();
        configArea.setEditable(false);
        configArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        configArea.setText("System Configuration\n\n" +
                          "Current Settings:\n" +
                          "- Database: Mock in-memory implementation\n" +
                          "- Session timeout: 30 minutes\n" +
                          "- Transaction limits:\n" +
                          "  * Teller: $2,000 daily\n" +
                          "  * Manager: $10,000 daily\n" +
                          "  * Admin: Unlimited\n" +
                          "- Approval thresholds:\n" +
                          "  * Teller: $1,000\n" +
                          "  * Manager: $5,000\n\n" +
                          "Note: Configuration changes require system restart.");

        JScrollPane scrollPane = new JScrollPane(configArea);
        dialog.add(scrollPane);

        dialog.setVisible(true);
    }
}
