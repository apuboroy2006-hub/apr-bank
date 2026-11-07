package com.aprbank.core.ledger.dto.response;

import com.aprbank.core.ledger.entity.LedgerTransaction;
import com.aprbank.core.ledger.enums.LedgerTransactionStatus;

import java.time.Instant;
import java.util.UUID;

public record LedgerTransactionResponse(
        UUID id,
        String referenceNumber,
        String transactionType,
        LedgerTransactionStatus status,
        String description,
        String idempotencyKey,
        Instant initiatedAt,
        Instant postedAt
) {

    public static LedgerTransactionResponse from(
            LedgerTransaction transaction) {

        return new LedgerTransactionResponse(
                transaction.getId(),
                transaction.getReferenceNumber(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getIdempotencyKey(),
                transaction.getInitiatedAt(),
                transaction.getPostedAt()
        );
    }
}