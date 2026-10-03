package com.m2ibank.account.service;

import com.m2ibank.account.dto.AccountRequest;
import com.m2ibank.account.dto.AccountResponse;
import com.m2ibank.account.entity.Account;
import com.m2ibank.account.entity.AccountType;
import com.m2ibank.account.repository.AccountRepository;
import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.common.exception.ResourceNotFoundException;
import com.m2ibank.customer.entity.Customer;
import com.m2ibank.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private AccountService accountService;

    private AccountRequest request;
    private Customer customer;
    private Account account;

    @BeforeEach
    void setUp() {
        request = new AccountRequest();
        request.setCustomerId(1L);
        request.setAccountType(AccountType.CURRENT);
        request.setInitialBalance(new BigDecimal("100000.00"));

        customer = new Customer("Alice Ndzi", "alice@m2ibank.com", "+237600000001", "CNI000001");
        account = new Account("DB-ABC12345", new BigDecimal("100000.00"), AccountType.CURRENT, 1L);
    }

    @Test
    void shouldCreateAccountSuccessfully() {
        when(customerService.getCustomerEntityById(1L)).thenReturn(customer);
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            java.lang.reflect.Field idField = Account.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(saved, 1L);
            return saved;
        });

        AccountResponse response = accountService.createAccount(request);

        assertNotNull(response);
        assertEquals(AccountType.CURRENT, response.getAccountType());
        assertEquals(new BigDecimal("100000.00"), response.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void shouldReturnAccountById() throws Exception {
        java.lang.reflect.Field idField = Account.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(account, 1L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccountById(1L);

        assertEquals(1L, response.getId());
        assertEquals("DB-ABC12345", response.getAccountNumber());
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> accountService.getAccountById(99L));
    }

    @Test
    void shouldDebitAccountSuccessfully() {
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.debitAccount(account, new BigDecimal("5000.00"));

        assertEquals(new BigDecimal("95000.00"), account.getBalance());
    }

    @Test
    void shouldCreditAccountSuccessfully() {
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.creditAccount(account, new BigDecimal("5000.00"));

        assertEquals(new BigDecimal("105000.00"), account.getBalance());
    }

    @Test
    void shouldReturnAccountsByCustomerId() {
        when(customerService.getCustomerEntityById(1L)).thenReturn(customer);
        when(accountRepository.findByCustomerId(1L)).thenReturn(List.of(account));

        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(1L);

        assertEquals(1, accounts.size());
        assertEquals(1L, accounts.get(0).getCustomerId());
    }

    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficient() {
        Account account = new Account("DB-TEST001", new BigDecimal("5000.00"), AccountType.CURRENT, 1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> accountService.debitAccount(account, new BigDecimal("6000.00"))
        );

        assertEquals("Insufficient balance for transfer", exception.getMessage());
    }

    @Test
    void shouldDebitAccountWhenBalanceIsSufficient() {
        Account account = new Account("DB-TEST002", new BigDecimal("5000.00"), AccountType.CURRENT, 1L);

        accountService.debitAccount(account, new BigDecimal("1000.00"));

        assertEquals(new BigDecimal("4000.00"), account.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void shouldThrowExceptionWhenAmountIsZeroOrNegative() {
        Account account = new Account("DB-TEST003", new BigDecimal("5000.00"), AccountType.CURRENT, 1L);

        assertThrows(BusinessException.class,
                () -> accountService.debitAccount(account, BigDecimal.ZERO));

        assertThrows(BusinessException.class,
                () -> accountService.debitAccount(account, new BigDecimal("-10.00")));
    }

}
