package com.bankingsystem.exceptions;

public class DatabaseConnectionException extends Exception {
    private final String connectionUrl;
    
    public DatabaseConnectionException(String message, String connectionUrl) {
        super(message);
        this.connectionUrl = connectionUrl;
    }
    
    public DatabaseConnectionException(String message, String connectionUrl, Throwable cause) {
        super(message, cause);
        this.connectionUrl = connectionUrl;
    }
    
    public String getConnectionUrl() {
        return connectionUrl;
    }
    
    @Override
    public String getMessage() {
        return String.format("%s Connection URL: %s", super.getMessage(), connectionUrl);
    }
}
