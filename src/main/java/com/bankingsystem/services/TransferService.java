package com.bankingsystem.services;

import com.bankingsystem.dao.AccountDAO;
import com.bankingsystem.dao.TransactionDAO;
import com.bankingsystem.dao.UserDAO;
import com.bankingsystem.exceptions.InsufficientFundsException;
import com.bankingsystem.exceptions.InvalidAccountException;
import com.bankingsystem.exceptions.TransferLimitExceededException;
import com.bankingsystem.models.AbstractAccount;
import com.bankingsystem.models.Transaction;
import com.bankingsystem.models.TransferRequest;
import com.bankingsystem.models.TransferResult;
import com.bankingsystem.models.User;
import com.bankingsystem.utils.EncryptionUtil;
import com.bankingsystem.utils.ValidationUtil;
import com.bankingsystem.config.AppConfig;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TransferService {
    private static final Logger LOGGER = Logger.getLogger(TransferService.class.getName());
    
    private AccountService accountService;
    private TransactionService transactionService;
    private UserService userService;
    private AccountDAO accountDAO;
    private TransactionDAO transactionDAO;
    private UserDAO userDAO;
    
    public TransferService(AccountService accountService, TransactionService transactionService, UserService userService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.userService = userService;
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
        this.userDAO = new UserDAO();
    }
    
    public TransferResult performTransfer(TransferRequest request) throws SQLException, InvalidAccountException, 
            InsufficientFundsException, TransferLimitExceededException {
        
        validateTransferRequest(request);
        
        AbstractAccount fromAccount = accountService.getAccountByNumber(request.getFromAccountNumber());
        AbstractAccount toAccount = accountService.getAccountByNumber(request.getToAccountNumber());
        
        validateTransferAccounts(fromAccount, toAccount, request);
        validateTransferPermissions(request);
        validateTransferLimits(fromAccount, request);
        
        String referenceNumber = EncryptionUtil.generateReferenceNumber();
        
        List<Transaction> transactions = executeTransfer(fromAccount, toAccount, request, referenceNumber);
        
        logTransferOperation(request, referenceNumber, transactions);
        
        return new TransferResult(true, "Transfer completed successfully", referenceNumber, transactions);
    }
    
    private void validateTransferRequest(TransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Transfer request cannot be null");
        }
        
        if (request.getFromAccountNumber() == null || request.getFromAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("From account number is required");
        }
        
        if (request.getToAccountNumber() == null || request.getToAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("To account number is required");
        }
        
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        
        if (request.getAmount() == null || !ValidationUtil.isValidAmount(request.getAmount())) {
            throw new IllegalArgumentException("Invalid transfer amount");
        }
        
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required for transfer processing");
        }
    }
    
    private void validateTransferAccounts(AbstractAccount fromAccount, AbstractAccount toAccount, TransferRequest request) 
            throws InvalidAccountException {
        
        if (fromAccount == null) {
            throw new InvalidAccountException("From account not found", request.getFromAccountNumber());
        }
        
        if (toAccount == null) {
            throw new InvalidAccountException("To account not found", request.getToAccountNumber());
        }
        
        if (!AppConfig.ACCOUNT_STATUS_ACTIVE.equals(fromAccount.getStatus())) {
            throw new InvalidAccountException("From account is not active", request.getFromAccountNumber());
        }
        
        if (!AppConfig.ACCOUNT_STATUS_ACTIVE.equals(toAccount.getStatus())) {
            throw new InvalidAccountException("To account is not active", request.getToAccountNumber());
        }
    }
    
    private void validateTransferPermissions(TransferRequest request) throws SQLException, TransferLimitExceededException {
        User user = userDAO.findById(request.getUserId());
        if (user == null) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        
        if (!user.hasPermission(AppConfig.PERMISSION_PROCESS_TRANSACTION)) {
            throw new IllegalArgumentException("User does not have permission to process transfers");
        }
        
        if (!user.canProcessTransactionAmount(request.getAmount())) {
            throw new TransferLimitExceededException(
                String.format("Transfer amount $%.2f exceeds your limit. %s limit: $%.2f",
                            request.getAmount(), user.getRole(), getTransactionLimit(user)));
        }
        
        if (user.requiresApproval(request.getAmount())) {
            throw new TransferLimitExceededException(
                String.format("Transfer amount $%.2f requires manager approval. %s approval threshold: $%.2f",
                            request.getAmount(), user.getRole(), getApprovalThreshold(user)));
        }
    }
    
    private void validateTransferLimits(AbstractAccount fromAccount, TransferRequest request) 
            throws InsufficientFundsException, TransferLimitExceededException, SQLException {
        
        BigDecimal availableBalance = fromAccount.getBalance();
        if (availableBalance.compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(
                "Insufficient funds for transfer", availableBalance, request.getAmount());
        }
        
        BigDecimal dailyTransferTotal = getDailyTransferTotal(fromAccount.getAccountNumber(), LocalDate.now());
        BigDecimal totalWithNewTransfer = dailyTransferTotal.add(request.getAmount());
        BigDecimal dailyLimit = fromAccount.getDailyWithdrawalLimit();
        
        if (totalWithNewTransfer.compareTo(dailyLimit) > 0) {
            throw new TransferLimitExceededException(
                String.format("Daily transfer limit exceeded. Daily limit: $%.2f, Today's total: $%.2f, Requested: $%.2f",
                            dailyLimit, dailyTransferTotal, request.getAmount()));
        }
    }
    
    private List<Transaction> executeTransfer(AbstractAccount fromAccount, AbstractAccount toAccount, 
            TransferRequest request, String referenceNumber) throws SQLException {
        
        try {
            List<Transaction> transactions = transactionService.transfer(
                fromAccount.getAccountNumber(),
                toAccount.getAccountNumber(),
                request.getAmount(),
                request.getDescription(),
                request.getUserId()
            );
            
            LOGGER.info(String.format("Transfer executed successfully: $%.2f from %s to %s, ref: %s, user: %d",
                       request.getAmount(), fromAccount.getAccountNumber(), toAccount.getAccountNumber(), 
                       referenceNumber, request.getUserId()));
            
            return transactions;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Transfer execution failed", e);
            throw new SQLException("Transfer execution failed: " + e.getMessage(), e);
        }
    }
    
    private void logTransferOperation(TransferRequest request, String referenceNumber, List<Transaction> transactions) {
        LOGGER.warning(String.format("TRANSFER OPERATION: User %d transferred $%.2f from %s to %s (Ref: %s) - %s",
                      request.getUserId(), request.getAmount(), request.getFromAccountNumber(), 
                      request.getToAccountNumber(), referenceNumber, 
                      request.getDescription() != null ? request.getDescription() : "No description"));
    }
    
    public BigDecimal getDailyTransferTotal(String accountNumber, LocalDate date) throws SQLException {
        try {
            AbstractAccount account = accountService.getAccountByNumber(accountNumber);
            return transactionDAO.getDailyWithdrawalTotal(account.getAccountId(), date);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get daily transfer total for account: " + accountNumber, e);
            return BigDecimal.ZERO;
        }
    }
    
    public boolean canPerformTransfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount, Integer userId) {
        try {
            TransferRequest request = new TransferRequest(fromAccountNumber, toAccountNumber, amount, 
                                                        "Validation check", userId);
            validateTransferRequest(request);
            
            AbstractAccount fromAccount = accountService.getAccountByNumber(fromAccountNumber);
            AbstractAccount toAccount = accountService.getAccountByNumber(toAccountNumber);
            
            validateTransferAccounts(fromAccount, toAccount, request);
            validateTransferPermissions(request);
            validateTransferLimits(fromAccount, request);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private BigDecimal getTransactionLimit(User user) {
        if (user.isAdmin()) {
            return new BigDecimal("999999.99");
        } else if (user.isManager()) {
            return AppConfig.MANAGER_TRANSACTION_LIMIT;
        } else if (user.isTeller()) {
            return AppConfig.TELLER_TRANSACTION_LIMIT;
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal getApprovalThreshold(User user) {
        if (user.isAdmin()) {
            return new BigDecimal("999999.99");
        } else if (user.isManager()) {
            return AppConfig.MANAGER_APPROVAL_THRESHOLD;
        } else if (user.isTeller()) {
            return AppConfig.TELLER_APPROVAL_THRESHOLD;
        }
        return BigDecimal.ZERO;
    }
}
