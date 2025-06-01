package com.bankingsystem.dao;

import com.bankingsystem.models.Customer;
import com.bankingsystem.utils.DatabaseUtil;
import com.bankingsystem.utils.MockDatabaseUtil;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CustomerDAO {
    private static final Logger LOGGER = Logger.getLogger(CustomerDAO.class.getName());
    
    public Customer findById(Integer customerId) throws SQLException {
        if (!DatabaseUtil.isSQLiteDriverAvailable()) {
            LOGGER.info("SQLite driver not available, using mock database for customer lookup");
            return MockDatabaseUtil.findCustomerById(customerId);
        }

        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToCustomer(resultSet);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Database connection failed, falling back to mock database for customer ID: " + customerId, e);
            return MockDatabaseUtil.findCustomerById(customerId);
        }

        return null;
    }
    
    public Customer findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM customers WHERE email = ?";
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, email);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToCustomer(resultSet);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find customer by email: " + email, e);
            throw e;
        }
        
        return null;
    }
    
    public Customer findBySSN(String ssn) throws SQLException {
        String sql = "SELECT * FROM customers WHERE ssn = ?";
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, ssn);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToCustomer(resultSet);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find customer by SSN", e);
            throw e;
        }
        
        return null;
    }
    
    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT * FROM customers ORDER BY last_name, first_name";
        List<Customer> customers = new ArrayList<>();
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                customers.add(mapResultSetToCustomer(resultSet));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve all customers", e);
            throw e;
        }
        
        return customers;
    }
    
    public List<Customer> searchByName(String searchTerm) throws SQLException {
        String sql = """
            SELECT * FROM customers 
            WHERE LOWER(first_name) LIKE LOWER(?) OR LOWER(last_name) LIKE LOWER(?) 
            ORDER BY last_name, first_name
        """;
        List<Customer> customers = new ArrayList<>();
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    customers.add(mapResultSetToCustomer(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to search customers by name: " + searchTerm, e);
            throw e;
        }
        
        return customers;
    }
    
    public Customer save(Customer customer) throws SQLException {
        if (!DatabaseUtil.isSQLiteDriverAvailable()) {
            LOGGER.info("SQLite driver not available, using mock database for customer save");
            return MockDatabaseUtil.saveCustomer(customer);
        }

        try {
            if (customer.getCustomerId() == null) {
                return insert(customer);
            } else {
                return update(customer);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Database operation failed, falling back to mock database for customer save", e);
            return MockDatabaseUtil.saveCustomer(customer);
        }
    }
    
    private Customer insert(Customer customer) throws SQLException {
        String sql = """
            INSERT INTO customers (first_name, last_name, email, phone, address, date_of_birth, ssn) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, customer.getFirstName());
            statement.setString(2, customer.getLastName());
            statement.setString(3, customer.getEmail());
            statement.setString(4, customer.getPhone());
            statement.setString(5, customer.getAddress());
            statement.setDate(6, Date.valueOf(customer.getDateOfBirth()));
            statement.setString(7, customer.getSsn());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating customer failed, no rows affected");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    customer.setCustomerId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating customer failed, no ID obtained");
                }
            }
            
            LOGGER.info("Customer created successfully: " + customer.getFullName());
            return customer;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create customer: " + customer.getFullName(), e);
            throw e;
        }
    }
    
    private Customer update(Customer customer) throws SQLException {
        String sql = """
            UPDATE customers SET first_name = ?, last_name = ?, email = ?, phone = ?, 
                               address = ?, date_of_birth = ?, ssn = ?, updated_date = CURRENT_TIMESTAMP 
            WHERE customer_id = ?
        """;
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, customer.getFirstName());
            statement.setString(2, customer.getLastName());
            statement.setString(3, customer.getEmail());
            statement.setString(4, customer.getPhone());
            statement.setString(5, customer.getAddress());
            statement.setDate(6, Date.valueOf(customer.getDateOfBirth()));
            statement.setString(7, customer.getSsn());
            statement.setInt(8, customer.getCustomerId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating customer failed, no rows affected");
            }
            
            LOGGER.info("Customer updated successfully: " + customer.getFullName());
            return customer;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update customer: " + customer.getFullName(), e);
            throw e;
        }
    }
    
    public boolean delete(Integer customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, customerId);
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                LOGGER.info("Customer deleted successfully: " + customerId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete customer: " + customerId, e);
            throw e;
        }
    }
    
    private Customer mapResultSetToCustomer(ResultSet resultSet) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(resultSet.getInt("customer_id"));
        customer.setFirstName(resultSet.getString("first_name"));
        customer.setLastName(resultSet.getString("last_name"));
        customer.setEmail(resultSet.getString("email"));
        customer.setPhone(resultSet.getString("phone"));
        customer.setAddress(resultSet.getString("address"));
        
        Date dateOfBirth = resultSet.getDate("date_of_birth");
        if (dateOfBirth != null) {
            customer.setDateOfBirth(dateOfBirth.toLocalDate());
        }
        
        customer.setSsn(resultSet.getString("ssn"));
        
        Timestamp createdDate = resultSet.getTimestamp("created_date");
        if (createdDate != null) {
            customer.setCreatedDate(createdDate.toLocalDateTime());
        }
        
        Timestamp updatedDate = resultSet.getTimestamp("updated_date");
        if (updatedDate != null) {
            customer.setUpdatedDate(updatedDate.toLocalDateTime());
        }
        
        return customer;
    }
}
