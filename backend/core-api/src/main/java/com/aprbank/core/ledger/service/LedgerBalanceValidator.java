package com.aprbank.core.ledger.service;

import com.aprbank.core.ledger.enums.EntrySide;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class LedgerBalanceValidator {

    public void validate(List<LedgerPostingLine> entries) {

        if (entries == null || entries.size() < 2) {
            throw new IllegalArgumentException(
                "A ledger transaction requires at least two entries."
            );
        }

        Map<String, BigDecimal> debits = new HashMap<>();
        Map<String, BigDecimal> credits = new HashMap<>();

        for (LedgerPostingLine entry : entries) {

            if (entry.currencyCode() == null ||
                    !entry.currencyCode().matches("[A-Z]{3}")) {
                throw new IllegalArgumentException(
                    "Invalid ISO-style currency code."
                );
            }

            if (entry.amount() == null ||
                    entry.amount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                    "Ledger entry amount must be greater than zero."
                );
            }

            if (entry.side() == null) {
                throw new IllegalArgumentException(
                    "Ledger entry side is required."
                );
            }

            Map<String, BigDecimal> target =
                entry.side() == EntrySide.DEBIT ? debits : credits;

            target.merge(
                entry.currencyCode(),
                entry.amount(),
                BigDecimal::add
            );
        }

        if (!debits.keySet().equals(credits.keySet())) {
            throw new IllegalArgumentException(
                "Every currency must have both debit and credit entries."
            );
        }

        for (String currency : debits.keySet()) {

            if (debits.get(currency).compareTo(credits.get(currency)) != 0) {
                throw new IllegalArgumentException(
                    "Unbalanced ledger transaction for currency: " + currency
                );
            }
        }
    }

    public record LedgerPostingLine(
        String currencyCode,
        EntrySide side,
        BigDecimal amount
    ) {
    }
}