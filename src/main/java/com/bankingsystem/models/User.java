package com.bankingsystem.models;

import com.bankingsystem.config.AppConfig;
import java.util.Arrays;
import java.util.List;

public class User extends AbstractUser {

    public User() {
        super();
    }

    public User(String username, String passwordHash, String role,
               String firstName, String lastName, String email) {
        super(username, passwordHash, role, firstName, lastName, email);
    }

    @Override
    public boolean hasPermission(String permission) {
        switch (role) {
            case AppConfig.ROLE_ADMIN:
                return getAdminPermissions().contains(permission);
            case AppConfig.ROLE_MANAGER:
                return getManagerPermissions().contains(permission);
            case AppConfig.ROLE_TELLER:
                return getTellerPermissions().contains(permission);
            case AppConfig.ROLE_CUSTOMER:
                return getCustomerPermissions().contains(permission);
            default:
                return false;
        }
    }

    private List<String> getAdminPermissions() {
        return Arrays.asList(
            AppConfig.PERMISSION_CREATE_USER, AppConfig.PERMISSION_UPDATE_USER,
            AppConfig.PERMISSION_DELETE_USER, AppConfig.PERMISSION_VIEW_ALL_USERS,
            AppConfig.PERMISSION_CREATE_ACCOUNT, AppConfig.PERMISSION_UPDATE_ACCOUNT,
            AppConfig.PERMISSION_DELETE_ACCOUNT, AppConfig.PERMISSION_VIEW_ALL_ACCOUNTS,
            AppConfig.PERMISSION_PROCESS_TRANSACTION, AppConfig.PERMISSION_REVERSE_TRANSACTION,
            AppConfig.PERMISSION_VIEW_ALL_TRANSACTIONS, AppConfig.PERMISSION_GENERATE_REPORTS,
            AppConfig.PERMISSION_BACKUP_DATABASE, AppConfig.PERMISSION_RESTORE_DATABASE,
            AppConfig.PERMISSION_MANAGE_SYSTEM_CONFIG, AppConfig.PERMISSION_VIEW_AUDIT_LOG,
            AppConfig.PERMISSION_APPROVE_TRANSACTIONS, AppConfig.PERMISSION_PROCESS_LARGE_TRANSACTIONS,
            AppConfig.PERMISSION_CREATE_ADMIN_USER, AppConfig.PERMISSION_CREATE_MANAGER_USER,
            AppConfig.PERMISSION_CREATE_TELLER_USER
        );
    }

    private List<String> getManagerPermissions() {
        return Arrays.asList(
            AppConfig.PERMISSION_CREATE_MANAGER_USER, AppConfig.PERMISSION_CREATE_TELLER_USER,
            AppConfig.PERMISSION_UPDATE_USER, AppConfig.PERMISSION_VIEW_ALL_USERS,
            AppConfig.PERMISSION_CREATE_ACCOUNT, AppConfig.PERMISSION_UPDATE_ACCOUNT,
            AppConfig.PERMISSION_DELETE_ACCOUNT, AppConfig.PERMISSION_VIEW_ALL_ACCOUNTS,
            AppConfig.PERMISSION_PROCESS_TRANSACTION, AppConfig.PERMISSION_REVERSE_TRANSACTION,
            AppConfig.PERMISSION_VIEW_ALL_TRANSACTIONS, AppConfig.PERMISSION_GENERATE_REPORTS,
            AppConfig.PERMISSION_VIEW_AUDIT_LOG, AppConfig.PERMISSION_APPROVE_TRANSACTIONS,
            AppConfig.PERMISSION_PROCESS_LARGE_TRANSACTIONS
        );
    }

    private List<String> getTellerPermissions() {
        return Arrays.asList(
            AppConfig.PERMISSION_VIEW_ACCOUNT, AppConfig.PERMISSION_PROCESS_TRANSACTION,
            AppConfig.PERMISSION_VIEW_TRANSACTION, AppConfig.PERMISSION_GENERATE_CUSTOMER_REPORTS
        );
    }

    private List<String> getCustomerPermissions() {
        return Arrays.asList(
            "VIEW_OWN_ACCOUNT", "VIEW_OWN_TRANSACTIONS",
            "PROCESS_OWN_TRANSACTION", "GENERATE_OWN_REPORTS"
        );
    }

    public boolean canAccessAccount(Integer accountId) {
        if (hasPermission(AppConfig.PERMISSION_VIEW_ALL_ACCOUNTS)) {
            return true;
        }
        return false;
    }

    public boolean canProcessTransaction(String transactionType) {
        if (!hasPermission(AppConfig.PERMISSION_PROCESS_TRANSACTION)) {
            return false;
        }

        if (AppConfig.TRANSACTION_TYPE_REVERSAL.equals(transactionType)) {
            return hasPermission(AppConfig.PERMISSION_REVERSE_TRANSACTION);
        }

        return true;
    }

    public boolean canProcessTransactionAmount(java.math.BigDecimal amount) {
        if (isAdmin()) {
            return true;
        } else if (isManager()) {
            return amount.compareTo(AppConfig.MANAGER_TRANSACTION_LIMIT) <= 0;
        } else if (isTeller()) {
            return amount.compareTo(AppConfig.TELLER_TRANSACTION_LIMIT) <= 0;
        }
        return false;
    }

    public boolean requiresApproval(java.math.BigDecimal amount) {
        if (isAdmin()) {
            return false;
        } else if (isManager()) {
            return amount.compareTo(AppConfig.MANAGER_APPROVAL_THRESHOLD) > 0;
        } else if (isTeller()) {
            return amount.compareTo(AppConfig.TELLER_APPROVAL_THRESHOLD) > 0;
        }
        return true;
    }

    public boolean canCreateUserRole(String targetRole) {
        if (isAdmin()) {
            return true;
        } else if (isManager()) {
            return AppConfig.ROLE_TELLER.equals(targetRole) || AppConfig.ROLE_MANAGER.equals(targetRole);
        }
        return false;
    }

    public boolean isAdmin() {
        return AppConfig.ROLE_ADMIN.equals(role);
    }

    public boolean isManager() {
        return AppConfig.ROLE_MANAGER.equals(role);
    }

    public boolean isTeller() {
        return AppConfig.ROLE_TELLER.equals(role);
    }

    public boolean isCustomer() {
        return AppConfig.ROLE_CUSTOMER.equals(role);
    }
}
