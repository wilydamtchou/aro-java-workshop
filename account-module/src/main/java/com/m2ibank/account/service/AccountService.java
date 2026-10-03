package com.m2ibank.account.service;

import com.m2ibank.account.dto.AccountRequest;
import com.m2ibank.account.dto.AccountResponse;
import com.m2ibank.account.entity.Account;
import com.m2ibank.account.repository.AccountRepository;
import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.common.exception.ResourceNotFoundException;
import com.m2ibank.customer.service.CustomerService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerService customerService;

    public AccountService(AccountRepository accountRepository, CustomerService customerService) {
        this.accountRepository = accountRepository;
        this.customerService = customerService;
    }

    public AccountResponse createAccount(AccountRequest request) {
        customerService.getCustomerEntityById(request.getCustomerId());

        String accountNumber = generateAccountNumber();
        while (accountRepository.findByAccountNumber(accountNumber).isPresent()) {
            accountNumber = generateAccountNumber();
        }

        Account account = new Account(
                accountNumber,
                request.getInitialBalance(),
                request.getAccountType(),
                request.getCustomerId()
        );

        Account saved = accountRepository.save(account);
        return mapToResponse(saved);
    }

    public AccountResponse getAccountById(Long id) {
        return mapToResponse(getAccountEntityById(id));
    }

    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {
        customerService.getCustomerEntityById(customerId);
        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Account getAccountEntityById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id " + id));
    }

    public void debitAccount(Account account, BigDecimal amount) {
        if (account == null) {
            throw new BusinessException("Account must not be null");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new BusinessException("Debit amount must be greater than zero");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance for transfer");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
    }


    public void creditAccount(Account account, java.math.BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
    }

    private String generateAccountNumber() {
        return "DB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getAccountType(),
                account.getCustomerId(),
                account.getCreatedAt()
        );
    }
}
