package com.bankingsystem.services;

import com.bankingsystem.models.TransactionApproval;
import com.bankingsystem.models.User;
import com.bankingsystem.dao.UserDAO;
import com.bankingsystem.utils.MockDatabaseUtil;
import com.bankingsystem.config.AppConfig;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ApprovalService {
    private static final Logger LOGGER = Logger.getLogger(ApprovalService.class.getName());
    private final UserDAO userDAO;
    
    public ApprovalService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * Determines if a transaction requires approval based on user role and amount
     */
    public boolean requiresApproval(Integer userId, BigDecimal amount) throws SQLException {
        User user = userDAO.findById(userId);
        if (user == null) {
            return true; // Unknown user requires approval
        }
        
        return user.requiresApproval(amount);
    }
    
    /**
     * Determines if a user can approve their own transaction
     */
    public boolean canSelfApprove(Integer userId, BigDecimal amount) throws SQLException {
        User user = userDAO.findById(userId);
        if (user == null) {
            return false;
        }
        
        // Admins can always self-approve
        if (user.isAdmin()) {
            return true;
        }
        
        // Managers can self-approve if they have the authority for the amount
        if (user.isManager()) {
            return user.canProcessTransactionAmount(amount);
        }
        
        // Tellers cannot self-approve large transactions
        return false;
    }
    
    /**
     * Creates a new approval request
     */
    public TransactionApproval createApprovalRequest(String transactionType, String accountNumber, 
                                                   BigDecimal amount, String description, Integer userId) throws SQLException {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        
        TransactionApproval approval = new TransactionApproval(
            transactionType, accountNumber, amount, description, userId, user.getRole()
        );
        
        // Save to mock database
        approval = MockDatabaseUtil.saveTransactionApproval(approval);
        
        LOGGER.info(String.format("Approval request created: %s for $%.2f by user %d (%s)", 
                                transactionType, amount, userId, user.getRole()));
        
        return approval;
    }
    
    /**
     * Approves a transaction approval request
     */
    public TransactionApproval approveRequest(Integer approvalId, Integer approvingUserId, String comments) throws SQLException {
        TransactionApproval approval = MockDatabaseUtil.findTransactionApprovalById(approvalId);
        if (approval == null) {
            throw new IllegalArgumentException("Approval request not found");
        }
        
        if (!approval.isPending()) {
            throw new IllegalArgumentException("Approval request is not pending");
        }
        
        User approvingUser = userDAO.findById(approvingUserId);
        if (approvingUser == null) {
            throw new IllegalArgumentException("Invalid approving user ID");
        }
        
        // Check if user has authority to approve
        if (!canUserApprove(approvingUser, approval)) {
            throw new IllegalArgumentException("User does not have authority to approve this transaction");
        }
        
        approval.approve(approvingUserId, comments);
        MockDatabaseUtil.saveTransactionApproval(approval);
        
        LOGGER.info(String.format("Transaction approved: ID %d by user %d (%s)", 
                                approvalId, approvingUserId, approvingUser.getRole()));
        
        return approval;
    }
    
    /**
     * Rejects a transaction approval request
     */
    public TransactionApproval rejectRequest(Integer approvalId, Integer rejectingUserId, String reason) throws SQLException {
        TransactionApproval approval = MockDatabaseUtil.findTransactionApprovalById(approvalId);
        if (approval == null) {
            throw new IllegalArgumentException("Approval request not found");
        }
        
        if (!approval.isPending()) {
            throw new IllegalArgumentException("Approval request is not pending");
        }
        
        User rejectingUser = userDAO.findById(rejectingUserId);
        if (rejectingUser == null) {
            throw new IllegalArgumentException("Invalid rejecting user ID");
        }
        
        // Check if user has authority to reject
        if (!canUserApprove(rejectingUser, approval)) {
            throw new IllegalArgumentException("User does not have authority to reject this transaction");
        }
        
        approval.reject(rejectingUserId, reason);
        MockDatabaseUtil.saveTransactionApproval(approval);
        
        LOGGER.info(String.format("Transaction rejected: ID %d by user %d (%s)", 
                                approvalId, rejectingUserId, rejectingUser.getRole()));
        
        return approval;
    }
    
    /**
     * Gets all pending approval requests that a user can approve
     */
    public List<TransactionApproval> getPendingApprovalsForUser(Integer userId) throws SQLException {
        User user = userDAO.findById(userId);
        if (user == null) {
            return new ArrayList<>();
        }
        
        List<TransactionApproval> allPending = MockDatabaseUtil.getPendingTransactionApprovals();
        
        return allPending.stream()
            .filter(approval -> canUserApprove(user, approval))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets all approval requests made by a specific user
     */
    public List<TransactionApproval> getApprovalRequestsByUser(Integer userId) {
        return MockDatabaseUtil.getTransactionApprovalsByUser(userId);
    }
    
    /**
     * Checks if a user can approve a specific transaction
     */
    private boolean canUserApprove(User user, TransactionApproval approval) {
        // Users cannot approve their own requests (except self-approval cases)
        if (approval.getRequestedByUserId().equals(user.getUserId())) {
            try {
                return canSelfApprove(user.getUserId(), approval.getAmount());
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error checking self-approval capability", e);
                return false;
            }
        }
        
        // Admins can approve anything
        if (user.isAdmin()) {
            return true;
        }
        
        // Managers can approve teller requests and smaller manager requests
        if (user.isManager()) {
            // Can approve teller requests
            if (AppConfig.ROLE_TELLER.equals(approval.getRequestedByUserRole())) {
                return true;
            }
            
            // Can approve manager requests if within their authority
            if (AppConfig.ROLE_MANAGER.equals(approval.getRequestedByUserRole())) {
                return user.canProcessTransactionAmount(approval.getAmount());
            }
        }
        
        // Tellers cannot approve anything
        return false;
    }
    
    /**
     * Gets approval request by ID
     */
    public TransactionApproval getApprovalById(Integer approvalId) {
        return MockDatabaseUtil.findTransactionApprovalById(approvalId);
    }
    
    /**
     * Gets all approval requests (for admin/reporting purposes)
     */
    public List<TransactionApproval> getAllApprovalRequests() {
        return MockDatabaseUtil.getAllTransactionApprovals();
    }
}
