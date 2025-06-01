package com.bankingsystem.utils;

import com.bankingsystem.config.AppConfig;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Logger;
import java.util.logging.Level;

public class EncryptionUtil {
    private static final Logger LOGGER = Logger.getLogger(EncryptionUtil.class.getName());
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] salt = "BankingSalt123".getBytes();
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to hash password", e);
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            String newHash = hashPassword(password);
            return newHash.equals(hashedPassword);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to verify password", e);
            return false;
        }
    }

    public static String encrypt(String plainText) {
        try {
            return Base64.getEncoder().encodeToString(plainText.getBytes());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to encrypt data", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public static String decrypt(String encryptedText) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            return new String(decodedBytes);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to decrypt data", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    public static String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getEncoder().encodeToString(tokenBytes);
    }

    public static String generateAccountNumber() {
        StringBuilder accountNumber = new StringBuilder();
        for (int i = 0; i < AppConfig.ACCOUNT_NUMBER_LENGTH; i++) {
            accountNumber.append(SECURE_RANDOM.nextInt(10));
        }
        return accountNumber.toString();
    }

    public static String generateReferenceNumber() {
        return "TXN" + System.currentTimeMillis() + String.format("%04d", SECURE_RANDOM.nextInt(10000));
    }

    public static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }

        String lastFour = accountNumber.substring(accountNumber.length() - 4);
        return "******" + lastFour;
    }

    public static String maskSSN(String ssn) {
        if (ssn == null || ssn.length() < 4) {
            return ssn;
        }

        return "***-**-" + ssn.substring(ssn.length() - 4);
    }
}
