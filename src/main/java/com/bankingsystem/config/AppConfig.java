package com.bankingsystem.config;

import java.math.BigDecimal;

public class AppConfig {
    public static final String APPLICATION_NAME = "Banking Management System";
    public static final String VERSION = "1.0.0";

    public static final BigDecimal MAX_DAILY_WITHDRAWAL_LIMIT = new BigDecimal("2000.00");
    public static final BigDecimal DEFAULT_SAVINGS_INTEREST_RATE = new BigDecimal("0.025");
    public static final BigDecimal DEFAULT_CHECKING_OVERDRAFT_LIMIT = new BigDecimal("500.00");
    public static final BigDecimal DEFAULT_OVERDRAFT_FEE = new BigDecimal("25.00");
    public static final BigDecimal EXTERNAL_TRANSFER_FEE = new BigDecimal("5.00");
    public static final BigDecimal LARGE_WITHDRAWAL_THRESHOLD = new BigDecimal("1000.00");

    public static final int SESSION_TIMEOUT_MINUTES = 30;
    public static final int PASSWORD_SALT_ROUNDS = 12;
    public static final int ACCOUNT_NUMBER_LENGTH = 10;

    // Enhanced Security Configuration
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int ACCOUNT_LOCKOUT_DURATION_MINUTES = 30;
    public static final int LOGIN_ATTEMPT_RESET_MINUTES = 60;
    public static final int CAPTCHA_REQUIRED_AFTER_ATTEMPTS = 3;
    public static final int MAX_PROGRESSIVE_DELAY_SECONDS = 30;
    public static final int PASSWORD_EXPIRY_DAYS = 90;
    public static final int PASSWORD_EXPIRY_WARNING_DAYS = 14;
    public static final int PASSWORD_HISTORY_COUNT = 5;
    public static final int SESSION_INACTIVITY_TIMEOUT_MINUTES = 30;
    public static final int SESSION_WARNING_MINUTES = 5;
    public static final int MAX_CONCURRENT_SESSIONS_ADMIN = 3;
    public static final int MAX_CONCURRENT_SESSIONS_MANAGER = 2;
    public static final int MAX_CONCURRENT_SESSIONS_TELLER = 1;
    public static final int REMEMBER_ME_DURATION_DAYS = 30;

    public static final String SAVINGS_ACCOUNT_TYPE = "Savings";
    public static final String CHECKING_ACCOUNT_TYPE = "Checking";
    public static final String BUSINESS_ACCOUNT_TYPE = "Business";

    public static final String ROLE_CUSTOMER = "Customer";
    public static final String ROLE_TELLER = "Teller";
    public static final String ROLE_MANAGER = "Manager";
    public static final String ROLE_ADMIN = "Admin";

    public static final String ACCOUNT_STATUS_ACTIVE = "Active";
    public static final String ACCOUNT_STATUS_SUSPENDED = "Suspended";
    public static final String ACCOUNT_STATUS_CLOSED = "Closed";

    public static final String TRANSACTION_TYPE_DEPOSIT = "Deposit";
    public static final String TRANSACTION_TYPE_WITHDRAWAL = "Withdrawal";
    public static final String TRANSACTION_TYPE_TRANSFER = "Transfer";
    public static final String TRANSACTION_TYPE_FEE = "Fee";
    public static final String TRANSACTION_TYPE_INTEREST = "Interest";
    public static final String TRANSACTION_TYPE_REVERSAL = "Reversal";

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String CURRENCY_FORMAT = "$#,##0.00";

    public static final String BACKUP_DIRECTORY = "backups";
    public static final String EXPORT_DIRECTORY = "exports";
    public static final String IMPORT_DIRECTORY = "imports";
    public static final String REPORTS_DIRECTORY = "reports";

    public static final String CSV_DELIMITER = ",";
    public static final String PDF_FONT = "Helvetica";
    public static final int PDF_FONT_SIZE = 12;

    public static final String ENCRYPTION_ALGORITHM = "AES";
    public static final String ENCRYPTION_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    public static final int ENCRYPTION_KEY_LENGTH = 256;

    public static final String LOG_FILE_NAME = "banking_system.log";
    public static final String ERROR_LOG_FILE_NAME = "banking_system_errors.log";

    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 50;
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 50;

    public static final String PHONE_REGEX = "^\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$";
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
    public static final String NAME_REGEX = "^[A-Za-z\\s\\-']{2,50}$";
    public static final String ACCOUNT_NUMBER_REGEX = "^[0-9]{10}$";
    public static final String SSN_REGEX = "^[0-9]{3}-[0-9]{2}-[0-9]{4}$";

    public static final String PERMISSION_CREATE_USER = "CREATE_USER";
    public static final String PERMISSION_UPDATE_USER = "UPDATE_USER";
    public static final String PERMISSION_DELETE_USER = "DELETE_USER";
    public static final String PERMISSION_VIEW_ALL_USERS = "VIEW_ALL_USERS";
    public static final String PERMISSION_CREATE_ACCOUNT = "CREATE_ACCOUNT";
    public static final String PERMISSION_UPDATE_ACCOUNT = "UPDATE_ACCOUNT";
    public static final String PERMISSION_DELETE_ACCOUNT = "DELETE_ACCOUNT";
    public static final String PERMISSION_VIEW_ALL_ACCOUNTS = "VIEW_ALL_ACCOUNTS";
    public static final String PERMISSION_VIEW_ACCOUNT = "VIEW_ACCOUNT";
    public static final String PERMISSION_PROCESS_TRANSACTION = "PROCESS_TRANSACTION";
    public static final String PERMISSION_REVERSE_TRANSACTION = "REVERSE_TRANSACTION";
    public static final String PERMISSION_VIEW_ALL_TRANSACTIONS = "VIEW_ALL_TRANSACTIONS";
    public static final String PERMISSION_VIEW_TRANSACTION = "VIEW_TRANSACTION";
    public static final String PERMISSION_GENERATE_REPORTS = "GENERATE_REPORTS";
    public static final String PERMISSION_GENERATE_CUSTOMER_REPORTS = "GENERATE_CUSTOMER_REPORTS";
    public static final String PERMISSION_BACKUP_DATABASE = "BACKUP_DATABASE";
    public static final String PERMISSION_RESTORE_DATABASE = "RESTORE_DATABASE";
    public static final String PERMISSION_MANAGE_SYSTEM_CONFIG = "MANAGE_SYSTEM_CONFIG";
    public static final String PERMISSION_VIEW_AUDIT_LOG = "VIEW_AUDIT_LOG";
    public static final String PERMISSION_APPROVE_TRANSACTIONS = "APPROVE_TRANSACTIONS";
    public static final String PERMISSION_PROCESS_LARGE_TRANSACTIONS = "PROCESS_LARGE_TRANSACTIONS";
    public static final String PERMISSION_CREATE_ADMIN_USER = "CREATE_ADMIN_USER";
    public static final String PERMISSION_CREATE_MANAGER_USER = "CREATE_MANAGER_USER";
    public static final String PERMISSION_CREATE_TELLER_USER = "CREATE_TELLER_USER";

    public static final BigDecimal TELLER_TRANSACTION_LIMIT = new BigDecimal("2000.00");
    public static final BigDecimal TELLER_APPROVAL_THRESHOLD = new BigDecimal("1000.00");
    public static final BigDecimal MANAGER_TRANSACTION_LIMIT = new BigDecimal("10000.00");
    public static final BigDecimal MANAGER_APPROVAL_THRESHOLD = new BigDecimal("5000.00");
}
