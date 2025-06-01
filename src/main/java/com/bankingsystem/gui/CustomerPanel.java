package com.bankingsystem.gui;

import com.bankingsystem.models.User;
import com.bankingsystem.services.UserService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CustomerPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(CustomerPanel.class.getName());
    private UserService userService;

    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton createUserButton;
    private JButton editUserButton;
    private JButton deactivateUserButton;
    private JButton deletePermanentlyButton;
    private JButton refreshButton;
    private JLabel statusLabel;

    public CustomerPanel(UserService userService) {
        this.userService = userService;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadUsers();
    }

    private void initializeComponents() {
        String[] columnNames = {"User ID", "Username", "Role", "Full Name", "Email", "Status", "Last Login"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(25);
        userTable.getTableHeader().setReorderingAllowed(false);

        createUserButton = new JButton("Create User");
        editUserButton = new JButton("Edit User");
        deactivateUserButton = new JButton("Deactivate User");
        deletePermanentlyButton = new JButton("Delete Permanently");
        refreshButton = new JButton("Refresh");

        createUserButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        editUserButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        deactivateUserButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        deletePermanentlyButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        refreshButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        createUserButton.setBackground(new Color(70, 130, 180));
        createUserButton.setForeground(Color.WHITE);
        createUserButton.setFocusPainted(false);

        deactivateUserButton.setBackground(new Color(220, 20, 60));
        deactivateUserButton.setForeground(Color.WHITE);
        deactivateUserButton.setFocusPainted(false);

        deletePermanentlyButton.setBackground(new Color(139, 0, 0));
        deletePermanentlyButton.setForeground(Color.WHITE);
        deletePermanentlyButton.setFocusPainted(false);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        if (!userService.hasPermission("CREATE_USER")) {
            createUserButton.setEnabled(false);
            createUserButton.setToolTipText("You do not have permission to create users");
        }

        if (!userService.hasPermission("UPDATE_USER")) {
            editUserButton.setEnabled(false);
            editUserButton.setToolTipText("You do not have permission to edit users");
        }

        if (!userService.hasPermission("DELETE_USER")) {
            deactivateUserButton.setEnabled(false);
            deactivateUserButton.setToolTipText("You do not have permission to deactivate users");
        }

        if (!userService.getCurrentUser().isAdmin()) {
            deletePermanentlyButton.setEnabled(false);
            deletePermanentlyButton.setToolTipText("Only Admin users can permanently delete users");
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("User Management");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("System Users"));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(createUserButton);
        buttonPanel.add(editUserButton);
        buttonPanel.add(deactivateUserButton);
        if (userService.getCurrentUser().isAdmin()) {
            buttonPanel.add(deletePermanentlyButton);
        }
        buttonPanel.add(refreshButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.WEST);
        bottomPanel.add(statusLabel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        createUserButton.addActionListener(e -> showCreateUserDialog());
        editUserButton.addActionListener(e -> showEditUserDialog());
        deactivateUserButton.addActionListener(e -> deactivateSelectedUser());
        deletePermanentlyButton.addActionListener(e -> deleteSelectedUserPermanently());
        refreshButton.addActionListener(e -> loadUsers());

        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = userTable.getSelectedRow() != -1;
                editUserButton.setEnabled(hasSelection && userService.hasPermission("UPDATE_USER"));
                deactivateUserButton.setEnabled(hasSelection && userService.hasPermission("DELETE_USER"));
                if (userService.getCurrentUser().isAdmin()) {
                    deletePermanentlyButton.setEnabled(hasSelection);
                }
            }
        });
    }

    private void loadUsers() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return userService.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    List<User> users = get();
                    updateTable(users);
                    statusLabel.setText(String.format("Loaded %d users", users.size()));
                    statusLabel.setForeground(Color.BLUE);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load users", e);
                    statusLabel.setText("Failed to load users: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        };

        worker.execute();
    }

    private void updateTable(List<User> users) {
        tableModel.setRowCount(0);

        for (User user : users) {
            Object[] row = {
                user.getUserId(),
                user.getUsername(),
                user.getRole(),
                user.getFullName(),
                user.getEmail(),
                user.isActive() ? "Active" : "Inactive",
                user.getLastLogin() != null ? user.getLastLogin().toString() : "Never"
            };
            tableModel.addRow(row);
        }
    }

    private void showCreateUserDialog() {
        JOptionPane.showMessageDialog(this,
            "User creation dialog would be implemented here.\n" +
            "This would allow creating new Manager and Teller users\n" +
            "based on your current role permissions.",
            "Create User",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showEditUserDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a user to edit.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Integer userId = (Integer) tableModel.getValueAt(selectedRow, 0);
            User user = userService.getUserById(userId);

            if (user == null) {
                JOptionPane.showMessageDialog(this,
                    "Selected user not found in the system.",
                    "User Not Found",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            EditUserDialog dialog = new EditUserDialog(SwingUtilities.getWindowAncestor(this), user, userService);
            dialog.setVisible(true);

            if (dialog.isUserUpdated()) {
                loadUsers();
                statusLabel.setText("User updated successfully: " + user.getUsername());
                statusLabel.setForeground(Color.GREEN);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening edit user dialog", e);
            statusLabel.setText("Error opening edit dialog: " + e.getMessage());
            statusLabel.setForeground(Color.RED);

            JOptionPane.showMessageDialog(this,
                "Error opening edit user dialog:\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deactivateSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a user to deactivate.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) tableModel.getValueAt(selectedRow, 1);
        String currentUserRole = (String) tableModel.getValueAt(selectedRow, 2);

        if (!userService.getCurrentUser().canCreateUserRole(currentUserRole)) {
            JOptionPane.showMessageDialog(this,
                "You do not have permission to deactivate " + currentUserRole + " users.",
                "Permission Denied",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to deactivate user: " + username + "?",
            "Confirm Deactivation",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Integer userId = (Integer) tableModel.getValueAt(selectedRow, 0);
                boolean success = userService.deactivateUser(userId);
                if (success) {
                    statusLabel.setText("User deactivated successfully");
                    statusLabel.setForeground(Color.GREEN);
                    loadUsers();
                } else {
                    statusLabel.setText("Failed to deactivate user");
                    statusLabel.setForeground(Color.RED);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to deactivate user", e);
                statusLabel.setText("Error deactivating user: " + e.getMessage());
                statusLabel.setForeground(Color.RED);
            }
        }
    }

    private void deleteSelectedUserPermanently() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a user to permanently delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) tableModel.getValueAt(selectedRow, 1);
        String currentUserRole = (String) tableModel.getValueAt(selectedRow, 2);
        Integer userId = (Integer) tableModel.getValueAt(selectedRow, 0);

        if (userId.equals(userService.getCurrentUser().getUserId())) {
            JOptionPane.showMessageDialog(this,
                "You cannot permanently delete your own user account.",
                "Cannot Delete Current User",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] options = {"Cancel", "I Understand - Delete Permanently"};
        int firstConfirm = JOptionPane.showOptionDialog(this,
            "WARNING: PERMANENT DELETION\n\n" +
            "You are about to PERMANENTLY DELETE user: " + username + "\n" +
            "Role: " + currentUserRole + "\n\n" +
            "This action:\n" +
            "• Cannot be undone\n" +
            "• Will remove all user data from the system\n" +
            "• May affect related accounts and transactions\n\n" +
            "Are you absolutely sure you want to proceed?",
            "Confirm Permanent Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options[0]);

        if (firstConfirm != 1) {
            return;
        }

        int secondConfirm = JOptionPane.showConfirmDialog(this,
            "FINAL CONFIRMATION\n\n" +
            "Type the username to confirm permanent deletion:\n" +
            "Expected: " + username + "\n\n" +
            "This is your last chance to cancel this irreversible action.",
            "Final Confirmation Required",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.ERROR_MESSAGE);

        if (secondConfirm != JOptionPane.OK_OPTION) {
            return;
        }

        String typedUsername = JOptionPane.showInputDialog(this,
            "Type the username '" + username + "' to confirm:",
            "Confirm Username",
            JOptionPane.PLAIN_MESSAGE);

        if (!username.equals(typedUsername)) {
            JOptionPane.showMessageDialog(this,
                "Username confirmation failed. Deletion cancelled.",
                "Confirmation Failed",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            boolean success = userService.deleteUserPermanently(userId);
            if (success) {
                statusLabel.setText("User permanently deleted: " + username);
                statusLabel.setForeground(Color.RED);
                loadUsers();

                JOptionPane.showMessageDialog(this,
                    "User '" + username + "' has been permanently deleted from the system.",
                    "Deletion Complete",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                statusLabel.setText("Failed to permanently delete user");
                statusLabel.setForeground(Color.RED);
            }
        } catch (Exception e) {
            statusLabel.setText("Error permanently deleting user: " + e.getMessage());
            statusLabel.setForeground(Color.RED);

            JOptionPane.showMessageDialog(this,
                "Failed to permanently delete user:\n" + e.getMessage(),
                "Deletion Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
