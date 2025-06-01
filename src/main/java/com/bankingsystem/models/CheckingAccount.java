package com.bankingsystem.models;

import com.bankingsystem.config.AppConfig;
import java.math.BigDecimal;

public class CheckingAccount extends AbstractAccount {
    private static final BigDecimal MINIMUM_BALANCE = BigDecimal.ZERO;
    private static final BigDecimal INTEREST_RATE = BigDecimal.ZERO;
    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("500.00");
    private static final BigDecimal OVERDRAFT_FEE = new BigDecimal("25.00");
    private static final BigDecimal DAILY_WITHDRAWAL_LIMIT = AppConfig.MAX_DAILY_WITHDRAWAL_LIMIT;
    
    public CheckingAccount() {
        super();
    }
    
    public CheckingAccount(String accountNumber, Integer customerId, Integer accountTypeId) {
        super(accountNumber, customerId, accountTypeId);
    }
    
    @Override
    public BigDecimal getMinimumBalance() {
        return MINIMUM_BALANCE;
    }
    
    @Override
    public BigDecimal getInterestRate() {
        return INTEREST_RATE;
    }
    
    @Override
    public BigDecimal getOverdraftLimit() {
        return OVERDRAFT_LIMIT;
    }
    
    @Override
    public BigDecimal getOverdraftFee() {
        return OVERDRAFT_FEE;
    }
    
    @Override
    public BigDecimal getDailyWithdrawalLimit() {
        return DAILY_WITHDRAWAL_LIMIT;
    }
    
    @Override
    public boolean canWithdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        BigDecimal balanceAfterWithdrawal = balance.subtract(amount);
        BigDecimal minimumAllowedBalance = MINIMUM_BALANCE.subtract(OVERDRAFT_LIMIT);
        return balanceAfterWithdrawal.compareTo(minimumAllowedBalance) >= 0;
    }
    
    @Override
    public BigDecimal calculateInterest() {
        return BigDecimal.ZERO;
    }
    
    public boolean isOverdrawn() {
        return balance.compareTo(BigDecimal.ZERO) < 0;
    }
    
    public BigDecimal getOverdraftAmount() {
        if (isOverdrawn()) {
            return balance.abs();
        }
        return BigDecimal.ZERO;
    }
    
    public String getAccountType() {
        return AppConfig.CHECKING_ACCOUNT_TYPE;
    }
}
