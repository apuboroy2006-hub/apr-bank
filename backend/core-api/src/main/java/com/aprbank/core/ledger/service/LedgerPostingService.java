package com.aprbank.core.ledger.service;

import com.aprbank.core.ledger.dto.request.LedgerPostingRequest;
import com.aprbank.core.ledger.entity.LedgerAccount;
import com.aprbank.core.ledger.entity.LedgerEntry;
import com.aprbank.core.ledger.entity.LedgerTransaction;
import com.aprbank.core.ledger.enums.AccountStatus;
import com.aprbank.core.ledger.enums.LedgerTransactionStatus;
import com.aprbank.core.ledger.repository.LedgerAccountRepository;
import com.aprbank.core.ledger.repository.LedgerEntryRepository;
import com.aprbank.core.ledger.repository.LedgerTransactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LedgerPostingService {

    private final LedgerTransactionRepository transactionRepository;
    private final LedgerAccountRepository accountRepository;
    private final LedgerEntryRepository entryRepository;
    private final LedgerBalanceValidator balanceValidator;

    public LedgerPostingService(
            LedgerTransactionRepository transactionRepository,
            LedgerAccountRepository accountRepository,
            LedgerEntryRepository entryRepository,
            LedgerBalanceValidator balanceValidator) {

        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.entryRepository = entryRepository;
        this.balanceValidator = balanceValidator;
    }

    @Transactional
    public LedgerTransaction postTransaction(LedgerPostingRequest request) {

        UUID transactionId = request.transactionId();

        LedgerTransaction transaction = transactionRepository
            .findByIdForUpdate(transactionId)
            .orElseThrow(() ->
                new IllegalArgumentException("Ledger transaction not found.")
            );

        if (transaction.getStatus() != LedgerTransactionStatus.PENDING) {
            throw new IllegalStateException(
                "Only PENDING transactions can be posted."
            );
        }

        if (!entryRepository.findByTransaction_Id(transactionId).isEmpty()) {
            throw new IllegalStateException(
                "Ledger entries already exist for this transaction."
            );
        }

        List<UUID> accountIds = request.entries()
            .stream()
            .map(LedgerPostingRequest.PostingLine::ledgerAccountId)
            .distinct()
            .toList();

        Map<UUID, LedgerAccount> accounts = new HashMap<>();

        for (LedgerAccount account : accountRepository.findAllById(accountIds)) {
            accounts.put(account.getId(), account);
        }

        if (accounts.size() != accountIds.size()) {
            throw new IllegalArgumentException(
                "One or more ledger accounts do not exist."
            );
        }

        List<LedgerBalanceValidator.LedgerPostingLine> validationLines =
            new ArrayList<>();

        List<LedgerEntry> entries = new ArrayList<>();

        for (LedgerPostingRequest.PostingLine line : request.entries()) {

            if (line.amount().scale() > 4 || line.amount().precision() > 20) {
                throw new IllegalArgumentException(
                    "Amount exceeds supported precision (20,4)."
                );
            }

            LedgerAccount account = accounts.get(line.ledgerAccountId());

            if (account.getStatus() != AccountStatus.ACTIVE) {
                throw new IllegalStateException(
                    "Ledger account is not ACTIVE: " + account.getAccountCode()
                );
            }

            if (!account.getCurrencyCode().equals(line.currencyCode())) {
                throw new IllegalArgumentException(
                    "Entry currency does not match ledger account currency."
                );
            }

            validationLines.add(
                new LedgerBalanceValidator.LedgerPostingLine(
                    account.getCurrencyCode(),
                    line.side(),
                    line.amount()
                )
            );

            LedgerEntry entry = LedgerEntry.create(
                transaction,
                account,
                line.side(),
                line.amount(),
                account.getCurrencyCode()
            );

            entries.add(entry);
        }

        balanceValidator.validate(validationLines);

        entryRepository.saveAll(entries);

        transaction.setStatus(LedgerTransactionStatus.POSTED);
        transaction.setPostedAt(Instant.now());

        return transactionRepository.save(transaction);
    }
}