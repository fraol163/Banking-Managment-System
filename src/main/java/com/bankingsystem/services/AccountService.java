package com.bankingsystem.services;

import com.bankingsystem.dao.AccountDAO;
import com.bankingsystem.dao.CustomerDAO;
import com.bankingsystem.exceptions.InvalidAccountException;
import com.bankingsystem.models.*;
import com.bankingsystem.utils.EncryptionUtil;
import com.bankingsystem.utils.ValidationUtil;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AccountService {
    private static final Logger LOGGER = Logger.getLogger(AccountService.class.getName());
    private final AccountDAO accountDAO;
    private final CustomerDAO customerDAO;

    public AccountService() {
        this.accountDAO = new AccountDAO();
        this.customerDAO = new CustomerDAO();
    }

    public AbstractAccount createAccount(Integer customerId, String accountType, BigDecimal initialDeposit)
            throws SQLException, InvalidAccountException {

        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            throw new InvalidAccountException("Customer not found", customerId.toString());
        }

        if (initialDeposit == null || !ValidationUtil.isValidAmount(initialDeposit)) {
            throw new IllegalArgumentException("Invalid initial deposit amount");
        }

        String accountNumber = generateUniqueAccountNumber();
        Integer accountTypeId = getAccountTypeId(accountType);

        AbstractAccount account = createAccountByType(accountType, accountNumber, customerId, accountTypeId);

        if (initialDeposit.compareTo(account.getMinimumBalance()) < 0) {
            throw new IllegalArgumentException(
                String.format("Initial deposit $%.2f is below minimum balance requirement $%.2f",
                            initialDeposit, account.getMinimumBalance()));
        }

        account.setBalance(initialDeposit);
        account = accountDAO.save(account);

        LOGGER.info(String.format("Account created successfully: %s for customer %d with balance $%.2f",
                                accountNumber, customerId, initialDeposit));
        return account;
    }

    private AbstractAccount createAccountByType(String accountType, String accountNumber,
                                              Integer customerId, Integer accountTypeId) {
        switch (accountType) {
            case "Savings":
                return new SavingsAccount(accountNumber, customerId, accountTypeId);
            case "Checking":
                return new CheckingAccount(accountNumber, customerId, accountTypeId);
            case "Business":
                return new BusinessAccount(accountNumber, customerId, accountTypeId);
            default:
                throw new IllegalArgumentException("Invalid account type: " + accountType);
        }
    }

    private Integer getAccountTypeId(String accountType) {
        switch (accountType) {
            case "Savings":
                return 1;
            case "Checking":
                return 2;
            case "Business":
                return 3;
            default:
                throw new IllegalArgumentException("Invalid account type: " + accountType);
        }
    }

    private String generateUniqueAccountNumber() throws SQLException {
        String accountNumber;
        int attempts = 0;
        int maxAttempts = 100;

        do {
            accountNumber = EncryptionUtil.generateAccountNumber();
            attempts++;

            if (attempts > maxAttempts) {
                throw new RuntimeException("Failed to generate unique account number after " + maxAttempts + " attempts");
            }
        } while (accountDAO.findByAccountNumber(accountNumber) != null);

        return accountNumber;
    }

    public AbstractAccount getAccountById(Integer accountId) throws SQLException {
        return accountDAO.findById(accountId);
    }

    public AbstractAccount getAccountByNumber(String accountNumber) throws SQLException, InvalidAccountException {
        if (!ValidationUtil.isValidAccountNumber(accountNumber)) {
            throw new InvalidAccountException("Invalid account number format", accountNumber);
        }

        AbstractAccount account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new InvalidAccountException("Account not found", accountNumber);
        }

        return account;
    }

    public List<AbstractAccount> getAccountsByCustomerId(Integer customerId) throws SQLException {
        return accountDAO.findByCustomerId(customerId);
    }

    public List<AbstractAccount> getAllAccounts() throws SQLException {
        return accountDAO.findAll();
    }

    public AbstractAccount updateAccount(AbstractAccount account) throws SQLException {
        if (account == null || account.getAccountId() == null) {
            throw new IllegalArgumentException("Invalid account data");
        }

        return accountDAO.save(account);
    }

    public boolean updateAccountStatus(Integer accountId, String newStatus) throws SQLException {
        AbstractAccount account = accountDAO.findById(accountId);
        if (account == null) {
            return false;
        }

        if (!"Active".equals(newStatus) && !"Suspended".equals(newStatus) && !"Closed".equals(newStatus)) {
            throw new IllegalArgumentException("Invalid account status: " + newStatus);
        }

        if ("Closed".equals(newStatus) && account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Cannot close account with non-zero balance");
        }

        account.setStatus(newStatus);
        accountDAO.save(account);

        LOGGER.info(String.format("Account status updated: %s to %s", account.getAccountNumber(), newStatus));
        return true;
    }

    public boolean closeAccount(Integer accountId) throws SQLException {
        AbstractAccount account = accountDAO.findById(accountId);
        if (account == null) {
            return false;
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Cannot close account with non-zero balance. Current balance: $" + account.getBalance());
        }

        return updateAccountStatus(accountId, "Closed");
    }

    public BigDecimal getAccountBalance(String accountNumber) throws SQLException, InvalidAccountException {
        AbstractAccount account = getAccountByNumber(accountNumber);
        return account.getBalance();
    }

    public boolean isAccountActive(String accountNumber) throws SQLException, InvalidAccountException {
        AbstractAccount account = getAccountByNumber(accountNumber);
        return "Active".equals(account.getStatus());
    }

    public boolean canWithdraw(String accountNumber, BigDecimal amount) throws SQLException, InvalidAccountException {
        AbstractAccount account = getAccountByNumber(accountNumber);

        if (!"Active".equals(account.getStatus())) {
            return false;
        }

        return account.canWithdraw(amount);
    }

    public BigDecimal calculateMonthlyInterest(Integer accountId) throws SQLException {
        AbstractAccount account = accountDAO.findById(accountId);
        if (account == null) {
            return BigDecimal.ZERO;
        }

        return account.calculateInterest();
    }

    public void postMonthlyInterest(Integer accountId) throws SQLException {
        AbstractAccount account = accountDAO.findById(accountId);
        if (account == null || account.getInterestRate().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        BigDecimal interest = account.calculateInterest();
        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal newBalance = account.getBalance().add(interest);
            accountDAO.updateBalance(accountId, newBalance);

            LOGGER.info(String.format("Monthly interest posted: $%.2f to account %s",
                                    interest, account.getAccountNumber()));
        }
    }

    public boolean deleteAccountPermanently(Integer accountId) throws SQLException, InvalidAccountException {
        return deleteAccountPermanently(accountId, false);
    }

    public boolean deleteAccountPermanently(Integer accountId, boolean forceDelete) throws SQLException, InvalidAccountException {
        AbstractAccount account = accountDAO.findById(accountId);
        if (account == null) {
            throw new InvalidAccountException("Account not found", accountId.toString());
        }

        if (!forceDelete && !accountDAO.canDeleteAccount(accountId)) {
            throw new IllegalArgumentException("Cannot permanently delete account with non-zero balance. Use force delete for accounts with balances.");
        }

        return accountDAO.deletePermanently(accountId);
    }

    public boolean canDeleteAccountPermanently(Integer accountId) throws SQLException {
        try {
            return accountDAO.canDeleteAccount(accountId);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasTransactions(Integer accountId) throws SQLException {
        return accountDAO.hasTransactions(accountId);
    }
}
