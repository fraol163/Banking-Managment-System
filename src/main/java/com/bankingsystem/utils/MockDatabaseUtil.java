package com.bankingsystem.utils;

import com.bankingsystem.models.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MockDatabaseUtil {
    private static final Logger LOGGER = Logger.getLogger(MockDatabaseUtil.class.getName());

    private static final Map<Integer, User> users = new ConcurrentHashMap<>();
    private static final Map<Integer, Customer> customers = new ConcurrentHashMap<>();
    private static final Map<Integer, AbstractAccount> accounts = new ConcurrentHashMap<>();
    private static final Map<Integer, Transaction> transactions = new ConcurrentHashMap<>();
    private static final Map<String, AbstractAccount> accountsByNumber = new ConcurrentHashMap<>();
    private static final Map<Integer, TransactionApproval> transactionApprovals = new ConcurrentHashMap<>();

    private static final AtomicInteger userIdCounter = new AtomicInteger(1);
    private static final AtomicInteger customerIdCounter = new AtomicInteger(1);
    private static final AtomicInteger accountIdCounter = new AtomicInteger(1);
    private static final AtomicInteger transactionIdCounter = new AtomicInteger(1);
    private static final AtomicInteger approvalIdCounter = new AtomicInteger(1);

    private static boolean dataInitialized = false;

    static {
        // Only initialize data if explicitly requested or on first access
        // This allows the application to start with empty data if desired
    }

    public static void initializeData() {
        initializeData(true);
    }

    public static void initializeData(boolean withSampleData) {
        if (dataInitialized) {
            return; // Prevent double initialization
        }

        // Always load system users for authentication
        loadSystemUsers();

        if (withSampleData) {
            loadSampleCustomersAndAccounts();
        }
        dataInitialized = true;
    }

    private static void loadSystemUsers() {
        User admin = new User("admin", "OReq3vmOSzED0rYGoV3ca9eDcw/4ReCnrOSrZ6hPi5s=", "Admin", "System", "Administrator", "admin@bank.com");
        admin.setUserId(userIdCounter.getAndIncrement());
        users.put(admin.getUserId(), admin);

        User manager = new User("manager", "OReq3vmOSzED0rYGoV3ca9eDcw/4ReCnrOSrZ6hPi5s=", "Manager", "Bank", "Manager", "manager@bank.com");
        manager.setUserId(userIdCounter.getAndIncrement());
        users.put(manager.getUserId(), manager);

        User teller = new User("teller", "OReq3vmOSzED0rYGoV3ca9eDcw/4ReCnrOSrZ6hPi5s=", "Teller", "Bank", "Teller", "teller@bank.com");
        teller.setUserId(userIdCounter.getAndIncrement());
        users.put(teller.getUserId(), teller);

        LOGGER.info("System users loaded for authentication");
    }

    private static void loadSampleCustomersAndAccounts() {

        Customer customer1 = new Customer("John", "Smith", "john.smith@email.com", "(555) 123-4567", "123 Main St", LocalDate.of(1985, 3, 15), "123-45-6789");
        customer1.setCustomerId(customerIdCounter.getAndIncrement());
        customers.put(customer1.getCustomerId(), customer1);

        Customer customer2 = new Customer("Jane", "Johnson", "jane.johnson@email.com", "(555) 234-5678", "456 Oak Ave", LocalDate.of(1990, 7, 22), "234-56-7890");
        customer2.setCustomerId(customerIdCounter.getAndIncrement());
        customers.put(customer2.getCustomerId(), customer2);

        SavingsAccount account1 = new SavingsAccount("1234567890", customer1.getCustomerId(), 1);
        account1.setAccountId(accountIdCounter.getAndIncrement());
        account1.setBalance(new BigDecimal("2500.00"));
        accounts.put(account1.getAccountId(), account1);
        accountsByNumber.put(account1.getAccountNumber(), account1);

        CheckingAccount account2 = new CheckingAccount("2345678901", customer1.getCustomerId(), 2);
        account2.setAccountId(accountIdCounter.getAndIncrement());
        account2.setBalance(new BigDecimal("1200.50"));
        accounts.put(account2.getAccountId(), account2);
        accountsByNumber.put(account2.getAccountNumber(), account2);

        SavingsAccount account3 = new SavingsAccount("3456789012", customer2.getCustomerId(), 1);
        account3.setAccountId(accountIdCounter.getAndIncrement());
        account3.setBalance(new BigDecimal("5000.00"));
        accounts.put(account3.getAccountId(), account3);
        accountsByNumber.put(account3.getAccountNumber(), account3);

        LOGGER.info("Sample customers and accounts loaded");
    }

    /**
     * Clears all data from the mock database
     */
    public static void clearAllData() {
        users.clear();
        customers.clear();
        accounts.clear();
        transactions.clear();
        accountsByNumber.clear();
        transactionApprovals.clear();

        // Reset counters
        userIdCounter.set(1);
        customerIdCounter.set(1);
        accountIdCounter.set(1);
        transactionIdCounter.set(1);
        approvalIdCounter.set(1);

        dataInitialized = false;
        LOGGER.info("Mock database cleared - all data removed");
    }

    /**
     * Resets the database to initial state with sample data
     */
    public static void resetToSampleData() {
        clearAllData();
        initializeData(true);
        LOGGER.info("Mock database reset to initial sample data");
    }

    /**
     * Starts with empty database (no sample data)
     */
    public static void startEmpty() {
        clearAllData();
        initializeData(false);
        LOGGER.info("Mock database started empty - no sample data loaded");
    }

    /**
     * Ensures data is initialized (system users only, no sample data by default)
     */
    public static void ensureInitialized() {
        if (!dataInitialized) {
            initializeData(false); // Changed: Only load system users by default
        }
    }

    /**
     * Loads sample data explicitly (customers and accounts)
     */
    public static void loadSampleData() {
        ensureInitialized(); // Ensure system users are loaded first
        if (customers.isEmpty() && accounts.isEmpty()) {
            loadSampleCustomersAndAccounts();
            LOGGER.info("Sample data loaded on user request");
        } else {
            LOGGER.info("Sample data already exists - skipping load");
        }
    }

    public static User findUserByUsername(String username) {
        ensureInitialized();
        return users.values().stream()
            .filter(user -> user.getUsername().equals(username) && user.isActive())
            .findFirst()
            .orElse(null);
    }

    public static User findUserById(Integer userId) {
        ensureInitialized();
        return users.get(userId);
    }

    public static List<User> getAllUsers() {
        ensureInitialized();
        return new ArrayList<>(users.values());
    }

    public static User saveUser(User user) {
        ensureInitialized();
        if (user.getUserId() == null) {
            user.setUserId(userIdCounter.getAndIncrement());
        }
        users.put(user.getUserId(), user);
        return user;
    }

    public static Customer findCustomerById(Integer customerId) {
        ensureInitialized();
        return customers.get(customerId);
    }

    public static List<Customer> getAllCustomers() {
        ensureInitialized();
        return new ArrayList<>(customers.values());
    }

    public static Customer saveCustomer(Customer customer) {
        ensureInitialized();
        if (customer.getCustomerId() == null) {
            customer.setCustomerId(customerIdCounter.getAndIncrement());
        }
        customers.put(customer.getCustomerId(), customer);
        return customer;
    }

    public static AbstractAccount findAccountById(Integer accountId) {
        ensureInitialized();
        return accounts.get(accountId);
    }

    public static AbstractAccount findAccountByNumber(String accountNumber) {
        ensureInitialized();
        return accountsByNumber.get(accountNumber);
    }

    public static List<AbstractAccount> getAllAccounts() {
        ensureInitialized();
        return new ArrayList<>(accounts.values());
    }

    public static List<AbstractAccount> getAccountsByCustomerId(Integer customerId) {
        ensureInitialized();
        return accounts.values().stream()
            .filter(account -> account.getCustomerId().equals(customerId))
            .toList();
    }

    public static AbstractAccount saveAccount(AbstractAccount account) {
        ensureInitialized();
        if (account.getAccountId() == null) {
            account.setAccountId(accountIdCounter.getAndIncrement());
        }
        accounts.put(account.getAccountId(), account);
        accountsByNumber.put(account.getAccountNumber(), account);
        return account;
    }

    public static boolean updateAccountBalance(Integer accountId, BigDecimal newBalance) {
        ensureInitialized();
        AbstractAccount account = accounts.get(accountId);
        if (account != null) {
            account.setBalance(newBalance);
            account.setUpdatedDate(LocalDateTime.now());
            return true;
        }
        return false;
    }

    public static Transaction saveTransaction(Transaction transaction) {
        ensureInitialized();
        if (transaction.getTransactionId() == null) {
            transaction.setTransactionId(transactionIdCounter.getAndIncrement());
        }
        transaction.setCreatedDate(LocalDateTime.now());
        transactions.put(transaction.getTransactionId(), transaction);
        return transaction;
    }

    public static List<Transaction> getTransactionsByAccountId(Integer accountId) {
        return transactions.values().stream()
            .filter(transaction -> transaction.getAccountId().equals(accountId))
            .sorted((t1, t2) -> t2.getCreatedDate().compareTo(t1.getCreatedDate()))
            .toList();
    }

    public static List<Transaction> getAllTransactions() {
        return transactions.values().stream()
            .sorted((t1, t2) -> t2.getCreatedDate().compareTo(t1.getCreatedDate()))
            .toList();
    }

    public static Transaction findTransactionByReference(String referenceNumber) {
        return transactions.values().stream()
            .filter(transaction -> transaction.getReferenceNumber().equals(referenceNumber))
            .findFirst()
            .orElse(null);
    }

    public static BigDecimal getDailyWithdrawalTotal(Integer accountId, LocalDate date) {
        return transactions.values().stream()
            .filter(transaction -> transaction.getAccountId().equals(accountId))
            .filter(transaction -> transaction.getCreatedDate().toLocalDate().equals(date))
            .filter(transaction -> "Withdrawal".equals(transaction.getTransactionType()))
            .map(transaction -> transaction.getAmount().abs())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static Integer getTransactionTypeId(String typeName) {
        switch (typeName) {
            case "Deposit": return 1;
            case "Withdrawal": return 2;
            case "Transfer": return 3;
            case "Fee": return 4;
            case "Interest": return 5;
            case "Reversal": return 6;
            default: return null;
        }
    }

    public static boolean deleteUserPermanently(Integer userId) {
        try {
            User user = users.remove(userId);
            if (user != null) {
                LOGGER.warning("User permanently deleted from database: " + user.getUsername() + " (ID: " + userId + ")");
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to permanently delete user: " + userId, e);
            return false;
        }
    }

    public static boolean deleteAccountPermanently(Integer accountId) {
        try {
            AbstractAccount account = accounts.remove(accountId);
            if (account != null) {
                accountsByNumber.remove(account.getAccountNumber());

                List<Transaction> accountTransactions = getTransactionsByAccountId(accountId);
                for (Transaction transaction : accountTransactions) {
                    transactions.remove(transaction.getTransactionId());
                }

                LOGGER.warning("Account and " + accountTransactions.size() + " related transactions permanently deleted: " +
                             account.getAccountNumber() + " (ID: " + accountId + ")");
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to permanently delete account: " + accountId, e);
            return false;
        }
    }

    public static boolean deleteTransactionPermanently(Integer transactionId) {
        try {
            Transaction transaction = transactions.remove(transactionId);
            if (transaction != null) {
                LOGGER.warning("Transaction permanently deleted from database: " +
                             transaction.getReferenceNumber() + " (ID: " + transactionId + ")");
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to permanently delete transaction: " + transactionId, e);
            return false;
        }
    }

    public static Transaction findTransactionById(Integer transactionId) {
        ensureInitialized();
        return transactions.get(transactionId);
    }

    /**
     * Gets all available customer IDs for account creation
     */
    public static List<Integer> getAvailableCustomerIds() {
        ensureInitialized();
        return new ArrayList<>(customers.keySet());
    }

    /**
     * Gets customer information for display
     */
    public static String getCustomerDisplayInfo(Integer customerId) {
        ensureInitialized();
        Customer customer = customers.get(customerId);
        if (customer != null) {
            return String.format("ID: %d - %s", customerId, customer.getFullName());
        }
        return "Customer not found";
    }

    /**
     * Checks if a customer exists
     */
    public static boolean customerExists(Integer customerId) {
        ensureInitialized();
        return customers.containsKey(customerId);
    }

    /**
     * Gets database statistics for debugging
     */
    public static String getDatabaseStats() {
        ensureInitialized();
        return String.format("Users: %d, Customers: %d, Accounts: %d, Transactions: %d, Approvals: %d",
                           users.size(), customers.size(), accounts.size(), transactions.size(), transactionApprovals.size());
    }

    // Transaction Approval Methods

    public static TransactionApproval saveTransactionApproval(TransactionApproval approval) {
        ensureInitialized();
        if (approval.getApprovalId() == null) {
            approval.setApprovalId(approvalIdCounter.getAndIncrement());
        }
        transactionApprovals.put(approval.getApprovalId(), approval);
        return approval;
    }

    public static TransactionApproval findTransactionApprovalById(Integer approvalId) {
        ensureInitialized();
        return transactionApprovals.get(approvalId);
    }

    public static List<TransactionApproval> getAllTransactionApprovals() {
        ensureInitialized();
        return new ArrayList<>(transactionApprovals.values());
    }

    public static List<TransactionApproval> getPendingTransactionApprovals() {
        ensureInitialized();
        return transactionApprovals.values().stream()
            .filter(TransactionApproval::isPending)
            .collect(Collectors.toList());
    }

    public static List<TransactionApproval> getTransactionApprovalsByUser(Integer userId) {
        ensureInitialized();
        return transactionApprovals.values().stream()
            .filter(approval -> approval.getRequestedByUserId().equals(userId))
            .collect(Collectors.toList());
    }

    public static boolean deleteTransactionApprovalPermanently(Integer approvalId) {
        try {
            TransactionApproval approval = transactionApprovals.remove(approvalId);
            if (approval != null) {
                LOGGER.warning("Transaction approval permanently deleted: ID " + approvalId);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to permanently delete transaction approval: " + approvalId, e);
            return false;
        }
    }

    // Additional methods for backup functionality
    public static List<TransactionApproval> getAllApprovals() {
        ensureInitialized();
        return new ArrayList<>(transactionApprovals.values());
    }

    public static TransactionApproval saveApproval(TransactionApproval approval) {
        ensureInitialized();
        if (approval.getApprovalId() == null) {
            approval.setApprovalId(approvalIdCounter.getAndIncrement());
        }
        transactionApprovals.put(approval.getApprovalId(), approval);
        return approval;
    }

    /**
     * Refresh data for reporting - ensures all collections are up to date
     */
    public static void refreshData() {
        // Force re-initialization to ensure data consistency
        // This method can be called to refresh cached data for reports
        LOGGER.info("Refreshing database data for reports");

        // Validate data integrity
        validateDataIntegrity();
    }

    /**
     * Validate data integrity across all collections
     */
    private static void validateDataIntegrity() {
        try {
            // Validate account-customer relationships
            for (AbstractAccount account : accounts.values()) {
                if (account.getCustomerId() != null && !customers.containsKey(account.getCustomerId())) {
                    LOGGER.warning("Account " + account.getAccountNumber() + " references non-existent customer " + account.getCustomerId());
                }
            }

            // Validate transaction-account relationships
            for (Transaction transaction : transactions.values()) {
                if (transaction.getAccountId() != null && !accounts.containsKey(transaction.getAccountId())) {
                    LOGGER.warning("Transaction " + transaction.getTransactionId() + " references non-existent account " + transaction.getAccountId());
                }
            }

            LOGGER.info("Data integrity validation completed");

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during data integrity validation", e);
        }
    }
}
