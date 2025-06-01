package com.bankingsystem.services;

import com.bankingsystem.dao.AccountDAO;
import com.bankingsystem.dao.TransactionDAO;
import com.bankingsystem.exceptions.InsufficientFundsException;
import com.bankingsystem.exceptions.InvalidAccountException;
import com.bankingsystem.exceptions.TransactionLimitExceededException;
import com.bankingsystem.models.AbstractAccount;
import com.bankingsystem.models.Transaction;
import com.bankingsystem.models.TransactionApproval;
import com.bankingsystem.utils.EncryptionUtil;
import com.bankingsystem.utils.ValidationUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TransactionService {
    private static final Logger LOGGER = Logger.getLogger(TransactionService.class.getName());
    private final TransactionDAO transactionDAO;
    private final AccountDAO accountDAO;
    private final AccountService accountService;
    private final ApprovalService approvalService;

    public TransactionService() {
        this.transactionDAO = new TransactionDAO();
        this.accountDAO = new AccountDAO();
        this.accountService = new AccountService();
        this.approvalService = new ApprovalService();
    }

    public Transaction deposit(String accountNumber, BigDecimal amount, String description, Integer userId)
            throws SQLException, InvalidAccountException {

        if (!ValidationUtil.isValidAmount(amount)) {
            throw new IllegalArgumentException("Invalid deposit amount");
        }

        validateTransactionPermissions(userId, amount, "Deposit");

        AbstractAccount account = accountService.getAccountByNumber(accountNumber);

        if (!"Active".equals(account.getStatus())) {
            throw new InvalidAccountException("Account is not active", accountNumber);
        }

        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        String referenceNumber = EncryptionUtil.generateReferenceNumber();

        Integer transactionTypeId = transactionDAO.getTransactionTypeId("Deposit");

        Transaction transaction = new Transaction(
            account.getAccountId(),
            transactionTypeId,
            amount,
            balanceBefore,
            balanceAfter,
            description != null ? description : "Deposit",
            referenceNumber,
            userId
        );

        transaction.setTransactionTypeName("Deposit");

        try {
            transaction = transactionDAO.save(transaction);
            accountDAO.updateBalance(account.getAccountId(), balanceAfter);

            LOGGER.info(String.format("Deposit successful: $%.2f to account %s, ref: %s",
                                    amount, accountNumber, referenceNumber));
            return transaction;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Deposit transaction failed", e);
            throw e;
        }
    }

    public Transaction withdraw(String accountNumber, BigDecimal amount, String description, Integer userId)
            throws SQLException, InvalidAccountException, InsufficientFundsException, TransactionLimitExceededException {

        if (!ValidationUtil.isValidAmount(amount)) {
            throw new IllegalArgumentException("Invalid withdrawal amount");
        }

        validateTransactionPermissions(userId, amount, "Withdrawal");

        AbstractAccount account = accountService.getAccountByNumber(accountNumber);

        if (!"Active".equals(account.getStatus())) {
            throw new InvalidAccountException("Account is not active", accountNumber);
        }

        BigDecimal dailyLimit = account.getDailyWithdrawalLimit();
        BigDecimal dailyTotal = transactionDAO.getDailyWithdrawalTotal(account.getAccountId(), LocalDate.now());

        if (dailyTotal.add(amount).compareTo(dailyLimit) > 0) {
            throw new TransactionLimitExceededException(
                "Daily withdrawal limit exceeded", amount, dailyLimit, dailyTotal);
        }

        if (!account.canWithdraw(amount)) {
            throw new InsufficientFundsException(
                "Insufficient funds for withdrawal", account.getBalance(), amount);
        }

        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        String referenceNumber = EncryptionUtil.generateReferenceNumber();

        Integer transactionTypeId = transactionDAO.getTransactionTypeId("Withdrawal");

        Transaction transaction = new Transaction(
            account.getAccountId(),
            transactionTypeId,
            amount.negate(),
            balanceBefore,
            balanceAfter,
            description != null ? description : "Withdrawal",
            referenceNumber,
            userId
        );

        transaction.setTransactionTypeName("Withdrawal");

        try {
            transaction = transactionDAO.save(transaction);
            accountDAO.updateBalance(account.getAccountId(), balanceAfter);

            LOGGER.info(String.format("Withdrawal successful: $%.2f from account %s, ref: %s",
                                    amount, accountNumber, referenceNumber));
            return transaction;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Withdrawal transaction failed", e);
            throw e;
        }
    }

    public List<Transaction> transfer(String fromAccountNumber, String toAccountNumber,
                                    BigDecimal amount, String description, Integer userId)
            throws SQLException, InvalidAccountException, InsufficientFundsException, TransactionLimitExceededException {

        if (!ValidationUtil.isValidAmount(amount)) {
            throw new IllegalArgumentException("Invalid transfer amount");
        }

        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        AbstractAccount fromAccount = accountService.getAccountByNumber(fromAccountNumber);
        AbstractAccount toAccount = accountService.getAccountByNumber(toAccountNumber);

        if (!"Active".equals(fromAccount.getStatus())) {
            throw new InvalidAccountException("Source account is not active", fromAccountNumber);
        }

        if (!"Active".equals(toAccount.getStatus())) {
            throw new InvalidAccountException("Destination account is not active", toAccountNumber);
        }

        BigDecimal dailyLimit = fromAccount.getDailyWithdrawalLimit();
        BigDecimal dailyTotal = transactionDAO.getDailyWithdrawalTotal(fromAccount.getAccountId(), LocalDate.now());

        if (dailyTotal.add(amount).compareTo(dailyLimit) > 0) {
            throw new TransactionLimitExceededException(
                "Daily withdrawal limit exceeded", amount, dailyLimit, dailyTotal);
        }

        if (!fromAccount.canWithdraw(amount)) {
            throw new InsufficientFundsException(
                "Insufficient funds for transfer", fromAccount.getBalance(), amount);
        }

        String referenceNumber = EncryptionUtil.generateReferenceNumber();
        Integer transferTypeId = transactionDAO.getTransactionTypeId("Transfer");

        BigDecimal fromBalanceBefore = fromAccount.getBalance();
        BigDecimal fromBalanceAfter = fromBalanceBefore.subtract(amount);

        BigDecimal toBalanceBefore = toAccount.getBalance();
        BigDecimal toBalanceAfter = toBalanceBefore.add(amount);

        Transaction debitTransaction = new Transaction(
            fromAccount.getAccountId(),
            transferTypeId,
            amount.negate(),
            fromBalanceBefore,
            fromBalanceAfter,
            description != null ? description : "Transfer to " + toAccountNumber,
            referenceNumber,
            userId
        );
        debitTransaction.setTransactionTypeName("Transfer");

        Transaction creditTransaction = new Transaction(
            toAccount.getAccountId(),
            transferTypeId,
            amount,
            toBalanceBefore,
            toBalanceAfter,
            description != null ? description : "Transfer from " + fromAccountNumber,
            referenceNumber,
            userId
        );
        creditTransaction.setTransactionTypeName("Transfer");

        try {
            debitTransaction = transactionDAO.save(debitTransaction);
            creditTransaction = transactionDAO.save(creditTransaction);

            accountDAO.updateBalance(fromAccount.getAccountId(), fromBalanceAfter);
            accountDAO.updateBalance(toAccount.getAccountId(), toBalanceAfter);

            LOGGER.info(String.format("Transfer successful: $%.2f from %s to %s, ref: %s",
                                    amount, fromAccountNumber, toAccountNumber, referenceNumber));

            return List.of(debitTransaction, creditTransaction);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Transfer transaction failed", e);
            throw e;
        }
    }

    public List<Transaction> getTransactionHistory(String accountNumber) throws SQLException, InvalidAccountException {
        AbstractAccount account = accountService.getAccountByNumber(accountNumber);
        return transactionDAO.findByAccountId(account.getAccountId());
    }

    public List<Transaction> getTransactionHistory(String accountNumber, LocalDate startDate, LocalDate endDate)
            throws SQLException, InvalidAccountException {
        AbstractAccount account = accountService.getAccountByNumber(accountNumber);
        return transactionDAO.findByAccountIdAndDateRange(account.getAccountId(), startDate, endDate);
    }

    public Transaction getTransactionByReference(String referenceNumber) throws SQLException {
        return transactionDAO.findByReferenceNumber(referenceNumber);
    }

    public List<Transaction> getAllTransactions() throws SQLException {
        return transactionDAO.findAll();
    }

    public BigDecimal getDailyWithdrawalTotal(String accountNumber, LocalDate date)
            throws SQLException, InvalidAccountException {
        AbstractAccount account = accountService.getAccountByNumber(accountNumber);
        return transactionDAO.getDailyWithdrawalTotal(account.getAccountId(), date);
    }

    private void validateTransactionPermissions(Integer userId, BigDecimal amount, String transactionType)
            throws SQLException {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required for transaction processing");
        }

        try {
            com.bankingsystem.dao.UserDAO userDAO = new com.bankingsystem.dao.UserDAO();
            com.bankingsystem.models.User user = userDAO.findById(userId);

            if (user == null) {
                throw new IllegalArgumentException("Invalid user ID");
            }

            if (!user.canProcessTransactionAmount(amount)) {
                throw new IllegalArgumentException(
                    String.format("Transaction amount $%.2f exceeds your limit. %s limit: $%.2f",
                                amount, user.getRole(), getTransactionLimit(user)));
            }

            if (user.requiresApproval(amount)) {
                throw new IllegalArgumentException(
                    String.format("Transaction amount $%.2f requires manager approval. %s approval threshold: $%.2f",
                                amount, user.getRole(), getApprovalThreshold(user)));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to validate transaction permissions", e);
            throw e;
        }
    }

    private BigDecimal getTransactionLimit(com.bankingsystem.models.User user) {
        if (user.isAdmin()) {
            return new BigDecimal("999999.99");
        } else if (user.isManager()) {
            return com.bankingsystem.config.AppConfig.MANAGER_TRANSACTION_LIMIT;
        } else if (user.isTeller()) {
            return com.bankingsystem.config.AppConfig.TELLER_TRANSACTION_LIMIT;
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getApprovalThreshold(com.bankingsystem.models.User user) {
        if (user.isAdmin()) {
            return new BigDecimal("999999.99");
        } else if (user.isManager()) {
            return com.bankingsystem.config.AppConfig.MANAGER_APPROVAL_THRESHOLD;
        } else if (user.isTeller()) {
            return com.bankingsystem.config.AppConfig.TELLER_APPROVAL_THRESHOLD;
        }
        return BigDecimal.ZERO;
    }

    public boolean deleteTransactionPermanently(Integer transactionId) throws SQLException {
        try {
            com.bankingsystem.dao.TransactionDAO transactionDAO = new com.bankingsystem.dao.TransactionDAO();

            if (!transactionDAO.canDeleteTransaction(transactionId)) {
                throw new IllegalArgumentException("Cannot permanently delete completed transaction older than 24 hours");
            }

            return transactionDAO.deletePermanently(transactionId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to permanently delete transaction", e);
            throw e;
        }
    }

    public boolean canDeleteTransactionPermanently(Integer transactionId) throws SQLException {
        try {
            com.bankingsystem.dao.TransactionDAO transactionDAO = new com.bankingsystem.dao.TransactionDAO();
            return transactionDAO.canDeleteTransaction(transactionId);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean cancelTransaction(Integer transactionId) throws SQLException {
        try {
            com.bankingsystem.dao.TransactionDAO transactionDAO = new com.bankingsystem.dao.TransactionDAO();
            return transactionDAO.delete(transactionId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to cancel transaction", e);
            throw e;
        }
    }

    // Approval-related methods

    /**
     * Attempts to deposit with approval workflow
     */
    public Object depositWithApproval(String accountNumber, BigDecimal amount, String description, Integer userId)
            throws SQLException, InvalidAccountException {

        if (!ValidationUtil.isValidAmount(amount)) {
            throw new IllegalArgumentException("Invalid deposit amount");
        }

        // Check if user can process the amount directly
        if (!approvalService.requiresApproval(userId, amount)) {
            // Process directly
            return deposit(accountNumber, amount, description, userId);
        }

        // Check if user can self-approve
        if (approvalService.canSelfApprove(userId, amount)) {
            // Self-approve and process
            TransactionApproval approval = approvalService.createApprovalRequest(
                "Deposit", accountNumber, amount, description, userId);
            approvalService.approveRequest(approval.getApprovalId(), userId, "Self-approved");
            return processApprovedDeposit(approval);
        }

        // Create approval request
        return approvalService.createApprovalRequest("Deposit", accountNumber, amount, description, userId);
    }

    /**
     * Processes an approved deposit
     */
    public Transaction processApprovedDeposit(TransactionApproval approval)
            throws SQLException, InvalidAccountException {

        if (!approval.isApproved()) {
            throw new IllegalArgumentException("Transaction approval is not approved");
        }

        // Process the deposit without permission validation (already approved)
        return depositWithoutValidation(approval.getAccountNumber(), approval.getAmount(),
                                      approval.getDescription(), approval.getRequestedByUserId());
    }

    /**
     * Internal deposit method without permission validation (for approved transactions)
     */
    private Transaction depositWithoutValidation(String accountNumber, BigDecimal amount,
                                               String description, Integer userId)
            throws SQLException, InvalidAccountException {

        AbstractAccount account = accountService.getAccountByNumber(accountNumber);

        if (!"Active".equals(account.getStatus())) {
            throw new InvalidAccountException("Account is not active", accountNumber);
        }

        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        String referenceNumber = EncryptionUtil.generateReferenceNumber();

        Integer transactionTypeId = transactionDAO.getTransactionTypeId("Deposit");

        Transaction transaction = new Transaction(
            account.getAccountId(),
            transactionTypeId,
            amount,
            balanceBefore,
            balanceAfter,
            description != null ? description : "Deposit",
            referenceNumber,
            userId
        );

        transaction.setTransactionTypeName("Deposit");

        try {
            transaction = transactionDAO.save(transaction);
            accountDAO.updateBalance(account.getAccountId(), balanceAfter);

            LOGGER.info(String.format("Approved deposit successful: $%.2f to account %s, ref: %s",
                                    amount, accountNumber, referenceNumber));
            return transaction;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Approved deposit transaction failed", e);
            throw e;
        }
    }
}
