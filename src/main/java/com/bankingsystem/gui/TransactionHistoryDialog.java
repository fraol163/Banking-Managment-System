package com.bankingsystem.gui;

import com.bankingsystem.models.Transaction;
import com.bankingsystem.services.TransactionService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TransactionHistoryDialog extends JDialog {
    private static final Logger LOGGER = Logger.getLogger(TransactionHistoryDialog.class.getName());
    
    private TransactionService transactionService;
    private String accountNumber;
    
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    
    public TransactionHistoryDialog(Window parent, TransactionService transactionService, String accountNumber) {
        super(parent, "Transaction History - " + accountNumber, ModalityType.APPLICATION_MODAL);
        this.transactionService = transactionService;
        this.accountNumber = accountNumber;
        
        initializeComponents();
        setupLayout();
        setupDialog();
        loadTransactionHistory();
    }
    
    private void initializeComponents() {
        String[] columnNames = {"Date", "Type", "Amount", "Balance After", "Description", "Reference"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        transactionTable = new JTable(tableModel);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setRowHeight(25);
        transactionTable.getTableHeader().setReorderingAllowed(false);
        
        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(4).setPreferredWidth(200);
        transactionTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Transaction History for Account: " + accountNumber);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setPreferredSize(new Dimension(750, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(closeButton);
        
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setupDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(getParent());
        setResizable(true);
    }
    
    private void loadTransactionHistory() {
        SwingWorker<List<Transaction>, Void> worker = new SwingWorker<List<Transaction>, Void>() {
            @Override
            protected List<Transaction> doInBackground() throws Exception {
                return transactionService.getTransactionHistory(accountNumber);
            }
            
            @Override
            protected void done() {
                try {
                    List<Transaction> transactions = get();
                    updateTable(transactions);
                    statusLabel.setText(String.format("Loaded %d transactions", transactions.size()));
                    statusLabel.setForeground(Color.BLUE);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load transaction history", e);
                    statusLabel.setText("Failed to load transaction history: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateTable(List<Transaction> transactions) {
        tableModel.setRowCount(0);
        
        for (Transaction transaction : transactions) {
            Object[] row = {
                transaction.getCreatedDate() != null ? transaction.getCreatedDate().toString() : "",
                transaction.getTransactionType(),
                String.format("$%.2f", transaction.getAmount()),
                String.format("$%.2f", transaction.getBalanceAfter()),
                transaction.getDescription(),
                transaction.getReferenceNumber()
            };
            tableModel.addRow(row);
        }
    }
}
