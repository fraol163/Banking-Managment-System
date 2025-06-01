package com.bankingsystem.utils;

import com.bankingsystem.config.AppConfig;
import com.bankingsystem.models.Transaction;
import com.bankingsystem.models.AbstractAccount;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class FileUtil {
    private static final Logger LOGGER = Logger.getLogger(FileUtil.class.getName());

    public static void createDirectories() {
        try {
            Files.createDirectories(Paths.get(AppConfig.BACKUP_DIRECTORY));
            Files.createDirectories(Paths.get(AppConfig.EXPORT_DIRECTORY));
            Files.createDirectories(Paths.get(AppConfig.IMPORT_DIRECTORY));
            Files.createDirectories(Paths.get(AppConfig.REPORTS_DIRECTORY));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create directories", e);
        }
    }

    public static void exportTransactionsToCSV(List<Transaction> transactions, String filename) throws IOException {
        createDirectories();
        Path filePath = Paths.get(AppConfig.EXPORT_DIRECTORY, filename);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
            writer.println("Transaction ID,Account ID,Type,Amount,Balance Before,Balance After,Description,Reference,Date");

            for (Transaction transaction : transactions) {
                writer.printf("%d,%d,%s,%.2f,%.2f,%.2f,\"%s\",%s,%s%n",
                    transaction.getTransactionId(),
                    transaction.getAccountId(),
                    transaction.getTransactionType(),
                    transaction.getAmount(),
                    transaction.getBalanceBefore(),
                    transaction.getBalanceAfter(),
                    escapeCSV(transaction.getDescription()),
                    transaction.getReferenceNumber(),
                    transaction.getCreatedDate() != null ? transaction.getCreatedDate().toString() : ""
                );
            }
        }

        LOGGER.info("Transactions exported to CSV: " + filePath);
    }

    public static void exportAccountsToCSV(List<AbstractAccount> accounts, String filename) throws IOException {
        createDirectories();
        Path filePath = Paths.get(AppConfig.EXPORT_DIRECTORY, filename);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
            writer.println("Account ID,Account Number,Customer ID,Type,Balance,Status,Created Date");

            for (AbstractAccount account : accounts) {
                writer.printf("%d,%s,%d,%s,%.2f,%s,%s%n",
                    account.getAccountId(),
                    account.getAccountNumber(),
                    account.getCustomerId(),
                    getAccountTypeName(account),
                    account.getBalance(),
                    account.getStatus(),
                    account.getCreatedDate() != null ? account.getCreatedDate().toString() : ""
                );
            }
        }

        LOGGER.info("Accounts exported to CSV: " + filePath);
    }

    public static String backupDatabase() throws IOException {
        createDirectories();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFilename = String.format("banking_system_backup_%s.db", timestamp);
        Path backupPath = Paths.get(AppConfig.BACKUP_DIRECTORY, backupFilename);
        Path sourcePath = Paths.get("banking_system.db");

        if (Files.exists(sourcePath)) {
            Files.copy(sourcePath, backupPath);
            LOGGER.info("Database backed up to: " + backupPath);
            return backupPath.toString();
        } else {
            throw new IOException("Source database file not found");
        }
    }

    public static void generateAccountStatement(AbstractAccount account, List<Transaction> transactions, String filename) throws IOException {
        createDirectories();
        Path filePath = Paths.get(AppConfig.REPORTS_DIRECTORY, filename);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath))) {
            writer.println("BANKING MANAGEMENT SYSTEM");
            writer.println("ACCOUNT STATEMENT");
            writer.println("=====================================");
            writer.println();
            writer.printf("Account Number: %s%n", account.getAccountNumber());
            writer.printf("Account Type: %s%n", getAccountTypeName(account));
            writer.printf("Current Balance: $%.2f%n", account.getBalance());
            writer.printf("Statement Date: %s%n", LocalDateTime.now().format(DateTimeFormatter.ofPattern(AppConfig.DATETIME_FORMAT)));
            writer.println();
            writer.println("TRANSACTION HISTORY");
            writer.println("=====================================");
            writer.printf("%-20s %-12s %-12s %-12s %-30s%n", "Date", "Type", "Amount", "Balance", "Description");
            writer.println("-----------------------------------------------------------------------------------------------------");

            for (Transaction transaction : transactions) {
                writer.printf("%-20s %-12s $%-11.2f $%-11.2f %-30s%n",
                    transaction.getCreatedDate() != null ?
                        transaction.getCreatedDate().format(DateTimeFormatter.ofPattern(AppConfig.DATETIME_FORMAT)) : "",
                    transaction.getTransactionType(),
                    transaction.getAmount(),
                    transaction.getBalanceAfter(),
                    truncateString(transaction.getDescription(), 30)
                );
            }

            writer.println();
            writer.println("End of Statement");
        }

        LOGGER.info("Account statement generated: " + filePath);
    }

    public static void writeErrorLog(String message, Exception exception) {
        try {
            Path logPath = Paths.get(AppConfig.ERROR_LOG_FILE_NAME);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(AppConfig.DATETIME_FORMAT));
            String logEntry = String.format("[%s] ERROR: %s%n", timestamp, message);

            if (exception != null) {
                logEntry += String.format("Exception: %s%n", exception.getMessage());
                logEntry += String.format("Stack trace: %s%n", getStackTrace(exception));
            }

            logEntry += "-----------------------------------" + System.lineSeparator();

            Files.write(logPath, logEntry.getBytes(),
                       java.nio.file.StandardOpenOption.CREATE,
                       java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to write error log", e);
        }
    }

    public static void writeAuditLog(String operation, String details) {
        try {
            Path logPath = Paths.get("audit.log");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(AppConfig.DATETIME_FORMAT));
            String logEntry = String.format("[%s] %s: %s%n", timestamp, operation, details);

            Files.write(logPath, logEntry.getBytes(),
                       java.nio.file.StandardOpenOption.CREATE,
                       java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to write audit log", e);
        }
    }

    private static String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private static String getAccountTypeName(AbstractAccount account) {
        String className = account.getClass().getSimpleName();
        return className.replace("Account", "");
    }

    private static String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    private static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static boolean fileExists(String filename) {
        return Files.exists(Paths.get(filename));
    }

    public static long getFileSize(String filename) {
        try {
            return Files.size(Paths.get(filename));
        } catch (IOException e) {
            return 0;
        }
    }

    public static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    public static String saveReportToFile(String filename, String content) throws IOException {
        try {
            createDirectories();
            Path reportsDir = Paths.get("reports");
            if (!Files.exists(reportsDir)) {
                Files.createDirectories(reportsDir);
            }

            Path filePath = reportsDir.resolve(filename);

            Files.write(filePath, content.getBytes(),
                       java.nio.file.StandardOpenOption.CREATE,
                       java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);

            String absolutePath = filePath.toAbsolutePath().toString();
            LOGGER.info("Report saved to: " + absolutePath);

            return absolutePath;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save report: " + filename, e);
            throw e;
        }
    }
}
