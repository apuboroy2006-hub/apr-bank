package com.aprbank.core.ledger.dto.response;

import com.aprbank.core.ledger.entity.LedgerAccount;
import com.aprbank.core.ledger.enums.AccountStatus;
import com.aprbank.core.ledger.enums.AccountType;

import java.time.Instant;
import java.util.UUID;

public record LedgerAccountResponse(
    UUID id,
    String accountCode,
    String accountName,
    AccountType accountType,
    String currencyCode,
    AccountStatus status,
    Instant createdAt
) {

    public static LedgerAccountResponse from(LedgerAccount account) {
        return new LedgerAccountResponse(
            account.getId(),
            account.getAccountCode(),
            account.getAccountName(),
            account.getAccountType(),
            account.getCurrencyCode(),
            account.getStatus(),
            account.getCreatedAt()
        );
    }
}