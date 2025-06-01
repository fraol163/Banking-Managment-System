package com.bankingsystem.utils;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Enhanced error handling utility for user-friendly error messages
 */
public class ErrorHandler {
    private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class.getName());
    
    /**
     * Show user-friendly error dialog with helpful information
     */
    public static void showError(Component parent, String title, String message) {
        showError(parent, title, message, null);
    }
    
    /**
     * Show user-friendly error dialog with exception logging
     */
    public static void showError(Component parent, String title, String message, Exception exception) {
        // Log the technical details
        if (exception != null) {
            LOGGER.log(Level.WARNING, "User-facing error: " + title + " - " + message, exception);
        } else {
            LOGGER.warning("User-facing error: " + title + " - " + message);
        }
        
        // Show user-friendly dialog
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show validation error with helpful suggestions
     */
    public static void showValidationError(Component parent, String message) {
        showError(parent, "Input Validation Error", message);
    }
    
    /**
     * Show transaction error with specific guidance
     */
    public static void showTransactionError(Component parent, String message) {
        showError(parent, "Transaction Error", message);
    }
    
    /**
     * Show account error with helpful information
     */
    public static void showAccountError(Component parent, String message) {
        showError(parent, "Account Error", message);
    }
    
    /**
     * Show permission error with role information
     */
    public static void showPermissionError(Component parent, String requiredPermission) {
        String message = String.format(
            "You do not have permission to perform this action.\n\n" +
            "Required permission: %s\n\n" +
            "Please contact your administrator if you believe you should have access to this feature.",
            requiredPermission
        );
        showError(parent, "Permission Denied", message);
    }
    
    /**
     * Show insufficient funds error with balance information
     */
    public static void showInsufficientFundsError(Component parent, double requestedAmount, double availableBalance) {
        String message = String.format(
            "Insufficient funds for this transaction.\n\n" +
            "Requested amount: $%.2f\n" +
            "Available balance: $%.2f\n" +
            "Shortage: $%.2f\n\n" +
            "Please reduce the transaction amount or make a deposit first.",
            requestedAmount, availableBalance, requestedAmount - availableBalance
        );
        showTransactionError(parent, message);
    }
    
    /**
     * Show approval required message with tracking information
     */
    public static void showApprovalRequired(Component parent, String transactionType, double amount, int approvalId) {
        String message = String.format(
            "Manager approval required for this transaction.\n\n" +
            "Transaction type: %s\n" +
            "Amount: $%.2f\n" +
            "Approval request ID: %d\n\n" +
            "Your request has been sent to a manager for approval.\n" +
            "You will be notified once the decision is made.",
            transactionType, amount, approvalId
        );
        
        JOptionPane.showMessageDialog(parent, message, "Approval Required", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show approval notification (approved/rejected)
     */
    public static void showApprovalNotification(Component parent, boolean approved, String transactionType, 
                                              double amount, String managerComments) {
        String status = approved ? "APPROVED" : "REJECTED";
        String title = "Transaction " + status;
        int messageType = approved ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE;
        
        String message = String.format(
            "Your %s request for $%.2f has been %s.\n\n" +
            "Manager comments: %s\n\n" +
            "%s",
            transactionType, amount, status.toLowerCase(),
            managerComments != null && !managerComments.trim().isEmpty() ? managerComments : "No comments provided",
            approved ? "The transaction has been processed." : "Please contact your manager for more information."
        );
        
        JOptionPane.showMessageDialog(parent, message, title, messageType);
    }
    
    /**
     * Show connection error with troubleshooting tips
     */
    public static void showConnectionError(Component parent, String operation) {
        String message = String.format(
            "Unable to complete %s due to a connection issue.\n\n" +
            "Troubleshooting steps:\n" +
            "1. Check your network connection\n" +
            "2. Verify the database service is running\n" +
            "3. Contact your system administrator if the problem persists\n\n" +
            "Please try again in a few moments.",
            operation
        );
        showError(parent, "Connection Error", message);
    }
    
    /**
     * Show data not found error with helpful suggestions
     */
    public static void showDataNotFoundError(Component parent, String dataType, String identifier) {
        String message = String.format(
            "%s not found: %s\n\n" +
            "Possible reasons:\n" +
            "• The %s may have been deleted\n" +
            "• You may have entered an incorrect identifier\n" +
            "• You may not have permission to access this %s\n\n" +
            "Please verify the information and try again.",
            dataType, identifier, dataType.toLowerCase(), dataType.toLowerCase()
        );
        showError(parent, dataType + " Not Found", message);
    }
    
    /**
     * Show success message with transaction details
     */
    public static void showSuccess(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show transaction success with reference number
     */
    public static void showTransactionSuccess(Component parent, String transactionType, double amount, String referenceNumber) {
        String message = String.format(
            "%s completed successfully!\n\n" +
            "Amount: $%.2f\n" +
            "Reference number: %s\n" +
            "Date: %s\n\n" +
            "Please keep the reference number for your records.",
            transactionType, amount, referenceNumber, 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        showSuccess(parent, "Transaction Successful", message);
    }
    
    /**
     * Show warning message with actionable information
     */
    public static void showWarning(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Show confirmation dialog with clear options
     */
    public static boolean showConfirmation(Component parent, String title, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, title, 
                                                 JOptionPane.YES_NO_OPTION, 
                                                 JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Show confirmation dialog for potentially destructive actions
     */
    public static boolean showDestructiveConfirmation(Component parent, String action, String target) {
        String message = String.format(
            "Are you sure you want to %s?\n\n" +
            "Target: %s\n\n" +
            "This action cannot be undone.\n" +
            "Click 'Yes' to proceed or 'No' to cancel.",
            action, target
        );
        
        int result = JOptionPane.showConfirmDialog(parent, message, "Confirm " + action, 
                                                 JOptionPane.YES_NO_OPTION, 
                                                 JOptionPane.WARNING_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Convert technical exception to user-friendly message
     */
    public static String getUserFriendlyMessage(Exception exception) {
        String message = exception.getMessage();
        
        // Handle common technical exceptions
        if (exception instanceof java.sql.SQLException) {
            return "Database operation failed. Please try again or contact support.";
        } else if (exception instanceof java.net.ConnectException) {
            return "Unable to connect to the database. Please check your connection.";
        } else if (exception instanceof IllegalArgumentException) {
            // Try to extract useful information from IllegalArgumentException
            if (message != null && message.contains("requires manager approval")) {
                return "This transaction requires manager approval due to the amount or your permission level.";
            } else if (message != null && message.contains("Insufficient funds")) {
                return "Insufficient funds for this transaction. Please check your account balance.";
            } else {
                return "Invalid input provided. Please check your entries and try again.";
            }
        } else if (exception instanceof SecurityException) {
            return "You do not have permission to perform this action.";
        } else if (exception instanceof RuntimeException && message != null) {
            // Try to make runtime exceptions more user-friendly
            if (message.contains("not found")) {
                return "The requested information could not be found.";
            } else if (message.contains("already exists")) {
                return "This item already exists. Please use a different identifier.";
            }
        }
        
        // Default user-friendly message
        return "An unexpected error occurred. Please try again or contact support if the problem persists.";
    }
}
