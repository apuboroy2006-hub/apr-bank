package com.aprbank.core.ledger.service;

import com.aprbank.core.ledger.entity.LedgerAccount;
import com.aprbank.core.ledger.enums.AccountStatus;
import com.aprbank.core.ledger.enums.AccountType;
import com.aprbank.core.ledger.repository.LedgerAccountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LedgerAccountServiceTest {

    @Mock
    private LedgerAccountRepository accountRepository;

    private LedgerAccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new LedgerAccountService(accountRepository);
    }

    @Test
    void shouldCreateValidAccount() {

        when(accountRepository.existsByAccountCode("CASH-001"))
            .thenReturn(false);

        when(accountRepository.save(any(LedgerAccount.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        LedgerAccount result = accountService.createAccount(
            "cash-001",
            "Cash Reserve",
            AccountType.ASSET,
            "inr"
        );

        assertEquals("CASH-001", result.getAccountCode());
        assertEquals("Cash Reserve", result.getAccountName());
        assertEquals(AccountType.ASSET, result.getAccountType());
        assertEquals("INR", result.getCurrencyCode());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());

        verify(accountRepository).save(any(LedgerAccount.class));
    }

    @Test
    void shouldRejectDuplicateAccountCode() {

        when(accountRepository.existsByAccountCode("CASH-001"))
            .thenReturn(true);

        assertThrows(
            IllegalStateException.class,
            () -> accountService.createAccount(
                "CASH-001",
                "Cash Reserve",
                AccountType.ASSET,
                "INR"
            )
        );

        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidCurrency() {

        assertThrows(
            IllegalArgumentException.class,
            () -> accountService.createAccount(
                "CASH-001",
                "Cash Reserve",
                AccountType.ASSET,
                "INDIA"
            )
        );

        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidAccountCode() {

        assertThrows(
            IllegalArgumentException.class,
            () -> accountService.createAccount(
                "A!",
                "Cash Reserve",
                AccountType.ASSET,
                "INR"
            )
        );

        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldRejectBlankAccountName() {

        assertThrows(
            IllegalArgumentException.class,
            () -> accountService.createAccount(
                "CASH-001",
                "   ",
                AccountType.ASSET,
                "INR"
            )
        );

        verify(accountRepository, never()).save(any());
    }
}