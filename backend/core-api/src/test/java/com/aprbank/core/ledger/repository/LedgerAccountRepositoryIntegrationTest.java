package com.aprbank.core.ledger.repository;

import com.aprbank.core.ledger.entity.LedgerAccount;
import com.aprbank.core.ledger.enums.AccountStatus;
import com.aprbank.core.ledger.enums.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Testcontainers
@Transactional
class LedgerAccountRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("aprbank_test")
                    .withUsername("aprbank_test_user")
                    .withPassword("aprbank_test_password");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name",
                POSTGRES::getDriverClassName);
    }

    @Autowired
    private LedgerAccountRepository accountRepository;

    @Test
    void shouldPersistAndRetrieveAccountFromPostgreSQL() {
        String accountCode = "IT-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();

        LedgerAccount account = LedgerAccount.create(
                accountCode,
                "Integration Test Account",
                AccountType.ASSET,
                "USD"
        );

        LedgerAccount saved = accountRepository.saveAndFlush(account);

        assertNotNull(saved.getId());
        assertEquals(accountCode, saved.getAccountCode());
        assertEquals(AccountType.ASSET, saved.getAccountType());
        assertEquals(AccountStatus.ACTIVE, saved.getStatus());
        assertNotNull(saved.getCreatedAt());

        LedgerAccount retrieved = accountRepository
                .findByAccountCode(accountCode)
                .orElseThrow();

        assertEquals(saved.getId(), retrieved.getId());
        assertEquals("Integration Test Account",
                retrieved.getAccountName());
        assertEquals("USD", retrieved.getCurrencyCode().trim());
    }

    @Test
    void shouldCheckAccountCodeExistenceInPostgreSQL() {
        String accountCode = "IT-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();

        assertFalse(accountRepository.existsByAccountCode(accountCode));

        accountRepository.saveAndFlush(
                LedgerAccount.create(
                        accountCode,
                        "Existence Test Account",
                        AccountType.ASSET,
                        "USD"
                )
        );

        assertTrue(accountRepository.existsByAccountCode(accountCode));
    }

    @Test
    void shouldRejectDuplicateAccountCodeAtDatabaseLevel() {
        String accountCode = "IT-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();

        accountRepository.saveAndFlush(
                LedgerAccount.create(
                        accountCode,
                        "Original Account",
                        AccountType.ASSET,
                        "USD"
                )
        );

        LedgerAccount duplicate = LedgerAccount.create(
                accountCode,
                "Duplicate Account",
                AccountType.LIABILITY,
                "USD"
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> accountRepository.saveAndFlush(duplicate)
        );
    }

    @Test
    void shouldLoadAccountsWithPessimisticWriteLock() {
        LedgerAccount first = accountRepository.saveAndFlush(
                LedgerAccount.create(
                        "LOCK-" + UUID.randomUUID().toString().substring(0, 8),
                        "Lock Test Account One",
                        AccountType.ASSET,
                        "INR"
                )
        );

        LedgerAccount second = accountRepository.saveAndFlush(
                LedgerAccount.create(
                        "LOCK-" + UUID.randomUUID().toString().substring(0, 8),
                        "Lock Test Account Two",
                        AccountType.LIABILITY,
                        "INR"
                )
        );

        List<UUID> accountIds = List.of(first.getId(), second.getId());

        List<LedgerAccount> lockedAccounts =
                accountRepository.findAllByIdInForUpdate(accountIds);

        assertEquals(2, lockedAccounts.size());
        List<UUID> reversedAccountIds = List.of(second.getId(), first.getId());

        List<LedgerAccount> lockedAccountsFromReversedInput =
                accountRepository.findAllByIdInForUpdate(reversedAccountIds);

        assertEquals(
                lockedAccounts.stream().map(LedgerAccount::getId).toList(),
                lockedAccountsFromReversedInput.stream()
                        .map(LedgerAccount::getId)
                        .toList()
        );
    }}