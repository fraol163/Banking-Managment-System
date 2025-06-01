package com.bankingsystem.utils;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

public class SettingsManager {
    private static final Logger LOGGER = Logger.getLogger(SettingsManager.class.getName());
    private static final String SETTINGS_FILE = "banking_system_settings.properties";
    
    private static SettingsManager instance;
    private Properties properties;
    
    // Default values
    private static final String DEFAULT_THEME = "Light";
    private static final String DEFAULT_LOOK_AND_FEEL = "Metal";
    private static final int DEFAULT_SESSION_TIMEOUT = 30;
    private static final boolean DEFAULT_AUTO_SAVE = true;
    private static final boolean DEFAULT_CONFIRM_DELETE = true;
    private static final boolean DEFAULT_SHOW_BALANCE = true;
    private static final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";
    private static final String DEFAULT_CURRENCY_FORMAT = "$#,##0.00";
    private static final int DEFAULT_MAX_TRANSACTION_HISTORY = 1000;
    private static final boolean DEFAULT_SOUND_ENABLED = true;
    
    private SettingsManager() {
        properties = new Properties();
        loadSettings();
    }
    
    public static synchronized SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }
    
    private void loadSettings() {
        try {
            File settingsFile = new File(SETTINGS_FILE);
            if (settingsFile.exists()) {
                try (FileInputStream fis = new FileInputStream(settingsFile)) {
                    properties.load(fis);
                    LOGGER.info("Settings loaded from " + SETTINGS_FILE);
                }
            } else {
                LOGGER.info("Settings file not found, using defaults");
                setDefaults();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load settings, using defaults", e);
            setDefaults();
        }
    }
    
    public void saveSettings() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(fos, "Banking System Settings");
            LOGGER.info("Settings saved to " + SETTINGS_FILE);
        }
    }
    
    private void setDefaults() {
        properties.setProperty("theme", DEFAULT_THEME);
        properties.setProperty("lookAndFeel", DEFAULT_LOOK_AND_FEEL);
        properties.setProperty("sessionTimeout", String.valueOf(DEFAULT_SESSION_TIMEOUT));
        properties.setProperty("autoSave", String.valueOf(DEFAULT_AUTO_SAVE));
        properties.setProperty("confirmDelete", String.valueOf(DEFAULT_CONFIRM_DELETE));
        properties.setProperty("showBalanceInList", String.valueOf(DEFAULT_SHOW_BALANCE));
        properties.setProperty("dateFormat", DEFAULT_DATE_FORMAT);
        properties.setProperty("currencyFormat", DEFAULT_CURRENCY_FORMAT);
        properties.setProperty("maxTransactionHistory", String.valueOf(DEFAULT_MAX_TRANSACTION_HISTORY));
        properties.setProperty("soundEnabled", String.valueOf(DEFAULT_SOUND_ENABLED));
    }
    
    public void resetToDefaults() {
        setDefaults();
        try {
            saveSettings();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save default settings", e);
        }
    }
    
    // Getters
    public String getTheme() {
        return properties.getProperty("theme", DEFAULT_THEME);
    }
    
    public String getLookAndFeel() {
        return properties.getProperty("lookAndFeel", DEFAULT_LOOK_AND_FEEL);
    }
    
    public int getSessionTimeout() {
        try {
            return Integer.parseInt(properties.getProperty("sessionTimeout", String.valueOf(DEFAULT_SESSION_TIMEOUT)));
        } catch (NumberFormatException e) {
            return DEFAULT_SESSION_TIMEOUT;
        }
    }
    
    public boolean isAutoSaveEnabled() {
        return Boolean.parseBoolean(properties.getProperty("autoSave", String.valueOf(DEFAULT_AUTO_SAVE)));
    }
    
    public boolean isConfirmDeleteEnabled() {
        return Boolean.parseBoolean(properties.getProperty("confirmDelete", String.valueOf(DEFAULT_CONFIRM_DELETE)));
    }
    
    public boolean isShowBalanceInList() {
        return Boolean.parseBoolean(properties.getProperty("showBalanceInList", String.valueOf(DEFAULT_SHOW_BALANCE)));
    }
    
    public String getDateFormat() {
        return properties.getProperty("dateFormat", DEFAULT_DATE_FORMAT);
    }
    
    public String getCurrencyFormat() {
        return properties.getProperty("currencyFormat", DEFAULT_CURRENCY_FORMAT);
    }
    
    public int getMaxTransactionHistory() {
        try {
            return Integer.parseInt(properties.getProperty("maxTransactionHistory", String.valueOf(DEFAULT_MAX_TRANSACTION_HISTORY)));
        } catch (NumberFormatException e) {
            return DEFAULT_MAX_TRANSACTION_HISTORY;
        }
    }
    
    public boolean isSoundEnabled() {
        return Boolean.parseBoolean(properties.getProperty("soundEnabled", String.valueOf(DEFAULT_SOUND_ENABLED)));
    }
    
    // Setters
    public void setTheme(String theme) {
        properties.setProperty("theme", theme);
    }
    
    public void setLookAndFeel(String lookAndFeel) {
        properties.setProperty("lookAndFeel", lookAndFeel);
    }
    
    public void setSessionTimeout(int timeout) {
        properties.setProperty("sessionTimeout", String.valueOf(timeout));
    }
    
    public void setAutoSaveEnabled(boolean enabled) {
        properties.setProperty("autoSave", String.valueOf(enabled));
    }
    
    public void setConfirmDeleteEnabled(boolean enabled) {
        properties.setProperty("confirmDelete", String.valueOf(enabled));
    }
    
    public void setShowBalanceInList(boolean show) {
        properties.setProperty("showBalanceInList", String.valueOf(show));
    }
    
    public void setDateFormat(String format) {
        properties.setProperty("dateFormat", format);
    }
    
    public void setCurrencyFormat(String format) {
        properties.setProperty("currencyFormat", format);
    }
    
    public void setMaxTransactionHistory(int max) {
        properties.setProperty("maxTransactionHistory", String.valueOf(max));
    }
    
    public void setSoundEnabled(boolean enabled) {
        properties.setProperty("soundEnabled", String.valueOf(enabled));
    }
    
    // Utility methods
    public boolean isDarkTheme() {
        return "Dark".equals(getTheme());
    }
    
    public void applyLookAndFeel() {
        try {
            String laf = getLookAndFeel();
            switch (laf) {
                case "Metal":
                    javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                    break;
                case "Nimbus":
                    javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                    break;
                case "System":
                    // Use system default
                    javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                    break;
                case "Cross Platform":
                    // Use cross platform default
                    javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                    break;
                default:
                    javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            }
            LOGGER.info("Look and Feel set to: " + laf);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to set Look and Feel", e);
        }
    }
}
