package com.bankingsystem.gui;

import com.bankingsystem.models.TransactionApproval;
import com.bankingsystem.services.ApprovalService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ApprovalDialog extends JDialog {
    private static final Logger LOGGER = Logger.getLogger(ApprovalDialog.class.getName());
    
    private ApprovalService approvalService;
    private Integer currentUserId;
    private JTable approvalsTable;
    private DefaultTableModel tableModel;
    private JButton approveButton;
    private JButton rejectButton;
    private JButton refreshButton;
    private JButton closeButton;
    private JTextArea commentsArea;
    private JLabel statusLabel;
    
    public ApprovalDialog(Window parent, ApprovalService approvalService, Integer currentUserId) {
        super(parent, "Transaction Approvals", ModalityType.APPLICATION_MODAL);
        this.approvalService = approvalService;
        this.currentUserId = currentUserId;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupDialog();
        loadPendingApprovals();
    }
    
    private void initializeComponents() {
        // Table setup
        String[] columnNames = {"ID", "Type", "Account", "Amount", "Requested By", "Role", "Date", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        approvalsTable = new JTable(tableModel);
        approvalsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Buttons
        approveButton = new JButton("Approve");
        rejectButton = new JButton("Reject");
        refreshButton = new JButton("Refresh");
        closeButton = new JButton("Close");
        
        // Comments area
        commentsArea = new JTextArea(3, 30);
        commentsArea.setBorder(BorderFactory.createTitledBorder("Comments/Reason"));
        
        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        // Button styling
        approveButton.setBackground(new Color(34, 139, 34));
        approveButton.setForeground(Color.WHITE);
        rejectButton.setBackground(new Color(220, 20, 60));
        rejectButton.setForeground(Color.WHITE);
        refreshButton.setBackground(new Color(70, 130, 180));
        refreshButton.setForeground(Color.WHITE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Table panel
        JScrollPane tableScrollPane = new JScrollPane(approvalsTable);
        tableScrollPane.setPreferredSize(new Dimension(800, 300));
        add(tableScrollPane, BorderLayout.CENTER);
        
        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Comments panel
        JScrollPane commentsScrollPane = new JScrollPane(commentsArea);
        bottomPanel.add(commentsScrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        JPanel bottomButtonPanel = new JPanel(new BorderLayout());
        bottomButtonPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomButtonPanel.add(statusLabel, BorderLayout.SOUTH);
        
        bottomPanel.add(bottomButtonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        approveButton.addActionListener(new ApproveActionListener());
        rejectButton.addActionListener(new RejectActionListener());
        refreshButton.addActionListener(e -> loadPendingApprovals());
        closeButton.addActionListener(e -> dispose());
        
        // Enable/disable buttons based on selection
        approvalsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = approvalsTable.getSelectedRow() != -1;
            approveButton.setEnabled(hasSelection);
            rejectButton.setEnabled(hasSelection);
        });
    }
    
    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(getParent());
        setResizable(true);
        
        // Initially disable approve/reject buttons
        approveButton.setEnabled(false);
        rejectButton.setEnabled(false);
    }
    
    private void loadPendingApprovals() {
        try {
            List<TransactionApproval> pendingApprovals = approvalService.getPendingApprovalsForUser(currentUserId);
            updateTable(pendingApprovals);
            statusLabel.setText(String.format("Found %d pending approvals", pendingApprovals.size()));
            statusLabel.setForeground(Color.BLUE);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load pending approvals", e);
            statusLabel.setText("Failed to load approvals: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }
    
    private void updateTable(List<TransactionApproval> approvals) {
        tableModel.setRowCount(0);
        
        for (TransactionApproval approval : approvals) {
            Object[] row = {
                approval.getApprovalId(),
                approval.getTransactionType(),
                approval.getAccountNumber(),
                String.format("$%.2f", approval.getAmount()),
                approval.getRequestedByUserId(),
                approval.getRequestedByUserRole(),
                approval.getRequestedDate().toString(),
                approval.getDescription()
            };
            tableModel.addRow(row);
        }
    }
    
    private TransactionApproval getSelectedApproval() {
        int selectedRow = approvalsTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        
        Integer approvalId = (Integer) tableModel.getValueAt(selectedRow, 0);
        return approvalService.getApprovalById(approvalId);
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.RED);
    }
    
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.GREEN);
    }
    
    private class ApproveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            TransactionApproval approval = getSelectedApproval();
            if (approval == null) {
                showError("Please select an approval request");
                return;
            }
            
            String comments = commentsArea.getText().trim();
            if (comments.isEmpty()) {
                comments = "Approved by manager";
            }
            
            try {
                approvalService.approveRequest(approval.getApprovalId(), currentUserId, comments);
                showSuccess("Transaction approved successfully");
                loadPendingApprovals(); // Refresh the list
                commentsArea.setText(""); // Clear comments
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Failed to approve transaction", ex);
                showError("Failed to approve: " + ex.getMessage());
            }
        }
    }
    
    private class RejectActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            TransactionApproval approval = getSelectedApproval();
            if (approval == null) {
                showError("Please select an approval request");
                return;
            }
            
            String reason = commentsArea.getText().trim();
            if (reason.isEmpty()) {
                showError("Please provide a reason for rejection");
                return;
            }
            
            try {
                approvalService.rejectRequest(approval.getApprovalId(), currentUserId, reason);
                showSuccess("Transaction rejected");
                loadPendingApprovals(); // Refresh the list
                commentsArea.setText(""); // Clear comments
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Failed to reject transaction", ex);
                showError("Failed to reject: " + ex.getMessage());
            }
        }
    }
}
