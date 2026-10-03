package com.m2ibank.account.dto;

import com.m2ibank.account.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class AccountRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Initial balance must be greater than or equal to zero")
    private BigDecimal initialBalance;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}
