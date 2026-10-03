package com.m2ibank.transfer.service;

import com.m2ibank.account.entity.Account;
import com.m2ibank.account.entity.AccountType;
import com.m2ibank.account.service.AccountService;
import com.m2ibank.common.exception.BusinessException;
import com.m2ibank.transfer.dto.TransferRequest;
import com.m2ibank.transfer.dto.TransferResponse;
import com.m2ibank.transfer.entity.Transfer;
import com.m2ibank.transfer.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransferService transferService;

    private TransferRequest request;
    private Account sourceAccount;
    private Account destinationAccount;

    @BeforeEach
    void setUp() {
        request = new TransferRequest();
        request.setSourceAccountId(1L);
        request.setDestinationAccountId(2L);
        request.setAmount(new BigDecimal("5000.00"));
        request.setDescription("Monthly transfer");

        sourceAccount = new Account("DB-SRC12345", new BigDecimal("100000.00"), AccountType.CURRENT, 1L);
        destinationAccount = new Account("DB-DST12345", new BigDecimal("50000.00"), AccountType.SAVINGS, 2L);
    }

    @Test
    void shouldCreateTransferSuccessfully() {
        when(accountService.getAccountEntityById(1L)).thenReturn(sourceAccount);
        when(accountService.getAccountEntityById(2L)).thenReturn(destinationAccount);
        doNothing().when(accountService).debitAccount(sourceAccount, new BigDecimal("5000.00"));
        doNothing().when(accountService).creditAccount(destinationAccount, new BigDecimal("5000.00"));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer saved = invocation.getArgument(0);
            java.lang.reflect.Field idField = Transfer.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(saved, 1L);
            return saved;
        });

        TransferResponse response = transferService.createTransfer(request);

        assertNotNull(response);
        assertEquals(1L, response.getSourceAccountId());
        assertEquals(2L, response.getDestinationAccountId());
        assertEquals(new BigDecimal("5000.00"), response.getAmount());
        verify(accountService, times(1)).debitAccount(sourceAccount, new BigDecimal("5000.00"));
        verify(accountService, times(1)).creditAccount(destinationAccount, new BigDecimal("5000.00"));
    }

    @Test
    void shouldThrowExceptionWhenSourceAndDestinationAreSame() {
        request.setDestinationAccountId(1L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> transferService.createTransfer(request));

        assertEquals("Source and destination accounts must be different", exception.getMessage());
    }

    @Test
    void shouldReturnTransfersForAccount() {
        Transfer transfer = new Transfer(1L, 2L, new BigDecimal("5000.00"), "Monthly transfer");
        when(accountService.getAccountEntityById(1L)).thenReturn(sourceAccount);
        when(transferRepository.findBySourceAccountIdOrDestinationAccountId(1L, 1L))
                .thenReturn(List.of(transfer));

        List<TransferResponse> transfers = transferService.getTransfersForAccount(1L);

        assertEquals(1, transfers.size());
        assertEquals(new BigDecimal("5000.00"), transfers.get(0).getAmount());
    }
}
