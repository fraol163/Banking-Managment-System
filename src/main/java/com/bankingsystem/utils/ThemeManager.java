package com.bankingsystem.utils;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ThemeManager {
    private static final Logger LOGGER = Logger.getLogger(ThemeManager.class.getName());
    
    // Light theme colors
    public static final Color LIGHT_BACKGROUND = Color.WHITE;
    public static final Color LIGHT_FOREGROUND = Color.BLACK;
    public static final Color LIGHT_PANEL_BACKGROUND = new Color(245, 245, 245);
    public static final Color LIGHT_BUTTON_BACKGROUND = new Color(240, 240, 240);
    public static final Color LIGHT_BORDER = Color.GRAY;
    public static final Color LIGHT_SELECTION = new Color(184, 207, 229);
    
    // Dark theme colors
    public static final Color DARK_BACKGROUND = new Color(43, 43, 43);
    public static final Color DARK_FOREGROUND = new Color(220, 220, 220);
    public static final Color DARK_PANEL_BACKGROUND = new Color(60, 60, 60);
    public static final Color DARK_BUTTON_BACKGROUND = new Color(70, 70, 70);
    public static final Color DARK_BORDER = new Color(100, 100, 100);
    public static final Color DARK_SELECTION = new Color(75, 110, 175);
    public static final Color DARK_TEXT_FIELD = new Color(55, 55, 55);
    public static final Color DARK_TABLE_BACKGROUND = new Color(50, 50, 50);
    public static final Color DARK_TABLE_ALTERNATE = new Color(45, 45, 45);
    
    // Accent colors (same for both themes)
    public static final Color SUCCESS_COLOR = new Color(34, 139, 34);
    public static final Color ERROR_COLOR = new Color(220, 20, 60);
    public static final Color WARNING_COLOR = new Color(255, 140, 0);
    public static final Color INFO_COLOR = new Color(70, 130, 180);
    
    public static void applyTheme(String theme) {
        try {
            if ("Dark".equals(theme)) {
                applyDarkTheme();
            } else {
                applyLightTheme();
            }
            
            // Update all existing windows
            updateAllWindows();
            
            LOGGER.info("Theme applied: " + theme);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to apply theme: " + theme, e);
        }
    }
    
    private static void applyDarkTheme() {
        // Set UIManager defaults for dark theme
        UIManager.put("Panel.background", new ColorUIResource(DARK_PANEL_BACKGROUND));
        UIManager.put("Panel.foreground", new ColorUIResource(DARK_FOREGROUND));
        
        UIManager.put("Button.background", new ColorUIResource(DARK_BUTTON_BACKGROUND));
        UIManager.put("Button.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("Button.border", BorderFactory.createLineBorder(DARK_BORDER));
        
        UIManager.put("TextField.background", new ColorUIResource(DARK_TEXT_FIELD));
        UIManager.put("TextField.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("TextField.border", BorderFactory.createLineBorder(DARK_BORDER));
        
        UIManager.put("TextArea.background", new ColorUIResource(DARK_TEXT_FIELD));
        UIManager.put("TextArea.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("TextArea.border", BorderFactory.createLineBorder(DARK_BORDER));
        
        UIManager.put("ComboBox.background", new ColorUIResource(DARK_BUTTON_BACKGROUND));
        UIManager.put("ComboBox.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("ComboBox.border", BorderFactory.createLineBorder(DARK_BORDER));
        
        UIManager.put("Table.background", new ColorUIResource(DARK_TABLE_BACKGROUND));
        UIManager.put("Table.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("Table.alternateRowColor", new ColorUIResource(DARK_TABLE_ALTERNATE));
        UIManager.put("Table.selectionBackground", new ColorUIResource(DARK_SELECTION));
        UIManager.put("Table.selectionForeground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("Table.gridColor", new ColorUIResource(DARK_BORDER));
        
        UIManager.put("TableHeader.background", new ColorUIResource(DARK_BUTTON_BACKGROUND));
        UIManager.put("TableHeader.foreground", new ColorUIResource(DARK_FOREGROUND));
        
        UIManager.put("ScrollPane.background", new ColorUIResource(DARK_PANEL_BACKGROUND));
        UIManager.put("ScrollBar.background", new ColorUIResource(DARK_BUTTON_BACKGROUND));
        
        UIManager.put("Label.foreground", new ColorUIResource(DARK_FOREGROUND));
        
        UIManager.put("TabbedPane.background", new ColorUIResource(DARK_PANEL_BACKGROUND));
        UIManager.put("TabbedPane.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("TabbedPane.selected", new ColorUIResource(DARK_SELECTION));
        
        UIManager.put("MenuBar.background", new ColorUIResource(DARK_PANEL_BACKGROUND));
        UIManager.put("MenuBar.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("Menu.background", new ColorUIResource(DARK_PANEL_BACKGROUND));
        UIManager.put("Menu.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("MenuItem.background", new ColorUIResource(DARK_PANEL_BACKGROUND));
        UIManager.put("MenuItem.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("MenuItem.selectionBackground", new ColorUIResource(DARK_SELECTION));
        
        UIManager.put("OptionPane.background", new ColorUIResource(DARK_PANEL_BACKGROUND));
        UIManager.put("OptionPane.foreground", new ColorUIResource(DARK_FOREGROUND));
        UIManager.put("OptionPane.messageForeground", new ColorUIResource(DARK_FOREGROUND));
    }
    
    private static void applyLightTheme() {
        // Reset to default light theme
        UIManager.put("Panel.background", new ColorUIResource(LIGHT_PANEL_BACKGROUND));
        UIManager.put("Panel.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        
        UIManager.put("Button.background", new ColorUIResource(LIGHT_BUTTON_BACKGROUND));
        UIManager.put("Button.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("Button.border", BorderFactory.createRaisedBevelBorder());

        UIManager.put("TextField.background", new ColorUIResource(LIGHT_BACKGROUND));
        UIManager.put("TextField.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("TextField.border", BorderFactory.createLoweredBevelBorder());

        UIManager.put("TextArea.background", new ColorUIResource(LIGHT_BACKGROUND));
        UIManager.put("TextArea.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("TextArea.border", BorderFactory.createLoweredBevelBorder());
        
        UIManager.put("ComboBox.background", new ColorUIResource(LIGHT_BACKGROUND));
        UIManager.put("ComboBox.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        
        UIManager.put("Table.background", new ColorUIResource(LIGHT_BACKGROUND));
        UIManager.put("Table.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("Table.alternateRowColor", new ColorUIResource(LIGHT_PANEL_BACKGROUND));
        UIManager.put("Table.selectionBackground", new ColorUIResource(LIGHT_SELECTION));
        UIManager.put("Table.selectionForeground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("Table.gridColor", new ColorUIResource(LIGHT_BORDER));
        
        UIManager.put("TableHeader.background", new ColorUIResource(LIGHT_BUTTON_BACKGROUND));
        UIManager.put("TableHeader.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        
        UIManager.put("ScrollPane.background", new ColorUIResource(LIGHT_BACKGROUND));
        UIManager.put("ScrollBar.background", new ColorUIResource(LIGHT_BUTTON_BACKGROUND));
        
        UIManager.put("Label.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        
        UIManager.put("TabbedPane.background", new ColorUIResource(LIGHT_PANEL_BACKGROUND));
        UIManager.put("TabbedPane.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("TabbedPane.selected", new ColorUIResource(LIGHT_SELECTION));
        
        UIManager.put("MenuBar.background", new ColorUIResource(LIGHT_PANEL_BACKGROUND));
        UIManager.put("MenuBar.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("Menu.background", new ColorUIResource(LIGHT_PANEL_BACKGROUND));
        UIManager.put("Menu.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("MenuItem.background", new ColorUIResource(LIGHT_PANEL_BACKGROUND));
        UIManager.put("MenuItem.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("MenuItem.selectionBackground", new ColorUIResource(LIGHT_SELECTION));
        
        UIManager.put("OptionPane.background", new ColorUIResource(LIGHT_PANEL_BACKGROUND));
        UIManager.put("OptionPane.foreground", new ColorUIResource(LIGHT_FOREGROUND));
        UIManager.put("OptionPane.messageForeground", new ColorUIResource(LIGHT_FOREGROUND));
    }
    
    private static void updateAllWindows() {
        // Update all open windows
        for (Window window : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
            window.repaint();
        }
    }
    
    public static boolean isDarkTheme() {
        SettingsManager settings = SettingsManager.getInstance();
        return settings.isDarkTheme();
    }
    
    public static Color getBackgroundColor() {
        return isDarkTheme() ? DARK_BACKGROUND : LIGHT_BACKGROUND;
    }
    
    public static Color getForegroundColor() {
        return isDarkTheme() ? DARK_FOREGROUND : LIGHT_FOREGROUND;
    }
    
    public static Color getPanelBackgroundColor() {
        return isDarkTheme() ? DARK_PANEL_BACKGROUND : LIGHT_PANEL_BACKGROUND;
    }
    
    public static Color getButtonBackgroundColor() {
        return isDarkTheme() ? DARK_BUTTON_BACKGROUND : LIGHT_BUTTON_BACKGROUND;
    }
    
    public static Color getBorderColor() {
        return isDarkTheme() ? DARK_BORDER : LIGHT_BORDER;
    }
    
    public static Color getSelectionColor() {
        return isDarkTheme() ? DARK_SELECTION : LIGHT_SELECTION;
    }
}
