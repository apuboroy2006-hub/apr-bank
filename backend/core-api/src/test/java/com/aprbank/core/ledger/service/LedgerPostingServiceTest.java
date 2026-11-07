package com.aprbank.core.ledger.service;

import com.aprbank.core.ledger.dto.request.LedgerPostingRequest;
import com.aprbank.core.ledger.entity.LedgerAccount;
import com.aprbank.core.ledger.entity.LedgerTransaction;
import com.aprbank.core.ledger.enums.AccountStatus;
import com.aprbank.core.ledger.enums.EntrySide;
import com.aprbank.core.ledger.enums.LedgerTransactionStatus;
import com.aprbank.core.ledger.repository.LedgerAccountRepository;
import com.aprbank.core.ledger.repository.LedgerEntryRepository;
import com.aprbank.core.ledger.repository.LedgerTransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LedgerPostingServiceTest {

    @Mock
    private LedgerTransactionRepository transactionRepository;

    @Mock
    private LedgerAccountRepository accountRepository;

    @Mock
    private LedgerEntryRepository entryRepository;

    @InjectMocks
    private LedgerPostingService postingService;

    private LedgerBalanceValidator validator;

    private UUID transactionId;
    private UUID debitAccountId;
    private UUID creditAccountId;

    private LedgerTransaction transaction;
    private LedgerAccount debitAccount;
    private LedgerAccount creditAccount;

    @BeforeEach
    void setUp() {

        validator = new LedgerBalanceValidator();

        postingService = new LedgerPostingService(
            transactionRepository,
            accountRepository,
            entryRepository,
            validator
        );

        transactionId = UUID.randomUUID();
        debitAccountId = UUID.randomUUID();
        creditAccountId = UUID.randomUUID();

        transaction = mock(LedgerTransaction.class);
        debitAccount = mock(LedgerAccount.class);
        creditAccount = mock(LedgerAccount.class);

        when(transaction.getStatus())
            .thenReturn(LedgerTransactionStatus.PENDING);

        when(debitAccount.getId()).thenReturn(debitAccountId);
        when(debitAccount.getStatus()).thenReturn(AccountStatus.ACTIVE);
        when(debitAccount.getCurrencyCode()).thenReturn("INR");
        when(debitAccount.getAccountCode()).thenReturn("CASH-001");

        when(creditAccount.getId()).thenReturn(creditAccountId);
        when(creditAccount.getStatus()).thenReturn(AccountStatus.ACTIVE);
        when(creditAccount.getCurrencyCode()).thenReturn("INR");
        when(creditAccount.getAccountCode()).thenReturn("BANK-001");

        when(transactionRepository.findByIdForUpdate(transactionId))
            .thenReturn(Optional.of(transaction));

        when(entryRepository.findByTransaction_Id(transactionId))
            .thenReturn(List.of());

        when(accountRepository.findAllById(any()))
            .thenReturn(List.of(debitAccount, creditAccount));

        when(transactionRepository.save(transaction))
            .thenReturn(transaction);
    }

    private LedgerPostingRequest balancedRequest() {

        return new LedgerPostingRequest(
            transactionId,
            List.of(
                new LedgerPostingRequest.PostingLine(
                    debitAccountId,
                    EntrySide.DEBIT,
                    new BigDecimal("1000.00"),
                    "INR"
                ),
                new LedgerPostingRequest.PostingLine(
                    creditAccountId,
                    EntrySide.CREDIT,
                    new BigDecimal("1000.00"),
                    "INR"
                )
            )
        );
    }

    @Test
    void shouldPostBalancedTransaction() {

        LedgerTransaction result =
            postingService.postTransaction(balancedRequest());

        assertSame(transaction, result);

        verify(entryRepository).saveAll(any());

        verify(transaction).setStatus(LedgerTransactionStatus.POSTED);

        verify(transaction).setPostedAt(any());

        verify(transactionRepository).save(transaction);
    }

    @Test
    void shouldRejectUnbalancedTransaction() {

        LedgerPostingRequest request = new LedgerPostingRequest(
            transactionId,
            List.of(
                new LedgerPostingRequest.PostingLine(
                    debitAccountId,
                    EntrySide.DEBIT,
                    new BigDecimal("1000.00"),
                    "INR"
                ),
                new LedgerPostingRequest.PostingLine(
                    creditAccountId,
                    EntrySide.CREDIT,
                    new BigDecimal("900.00"),
                    "INR"
                )
            )
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> postingService.postTransaction(request)
        );

        verify(entryRepository, never()).saveAll(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldRejectAlreadyPostedTransaction() {

        when(transaction.getStatus())
            .thenReturn(LedgerTransactionStatus.POSTED);

        assertThrows(
            IllegalStateException.class,
            () -> postingService.postTransaction(balancedRequest())
        );

        verify(entryRepository, never()).saveAll(any());
    }

    @Test
    void shouldRejectInactiveAccount() {

        when(debitAccount.getStatus()).thenReturn(AccountStatus.BLOCKED);

        assertThrows(
            IllegalStateException.class,
            () -> postingService.postTransaction(balancedRequest())
        );

        verify(entryRepository, never()).saveAll(any());
    }

    @Test
    void shouldRejectCurrencyMismatch() {

        when(debitAccount.getCurrencyCode()).thenReturn("USD");

        assertThrows(
            IllegalArgumentException.class,
            () -> postingService.postTransaction(balancedRequest())
        );

        verify(entryRepository, never()).saveAll(any());
    }
}