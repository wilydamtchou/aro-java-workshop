package com.m2ibank.account.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String accountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType accountType;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Account() {
        this.createdAt = LocalDateTime.now();
    }

    public Account(String accountNumber, BigDecimal balance, AccountType accountType, Long customerId) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.accountType = accountType;
        this.customerId = customerId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
