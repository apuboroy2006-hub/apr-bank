package com.aprbank.core.ledger.service;

import com.aprbank.core.ledger.enums.EntrySide;
import com.aprbank.core.ledger.service.LedgerBalanceValidator.LedgerPostingLine;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LedgerBalanceValidatorTest {

    private final LedgerBalanceValidator validator =
        new LedgerBalanceValidator();

    @Test
    void shouldAcceptBalancedTransaction() {

        List<LedgerPostingLine> entries = List.of(
            new LedgerPostingLine("INR", EntrySide.DEBIT,
                new BigDecimal("1000.00")),
            new LedgerPostingLine("INR", EntrySide.CREDIT,
                new BigDecimal("1000.00"))
        );

        assertDoesNotThrow(() -> validator.validate(entries));
    }

    @Test
    void shouldRejectUnbalancedTransaction() {

        List<LedgerPostingLine> entries = List.of(
            new LedgerPostingLine("INR", EntrySide.DEBIT,
                new BigDecimal("1000.00")),
            new LedgerPostingLine("INR", EntrySide.CREDIT,
                new BigDecimal("900.00"))
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(entries)
        );
    }

    @Test
    void shouldRejectCrossCurrencyBalancing() {

        List<LedgerPostingLine> entries = List.of(
            new LedgerPostingLine("INR", EntrySide.DEBIT,
                new BigDecimal("1000.00")),
            new LedgerPostingLine("USD", EntrySide.CREDIT,
                new BigDecimal("1000.00"))
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(entries)
        );
    }

    @Test
    void shouldRejectZeroAmount() {

        List<LedgerPostingLine> entries = List.of(
            new LedgerPostingLine("INR", EntrySide.DEBIT,
                BigDecimal.ZERO),
            new LedgerPostingLine("INR", EntrySide.CREDIT,
                BigDecimal.ZERO)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(entries)
        );
    }

    @Test
    void shouldRejectNegativeAmount() {

        List<LedgerPostingLine> entries = List.of(
            new LedgerPostingLine("INR", EntrySide.DEBIT,
                new BigDecimal("-100.00")),
            new LedgerPostingLine("INR", EntrySide.CREDIT,
                new BigDecimal("-100.00"))
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(entries)
        );
    }

    @Test
    void shouldRejectSingleEntry() {

        List<LedgerPostingLine> entries = List.of(
            new LedgerPostingLine("INR", EntrySide.DEBIT,
                new BigDecimal("100.00"))
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(entries)
        );
    }
}