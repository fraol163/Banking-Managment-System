package com.bankingsystem.config;

public class DatabaseConfig {
    public static final String DATABASE_URL = "jdbc:sqlite:banking_system.db";
    public static final String DRIVER_CLASS = "org.sqlite.JDBC";
    public static final int MAX_POOL_SIZE = 10;
    public static final int MIN_IDLE = 2;
    public static final long CONNECTION_TIMEOUT = 30000;
    public static final long IDLE_TIMEOUT = 600000;
    public static final long MAX_LIFETIME = 1800000;

    public static final String CREATE_CUSTOMERS_TABLE = """
        CREATE TABLE IF NOT EXISTS customers (
            customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
            first_name TEXT NOT NULL,
            last_name TEXT NOT NULL,
            email TEXT UNIQUE NOT NULL,
            phone TEXT NOT NULL,
            address TEXT NOT NULL,
            date_of_birth DATE NOT NULL,
            ssn TEXT UNIQUE NOT NULL,
            created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """;

    public static final String CREATE_ACCOUNT_TYPES_TABLE = """
        CREATE TABLE IF NOT EXISTS account_types (
            type_id INTEGER PRIMARY KEY AUTOINCREMENT,
            type_name TEXT UNIQUE NOT NULL,
            minimum_balance DECIMAL(15,2) NOT NULL,
            interest_rate DECIMAL(5,4) NOT NULL,
            overdraft_limit DECIMAL(15,2) DEFAULT 0,
            overdraft_fee DECIMAL(10,2) DEFAULT 0,
            daily_withdrawal_limit DECIMAL(15,2) NOT NULL
        )
    """;

    public static final String CREATE_ACCOUNTS_TABLE = """
        CREATE TABLE IF NOT EXISTS accounts (
            account_id INTEGER PRIMARY KEY AUTOINCREMENT,
            account_number TEXT UNIQUE NOT NULL,
            customer_id INTEGER NOT NULL,
            account_type_id INTEGER NOT NULL,
            balance DECIMAL(15,2) NOT NULL DEFAULT 0,
            status TEXT NOT NULL DEFAULT 'Active',
            created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
            FOREIGN KEY (account_type_id) REFERENCES account_types(type_id)
        )
    """;

    public static final String CREATE_TRANSACTION_TYPES_TABLE = """
        CREATE TABLE IF NOT EXISTS transaction_types (
            type_id INTEGER PRIMARY KEY AUTOINCREMENT,
            type_name TEXT UNIQUE NOT NULL,
            description TEXT
        )
    """;

    public static final String CREATE_TRANSACTIONS_TABLE = """
        CREATE TABLE IF NOT EXISTS transactions (
            transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
            account_id INTEGER NOT NULL,
            transaction_type_id INTEGER NOT NULL,
            amount DECIMAL(15,2) NOT NULL,
            balance_before DECIMAL(15,2) NOT NULL,
            balance_after DECIMAL(15,2) NOT NULL,
            description TEXT,
            reference_number TEXT UNIQUE NOT NULL,
            created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            created_by INTEGER,
            FOREIGN KEY (account_id) REFERENCES accounts(account_id),
            FOREIGN KEY (transaction_type_id) REFERENCES transaction_types(type_id),
            FOREIGN KEY (created_by) REFERENCES users(user_id)
        )
    """;

    public static final String CREATE_USERS_TABLE = """
        CREATE TABLE IF NOT EXISTS users (
            user_id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE NOT NULL,
            password_hash TEXT NOT NULL,
            role TEXT NOT NULL,
            first_name TEXT NOT NULL,
            last_name TEXT NOT NULL,
            email TEXT UNIQUE NOT NULL,
            is_active BOOLEAN DEFAULT 1,
            last_login TIMESTAMP,
            created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """;

    public static final String CREATE_AUDIT_LOG_TABLE = """
        CREATE TABLE IF NOT EXISTS audit_log (
            log_id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER,
            operation_type TEXT NOT NULL,
            table_name TEXT NOT NULL,
            record_id INTEGER,
            old_values TEXT,
            new_values TEXT,
            timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES users(user_id)
        )
    """;
}
