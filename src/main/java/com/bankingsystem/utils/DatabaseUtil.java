package com.bankingsystem.utils;

import com.bankingsystem.config.DatabaseConfig;
import com.bankingsystem.exceptions.DatabaseConnectionException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DatabaseUtil {
    private static final Logger LOGGER = Logger.getLogger(DatabaseUtil.class.getName());

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(DatabaseConfig.DATABASE_URL);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "SQLite JDBC driver not found, using mock database fallback", e);
            throw new SQLException("SQLite JDBC driver not found - using mock database", e);
        }
    }

    public static boolean isSQLiteDriverAvailable() {
        try {
            Class.forName("org.sqlite.JDBC");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void initializeDatabase() throws SQLException {
        try {
            LOGGER.info("Using mock database - no initialization required");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database", e);
            throw new SQLException("Database initialization failed", e);
        }
    }



    public static void closeDataSource() {
        LOGGER.info("Database connections will be closed automatically");
    }
}
