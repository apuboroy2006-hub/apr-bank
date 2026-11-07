package com.aprbank.core.ledger.service;

import com.aprbank.core.ledger.entity.LedgerTransaction;
import com.aprbank.core.ledger.enums.LedgerTransactionStatus;
import com.aprbank.core.ledger.repository.LedgerTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerTransactionServiceTest {

    @Mock
    private LedgerTransactionRepository transactionRepository;

    @InjectMocks
    private LedgerTransactionService transactionService;

    @Test
    void shouldCreatePendingTransaction() {
        when(transactionRepository.findByIdempotencyKey("KEY-001"))
                .thenReturn(Optional.empty());

        when(transactionRepository.saveAndFlush(any(LedgerTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LedgerTransaction result = transactionService.createTransaction(
                "fund_transfer",
                "Test transfer",
                "KEY-001"
        );

        assertNotNull(result.getReferenceNumber());
        assertTrue(result.getReferenceNumber().startsWith("APR-"));
        assertEquals("FUND_TRANSFER", result.getTransactionType());
        assertEquals("Test transfer", result.getDescription());
        assertEquals("KEY-001", result.getIdempotencyKey());
        assertEquals(LedgerTransactionStatus.PENDING, result.getStatus());

        verify(transactionRepository).saveAndFlush(any(LedgerTransaction.class));
    }

    @Test
    void shouldReturnExistingTransactionForSameIdempotencyKey() {
        LedgerTransaction existing = new LedgerTransactionForTest()
                .create("KEY-002");

        when(transactionRepository.findByIdempotencyKey("KEY-002"))
                .thenReturn(Optional.of(existing));

        LedgerTransaction result = transactionService.createTransaction(
                "CASH_DEPOSIT",
                "Retry request",
                "KEY-002"
        );

        assertSame(existing, result);
        verify(transactionRepository, never())
                .saveAndFlush(any(LedgerTransaction.class));
    }

    @Test
    void shouldRejectBlankTransactionType() {
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(
                        " ", "Description", "KEY-003"));
    }

    @Test
    void shouldRejectInvalidTransactionType() {
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(
                        "INVALID TYPE!", "Description", "KEY-004"));
    }

    @Test
    void shouldRejectMissingIdempotencyKey() {
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(
                        "CASH_DEPOSIT", "Description", " "));
    }

    @Test
    void shouldRejectDescriptionLongerThan500Characters() {
        String description = "A".repeat(501);

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(
                        "CASH_DEPOSIT", description, "KEY-005"));
    }

    private static class LedgerTransactionForTest {
        LedgerTransaction create(String key) {
            return LedgerTransaction.create(
                    "APR-EXISTING",
                    "CASH_DEPOSIT",
                    null,
                    key
            );
        }
    }
}