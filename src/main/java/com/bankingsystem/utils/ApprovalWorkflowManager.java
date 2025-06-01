package com.bankingsystem.utils;

import com.bankingsystem.models.TransactionApproval;
import com.bankingsystem.models.User;
import com.bankingsystem.services.ApprovalService;
import com.bankingsystem.dao.UserDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import java.awt.Component;

/**
 * Enhanced approval workflow manager with automatic approval request creation
 * and user-friendly notifications
 */
public class ApprovalWorkflowManager {
    private static final Logger LOGGER = Logger.getLogger(ApprovalWorkflowManager.class.getName());
    
    private final ApprovalService approvalService;
    private final UserDAO userDAO;
    
    public ApprovalWorkflowManager() {
        this.approvalService = new ApprovalService();
        this.userDAO = new UserDAO();
    }
    
    /**
     * Handle transaction that requires approval - creates approval request and shows user-friendly message
     */
    public static class ApprovalResult {
        private final boolean requiresApproval;
        private final boolean canSelfApprove;
        private final TransactionApproval approvalRequest;
        private final String message;
        
        public ApprovalResult(boolean requiresApproval, boolean canSelfApprove, 
                            TransactionApproval approvalRequest, String message) {
            this.requiresApproval = requiresApproval;
            this.canSelfApprove = canSelfApprove;
            this.approvalRequest = approvalRequest;
            this.message = message;
        }
        
        public boolean requiresApproval() { return requiresApproval; }
        public boolean canSelfApprove() { return canSelfApprove; }
        public TransactionApproval getApprovalRequest() { return approvalRequest; }
        public String getMessage() { return message; }
    }
    
    /**
     * Handle deposit transaction that requires approval
     */
    public ApprovalResult handleDepositApproval(String accountNumber, BigDecimal amount, 
                                              String description, Integer userId, Component parent) {
        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                ErrorHandler.showError(parent, "User Error", "Invalid user session. Please log in again.");
                return new ApprovalResult(true, false, null, "Invalid user session");
            }
            
            // Check if approval is required
            if (!approvalService.requiresApproval(userId, amount)) {
                return new ApprovalResult(false, false, null, "No approval required");
            }
            
            // Check if user can self-approve
            if (approvalService.canSelfApprove(userId, amount)) {
                TransactionApproval approval = approvalService.createApprovalRequest(
                    "Deposit", accountNumber, amount, description, userId);
                approvalService.approveRequest(approval.getApprovalId(), userId, "Self-approved");
                
                String message = String.format(
                    "Deposit approved and processed.\n\n" +
                    "Amount: $%.2f\n" +
                    "Account: %s\n" +
                    "Approval ID: %d\n\n" +
                    "Transaction completed successfully.",
                    amount, accountNumber, approval.getApprovalId()
                );
                
                return new ApprovalResult(true, true, approval, message);
            }
            
            // Create approval request for manager/admin approval
            TransactionApproval approval = approvalService.createApprovalRequest(
                "Deposit", accountNumber, amount, description, userId);
            
            String approverRole = getRequiredApproverRole(user, amount);
            String message = String.format(
                "Deposit requires %s approval.\n\n" +
                "Amount: $%.2f\n" +
                "Account: %s\n" +
                "Approval Request ID: %d\n\n" +
                "Your request has been sent for approval.\n" +
                "You will be notified once a decision is made.\n\n" +
                "Current Status: PENDING",
                approverRole, amount, accountNumber, approval.getApprovalId()
            );
            
            // Show approval required notification
            SwingUtilities.invokeLater(() -> 
                ErrorHandler.showApprovalRequired(parent, "Deposit", amount.doubleValue(), approval.getApprovalId()));
            
