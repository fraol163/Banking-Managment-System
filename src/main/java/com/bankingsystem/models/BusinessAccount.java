package com.bankingsystem.models;

import com.bankingsystem.config.AppConfig;
import java.math.BigDecimal;

public class BusinessAccount extends AbstractAccount {
    private static final BigDecimal MINIMUM_BALANCE = new BigDecimal("1000.00");
    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.015");
    private static final BigDecimal OVERDRAFT_LIMIT = BigDecimal.ZERO;
    private static final BigDecimal OVERDRAFT_FEE = BigDecimal.ZERO;
    private static final BigDecimal DAILY_WITHDRAWAL_LIMIT = new BigDecimal("5000.00");
    
    public BusinessAccount() {
        super();
    }
    
    public BusinessAccount(String accountNumber, Integer customerId, Integer accountTypeId) {
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
        return balanceAfterWithdrawal.compareTo(MINIMUM_BALANCE) >= 0;
    }
    
    @Override
    public BigDecimal calculateInterest() {
        return balance.multiply(INTEREST_RATE).divide(new BigDecimal("12"), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    public String getAccountType() {
        return AppConfig.BUSINESS_ACCOUNT_TYPE;
    }
}
