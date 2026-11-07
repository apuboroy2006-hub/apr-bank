package com.aprbank.core.ledger.service;

import com.aprbank.core.ledger.entity.LedgerTransaction;
import com.aprbank.core.ledger.enums.LedgerTransactionStatus;
import com.aprbank.core.ledger.repository.LedgerTransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
public class LedgerTransactionService {

    private static final Pattern TRANSACTION_TYPE_PATTERN =
            Pattern.compile("^[A-Z][A-Z0-9_]{1,39}$");

    private final LedgerTransactionRepository transactionRepository;

    public LedgerTransactionService(
            LedgerTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public LedgerTransaction createTransaction(
            String transactionType,
            String description,
            String idempotencyKey) {

        if (transactionType == null || transactionType.isBlank()) {
            throw new IllegalArgumentException(
                    "Transaction type is required");
        }

        String normalizedType = transactionType.trim()
                .toUpperCase(Locale.ROOT);

        if (!TRANSACTION_TYPE_PATTERN.matcher(normalizedType).matches()) {
            throw new IllegalArgumentException(
                    "Invalid transaction type");
        }

        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException(
                    "Description must not exceed 500 characters");
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency key is required");
        }

        String normalizedKey = idempotencyKey.trim();

        if (normalizedKey.length() > 128) {
            throw new IllegalArgumentException(
                    "Idempotency key must not exceed 128 characters");
        }

        return transactionRepository.findByIdempotencyKey(normalizedKey)
                .orElseGet(() -> saveNewTransaction(
                        normalizedType,
                        description == null ? null : description.trim(),
                        normalizedKey
                ));
    }

    private LedgerTransaction saveNewTransaction(
            String transactionType,
            String description,
            String idempotencyKey) {

        String referenceNumber = "APR-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .toUpperCase(Locale.ROOT);

        LedgerTransaction transaction = LedgerTransaction.create(
                referenceNumber,
                transactionType,
                description,
                idempotencyKey
        );

        try {
            return transactionRepository.saveAndFlush(transaction);
        } catch (DataIntegrityViolationException exception) {
            return transactionRepository
                    .findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> exception);
        }
    }
}