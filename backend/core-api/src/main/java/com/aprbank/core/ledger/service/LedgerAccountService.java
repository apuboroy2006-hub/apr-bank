package com.aprbank.core.ledger.service;

import com.aprbank.core.ledger.entity.LedgerAccount;
import com.aprbank.core.ledger.enums.AccountStatus;
import com.aprbank.core.ledger.enums.AccountType;
import com.aprbank.core.ledger.repository.LedgerAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class LedgerAccountService {

    private static final Pattern ACCOUNT_CODE_PATTERN =
        Pattern.compile("^[A-Z0-9][A-Z0-9_-]{2,49}$");

    private static final Pattern CURRENCY_PATTERN =
        Pattern.compile("^[A-Z]{3}$");

    private final LedgerAccountRepository accountRepository;

    public LedgerAccountService(LedgerAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public LedgerAccount createAccount(
            String accountCode,
            String accountName,
            AccountType accountType,
            String currencyCode) {

        if (accountCode == null || accountName == null
                || accountType == null || currencyCode == null) {
            throw new IllegalArgumentException("Account fields are required.");
        }

        String normalizedCode = accountCode.trim().toUpperCase(Locale.ROOT);
        String normalizedName = accountName.trim();
        String normalizedCurrency = currencyCode.trim().toUpperCase(Locale.ROOT);

        if (!ACCOUNT_CODE_PATTERN.matcher(normalizedCode).matches()) {
            throw new IllegalArgumentException("Invalid account code.");
        }

        if (normalizedName.isBlank() || normalizedName.length() > 150) {
            throw new IllegalArgumentException("Invalid account name.");
        }

        if (!CURRENCY_PATTERN.matcher(normalizedCurrency).matches()) {
            throw new IllegalArgumentException("Invalid currency code.");
        }

        if (accountRepository.existsByAccountCode(normalizedCode)) {
            throw new IllegalStateException("Account code already exists.");
        }

        LedgerAccount account = LedgerAccount.create(
            normalizedCode,
            normalizedName,
            accountType,
            normalizedCurrency
        );

        return accountRepository.save(account);
    }
}