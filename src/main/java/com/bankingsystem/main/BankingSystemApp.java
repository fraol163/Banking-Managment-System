package com.bankingsystem.main;

import com.bankingsystem.config.DatabaseConfig;
import com.bankingsystem.gui.LoginFrame;
import com.bankingsystem.utils.DatabaseUtil;
import com.bankingsystem.utils.MockDatabaseUtil;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class BankingSystemApp {
    private static final Logger LOGGER = Logger.getLogger(BankingSystemApp.class.getName());

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to set look and feel", e);
        }

        // Check command line arguments for data initialization options
        boolean startEmpty = false;
        boolean resetData = false;
        boolean loadSampleData = false;

        for (String arg : args) {
            switch (arg.toLowerCase()) {
                case "--empty":
                case "-e":
                    startEmpty = true;
                    break;
                case "--reset":
                case "-r":
                    resetData = true;
                    break;
                case "--sample":
                case "-s":
                    loadSampleData = true;
                    break;
                case "--help":
                case "-h":
                    printUsage();
                    return;
            }
        }

        final boolean finalStartEmpty = startEmpty;
        final boolean finalResetData = resetData;
        final boolean finalLoadSampleData = loadSampleData;

        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseUtil.initializeDatabase();

                // Handle data initialization based on command line arguments
                if (finalResetData) {
                    MockDatabaseUtil.resetToSampleData();
                    LOGGER.info("Database reset to sample data");
                } else if (finalStartEmpty) {
                    MockDatabaseUtil.startEmpty();
                    LOGGER.info("Database started empty - no sample data loaded");
                } else if (finalLoadSampleData) {
                    MockDatabaseUtil.ensureInitialized();
                    MockDatabaseUtil.loadSampleData();
                    LOGGER.info("Database initialized with system users and sample data");
                } else {
                    MockDatabaseUtil.ensureInitialized();
                    LOGGER.info("Database initialized with system users only (default)");
                }

                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);

                LOGGER.info("Banking System Application started successfully");
                LOGGER.info("Database stats: " + MockDatabaseUtil.getDatabaseStats());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to start Banking System Application", e);
                System.exit(1);
            }
        });
    }

    private static void printUsage() {
        System.out.println("Banking Management System");
        System.out.println("Usage: java BankingSystemApp [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --empty, -e    Start with empty database (no customers or accounts)");
        System.out.println("  --sample, -s   Load sample customers and accounts");
        System.out.println("  --reset, -r    Reset database to initial sample data");
        System.out.println("  --help, -h     Show this help message");
        System.out.println();
        System.out.println("Default: Start with system users only (admin, manager, teller)");
        System.out.println("         No sample customers or accounts are loaded by default");
    }
}