            return new ApprovalResult(true, false, approval, message);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during approval workflow", e);
            ErrorHandler.showError(parent, "Database Error", 
                "Unable to process approval request due to database error.\nPlease try again or contact support.");
            return new ApprovalResult(true, false, null, "Database error");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during approval workflow", e);
            ErrorHandler.showError(parent, "System Error", 
                "An unexpected error occurred while processing your request.\nPlease try again or contact support.");
            return new ApprovalResult(true, false, null, "System error");
        }
    }
    
    /**
     * Handle withdrawal transaction that requires approval
     */
    public ApprovalResult handleWithdrawalApproval(String accountNumber, BigDecimal amount, 
                                                  String description, Integer userId, Component parent) {
        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                ErrorHandler.showError(parent, "User Error", "Invalid user session. Please log in again.");
                return new ApprovalResult(true, false, null, "Invalid user session");
            }
            
            // Check if approval is required
            if (!approvalService.requiresApproval(userId, amount)) {
                return new ApprovalResult(false, false, null, "No approval required");
            }
            
            // Check if user can self-approve
            if (approvalService.canSelfApprove(userId, amount)) {
                TransactionApproval approval = approvalService.createApprovalRequest(
                    "Withdrawal", accountNumber, amount, description, userId);
                approvalService.approveRequest(approval.getApprovalId(), userId, "Self-approved");
                
                return new ApprovalResult(true, true, approval, "Self-approved and processed");
            }
            
            // Create approval request
            TransactionApproval approval = approvalService.createApprovalRequest(
                "Withdrawal", accountNumber, amount, description, userId);
            
            String approverRole = getRequiredApproverRole(user, amount);
            
            // Show approval required notification
            SwingUtilities.invokeLater(() -> 
                ErrorHandler.showApprovalRequired(parent, "Withdrawal", amount.doubleValue(), approval.getApprovalId()));
            
            return new ApprovalResult(true, false, approval, "Approval request created");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during approval workflow", e);
            ErrorHandler.showError(parent, "Database Error", 
                "Unable to process approval request due to database error.\nPlease try again or contact support.");
            return new ApprovalResult(true, false, null, "Database error");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during approval workflow", e);
            ErrorHandler.showError(parent, "System Error", 
                "An unexpected error occurred while processing your request.\nPlease try again or contact support.");
            return new ApprovalResult(true, false, null, "System error");
        }
    }
    
    /**
     * Handle transfer transaction that requires approval
     */
    public ApprovalResult handleTransferApproval(String fromAccount, String toAccount, BigDecimal amount, 
                                               String description, Integer userId, Component parent) {
        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                ErrorHandler.showError(parent, "User Error", "Invalid user session. Please log in again.");
                return new ApprovalResult(true, false, null, "Invalid user session");
            }
            
            // Check if approval is required
            if (!approvalService.requiresApproval(userId, amount)) {
                return new ApprovalResult(false, false, null, "No approval required");
            }
            
            // Check if user can self-approve
            if (approvalService.canSelfApprove(userId, amount)) {
                TransactionApproval approval = approvalService.createApprovalRequest(
                    "Transfer", fromAccount + " to " + toAccount, amount, description, userId);
                approvalService.approveRequest(approval.getApprovalId(), userId, "Self-approved");
                
                return new ApprovalResult(true, true, approval, "Self-approved and processed");
            }
            
            // Create approval request
            TransactionApproval approval = approvalService.createApprovalRequest(
                "Transfer", fromAccount + " to " + toAccount, amount, description, userId);
            
            String approverRole = getRequiredApproverRole(user, amount);
            
            // Show approval required notification
            SwingUtilities.invokeLater(() -> 
                ErrorHandler.showApprovalRequired(parent, "Transfer", amount.doubleValue(), approval.getApprovalId()));
            
            return new ApprovalResult(true, false, approval, "Approval request created");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during approval workflow", e);
            ErrorHandler.showError(parent, "Database Error", 
                "Unable to process approval request due to database error.\nPlease try again or contact support.");
            return new ApprovalResult(true, false, null, "Database error");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during approval workflow", e);
            ErrorHandler.showError(parent, "System Error", 
                "An unexpected error occurred while processing your request.\nPlease try again or contact support.");
            return new ApprovalResult(true, false, null, "System error");
        }
    }
    
    /**
     * Determine the required approver role based on user role and amount
     */
    private String getRequiredApproverRole(User user, BigDecimal amount) {
        if (user.isTeller()) {
            return "Manager";
        } else if (user.isManager()) {
            // Large amounts may require admin approval
            if (amount.compareTo(new BigDecimal("10000")) > 0) {
                return "Administrator";
            } else {
                return "Senior Manager";
            }
        }
        return "Administrator";
    }
    
    /**
     * Show approval notification to user
     */
    public static void showApprovalNotification(Component parent, TransactionApproval approval, boolean approved) {
        try {
            UserDAO userDAO = new UserDAO();
            User approver = userDAO.findById(approval.getApprovedByUserId());
            String approverName = approver != null ? approver.getUsername() : "System";
            
            String comments = approved ? approval.getApprovalComments() : approval.getRejectionReason();
            if (comments == null || comments.trim().isEmpty()) {
                comments = approved ? "No comments provided" : "No reason provided";
            }
            
            ErrorHandler.showApprovalNotification(parent, approved, 
                approval.getTransactionType(), approval.getAmount().doubleValue(), 
                String.format("Reviewed by: %s\nComments: %s", approverName, comments));
                
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error retrieving approver information", e);
            ErrorHandler.showApprovalNotification(parent, approved, 
                approval.getTransactionType(), approval.getAmount().doubleValue(), 
                approved ? approval.getApprovalComments() : approval.getRejectionReason());
        }
    }
}
