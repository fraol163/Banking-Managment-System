package com.bankingsystem.services;

import com.bankingsystem.dao.CustomerDAO;
import com.bankingsystem.models.Customer;
import com.bankingsystem.utils.ValidationUtil;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class CustomerService {
    private static final Logger LOGGER = Logger.getLogger(CustomerService.class.getName());
    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public Customer createCustomer(String firstName, String lastName, String email, 
                                 String phone, String address, LocalDate dateOfBirth, String ssn) 
            throws SQLException, IllegalArgumentException {
        
        // Validate input
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email address is required");
        }
        
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address is required");
        }
        
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth is required");
        }
        
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
        
        if (ssn == null || ssn.trim().isEmpty()) {
            throw new IllegalArgumentException("SSN is required");
        }
        
        // Create customer
        Customer customer = new Customer(
            firstName.trim(), 
            lastName.trim(), 
            email.trim(), 
            phone.trim(), 
            address.trim(), 
            dateOfBirth, 
            ssn.trim()
        );
        
        customer = customerDAO.save(customer);
        
        LOGGER.info(String.format("Customer created successfully: %s (ID: %d)", 
                                customer.getFullName(), customer.getCustomerId()));
        return customer;
    }
    
    public Customer getCustomerById(Integer customerId) throws SQLException {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        
        return customerDAO.findById(customerId);
    }
    
    public Customer getCustomerBySSN(String ssn) throws SQLException {
        if (ssn == null || ssn.trim().isEmpty()) {
            throw new IllegalArgumentException("SSN cannot be null or empty");
        }
        
        return customerDAO.findBySSN(ssn.trim());
    }
    
    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.findAll();
    }
    
    public Customer updateCustomer(Customer customer) throws SQLException {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        
        if (customer.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required for update");
        }
        
        Customer existingCustomer = customerDAO.findById(customer.getCustomerId());
        if (existingCustomer == null) {
            throw new IllegalArgumentException("Customer not found with ID: " + customer.getCustomerId());
        }
        
        customer = customerDAO.save(customer);
        
        LOGGER.info(String.format("Customer updated successfully: %s (ID: %d)", 
                                customer.getFullName(), customer.getCustomerId()));
        return customer;
    }
    
    public boolean deleteCustomer(Integer customerId) throws SQLException {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        
        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            return false;
        }
        
        // Check if customer has any accounts
        // Note: This would require AccountService dependency, but for simplicity we'll skip this check
        // In a real application, you'd want to prevent deletion of customers with active accounts
        
        boolean deleted = customerDAO.delete(customerId);
        
        if (deleted) {
            LOGGER.info(String.format("Customer deleted successfully: %s (ID: %d)", 
                                    customer.getFullName(), customerId));
        }
        
        return deleted;
    }
    
    public List<Customer> searchCustomers(String searchTerm) throws SQLException {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllCustomers();
        }
        
        return customerDAO.searchByName(searchTerm.trim());
    }
    
    public boolean customerExists(Integer customerId) throws SQLException {
        if (customerId == null) {
            return false;
        }
        
        return customerDAO.findById(customerId) != null;
    }
    
    public int getCustomerAge(Integer customerId) throws SQLException {
        Customer customer = getCustomerById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found with ID: " + customerId);
        }
        
        return customer.getAge();
    }
    
    public String getCustomerDisplayName(Integer customerId) throws SQLException {
        Customer customer = getCustomerById(customerId);
        if (customer == null) {
            return "Customer not found";
        }
        
        return customer.getFullName();
    }
}
