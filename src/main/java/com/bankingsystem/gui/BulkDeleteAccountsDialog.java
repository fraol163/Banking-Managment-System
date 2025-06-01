package com.bankingsystem.gui;

import com.bankingsystem.models.AbstractAccount;
import com.bankingsystem.services.AccountService;
import com.bankingsystem.services.UserService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class BulkDeleteAccountsDialog extends JDialog {
    private static final Logger LOGGER = Logger.getLogger(BulkDeleteAccountsDialog.class.getName());

    private AccountService accountService;
    private UserService userService;
    private boolean accountsDeleted = false;

    private JTable accountTable;
    private DefaultTableModel tableModel;
    private JButton selectAllButton;
    private JButton deselectAllButton;
    private JButton deleteSelectedButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private JProgressBar progressBar;

    public BulkDeleteAccountsDialog(Window parent, AccountService accountService, UserService userService) {
        super(parent, "Bulk Delete Accounts", ModalityType.APPLICATION_MODAL);
        this.accountService = accountService;
        this.userService = userService;

        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadAccounts();
        setupDialog();
    }

    private void initializeComponents() {
        String[] columnNames = {"Select", "Account ID", "Account Number", "Customer ID", "Type", "Balance", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        accountTable = new JTable(tableModel);
        accountTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        accountTable.setRowHeight(25);
        accountTable.getTableHeader().setReorderingAllowed(false);

        accountTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        accountTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        accountTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        accountTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        accountTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        accountTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        accountTable.getColumnModel().getColumn(6).setPreferredWidth(80);

        selectAllButton = new JButton("Select All");
        deselectAllButton = new JButton("Deselect All");
        deleteSelectedButton = new JButton("Delete Selected Accounts");
        cancelButton = new JButton("Cancel");

        selectAllButton.setBackground(new Color(70, 130, 180));
        selectAllButton.setForeground(Color.WHITE);
        selectAllButton.setFocusPainted(false);

        deselectAllButton.setBackground(new Color(128, 128, 128));
        deselectAllButton.setForeground(Color.WHITE);
        deselectAllButton.setFocusPainted(false);

        deleteSelectedButton.setBackground(new Color(220, 20, 60));
        deleteSelectedButton.setForeground(Color.WHITE);
        deleteSelectedButton.setFocusPainted(false);

        statusLabel = new JLabel("Select accounts to delete permanently");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Bulk Account Deletion - Select Accounts to Delete Permanently");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLabel.setForeground(Color.RED);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JLabel warningLabel = new JLabel("WARNING: This will permanently delete selected accounts and all related data!");
        warningLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        warningLabel.setForeground(Color.RED);
        headerPanel.add(warningLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(accountTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Accounts"));
        scrollPane.setPreferredSize(new Dimension(700, 400));
        add(scrollPane, BorderLayout.CENTER);

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.add(selectAllButton);
        selectionPanel.add(deselectAllButton);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(deleteSelectedButton);
        buttonPanel.add(cancelButton);

        JPanel controlPanel = new JPanel(new BorderLayout());
        controlPanel.add(selectionPanel, BorderLayout.WEST);
        controlPanel.add(buttonPanel, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(controlPanel, BorderLayout.NORTH);
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(progressBar, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        selectAllButton.addActionListener(e -> selectAllAccounts(true));
        deselectAllButton.addActionListener(e -> selectAllAccounts(false));
        deleteSelectedButton.addActionListener(e -> deleteSelectedAccounts());
        cancelButton.addActionListener(e -> dispose());

        tableModel.addTableModelListener(e -> updateDeleteButtonState());
    }

    private void loadAccounts() {
        SwingWorker<List<AbstractAccount>, Void> worker = new SwingWorker<List<AbstractAccount>, Void>() {
            @Override
            protected List<AbstractAccount> doInBackground() throws Exception {
                return accountService.getAllAccounts();
            }

            @Override
            protected void done() {
                try {
                    List<AbstractAccount> accounts = get();
                    updateTable(accounts);
                    statusLabel.setText(String.format("Loaded %d accounts available for deletion", accounts.size()));
                    statusLabel.setForeground(Color.BLUE);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load accounts", e);
                    statusLabel.setText("Failed to load accounts: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        };

        worker.execute();
    }

    private void updateTable(List<AbstractAccount> accounts) {
        tableModel.setRowCount(0);

        for (AbstractAccount account : accounts) {
            Object[] row = {
                false,
                account.getAccountId(),
                account.getAccountNumber(),
                account.getCustomerId(),
                getAccountTypeName(account),
                String.format("$%.2f", account.getBalance()),
                account.getStatus()
            };
            tableModel.addRow(row);
        }
    }

    private String getAccountTypeName(AbstractAccount account) {
        String className = account.getClass().getSimpleName();
        return className.replace("Account", "");
    }

    private void selectAllAccounts(boolean select) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(select, i, 0);
        }
        updateDeleteButtonState();
    }

    private void updateDeleteButtonState() {
        int selectedCount = getSelectedAccountCount();
        deleteSelectedButton.setEnabled(selectedCount > 0);

        if (selectedCount > 0) {
            statusLabel.setText(String.format("%d accounts selected for deletion", selectedCount));
            statusLabel.setForeground(Color.ORANGE);
        } else {
            statusLabel.setText("Select accounts to delete permanently");
            statusLabel.setForeground(Color.BLACK);
        }
    }

    private int getSelectedAccountCount() {
        int count = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((Boolean) tableModel.getValueAt(i, 0)) {
                count++;
            }
        }
        return count;
    }

    private List<Integer> getSelectedAccountIds() {
        List<Integer> accountIds = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((Boolean) tableModel.getValueAt(i, 0)) {
                accountIds.add((Integer) tableModel.getValueAt(i, 1));
            }
        }
        return accountIds;
    }

    private void deleteSelectedAccounts() {
        List<Integer> selectedAccountIds = getSelectedAccountIds();
        if (selectedAccountIds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please select at least one account to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder accountDetails = new StringBuilder();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((Boolean) tableModel.getValueAt(i, 0)) {
                String accountNumber = (String) tableModel.getValueAt(i, 2);
                String accountType = (String) tableModel.getValueAt(i, 4);
                String balance = (String) tableModel.getValueAt(i, 5);
                accountDetails.append(String.format("• %s (%s) - %s\n", accountNumber, accountType, balance));
            }
        }

        String[] options = {"Cancel", "I Understand - Delete All Selected"};
        int firstConfirm = JOptionPane.showOptionDialog(this,
            String.format("WARNING: BULK PERMANENT ACCOUNT DELETION\n\n" +
                         "You are about to PERMANENTLY DELETE %d accounts:\n\n%s\n" +
                         "This action:\n" +
                         "• Cannot be undone\n" +
                         "• Will remove ALL selected accounts and their transactions\n" +
                         "• Will affect all reports containing these accounts\n" +
                         "• May impact customer records and audit trails\n\n" +
                         "Are you absolutely sure you want to proceed?",
                         selectedAccountIds.size(), accountDetails.toString()),
            "Confirm Bulk Permanent Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            options,
            options[0]);

        if (firstConfirm != 1) {
            return;
        }

        int secondConfirm = JOptionPane.showConfirmDialog(this,
            String.format("FINAL CONFIRMATION\n\n" +
                         "This is your last chance to cancel this irreversible action.\n" +
                         "Permanently delete %d selected accounts and all their data?",
                         selectedAccountIds.size()),
            "Final Confirmation Required",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.ERROR_MESSAGE);

        if (secondConfirm == JOptionPane.OK_OPTION) {
            performBulkDeletion(selectedAccountIds);
        }
    }

    private void performBulkDeletion(List<Integer> accountIds) {
        progressBar.setVisible(true);
        progressBar.setMaximum(accountIds.size());
        progressBar.setValue(0);
        progressBar.setString("Preparing deletion...");

        selectAllButton.setEnabled(false);
        deselectAllButton.setEnabled(false);
        deleteSelectedButton.setEnabled(false);

        SwingWorker<Integer, String> worker = new SwingWorker<Integer, String>() {
            @Override
            protected Integer doInBackground() throws Exception {
                int deletedCount = 0;
                int currentIndex = 0;

                for (Integer accountId : accountIds) {
                    currentIndex++;

                    try {
                        AbstractAccount account = accountService.getAccountById(accountId);
                        if (account != null) {
                            publish(String.format("Deleting account %s (%d/%d)...",
                                   account.getAccountNumber(), currentIndex, accountIds.size()));

                            boolean success = accountService.deleteAccountPermanently(accountId, true);
                            if (success) {
                                deletedCount++;
                                LOGGER.warning(String.format("Bulk deletion: Account %s permanently deleted by %s",
                                             account.getAccountNumber(), userService.getCurrentUser().getUsername()));
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Failed to delete account ID: " + accountId, e);
                    }

                    final int index = currentIndex;
                    SwingUtilities.invokeLater(() -> progressBar.setValue(index));

                    Thread.sleep(100);
                }

                return deletedCount;
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    progressBar.setString(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                try {
                    int deletedCount = get();

                    progressBar.setString("Deletion completed");
                    statusLabel.setText(String.format("Successfully deleted %d out of %d selected accounts",
                                       deletedCount, accountIds.size()));
                    statusLabel.setForeground(deletedCount > 0 ? Color.GREEN : Color.RED);

                    accountsDeleted = deletedCount > 0;

                    JOptionPane.showMessageDialog(BulkDeleteAccountsDialog.this,
                        String.format("Bulk deletion completed.\n" +
                                     "Successfully deleted: %d accounts\n" +
                                     "Total selected: %d accounts",
                                     deletedCount, accountIds.size()),
                        "Bulk Deletion Complete",
                        JOptionPane.INFORMATION_MESSAGE);

                    if (deletedCount > 0) {
                        loadAccounts();
                    }

                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Bulk deletion failed", e);
                    statusLabel.setText("Bulk deletion failed: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);

                    JOptionPane.showMessageDialog(BulkDeleteAccountsDialog.this,
                        "Bulk deletion failed:\n" + e.getMessage(),
                        "Deletion Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setVisible(false);
                    selectAllButton.setEnabled(true);
                    deselectAllButton.setEnabled(true);
                    deleteSelectedButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(getParent());
        setResizable(true);
    }

    public boolean isAccountsDeleted() {
        return accountsDeleted;
    }
}
