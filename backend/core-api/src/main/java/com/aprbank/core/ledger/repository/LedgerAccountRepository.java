package com.aprbank.core.ledger.repository;

import com.aprbank.core.ledger.entity.LedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LedgerAccountRepository
        extends JpaRepository<LedgerAccount, UUID> {

    Optional<LedgerAccount> findByAccountCode(String accountCode);

    List<LedgerAccount> findByCurrencyCode(String currencyCode);

    boolean existsByAccountCode(String accountCode);
}