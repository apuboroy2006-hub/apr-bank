package com.aprbank.core.ledger.repository;

import com.aprbank.core.ledger.entity.LedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LedgerAccountRepository
        extends JpaRepository<LedgerAccount, UUID> {

    Optional<LedgerAccount> findByAccountCode(String accountCode);

    List<LedgerAccount> findByCurrencyCode(String currencyCode);

    boolean existsByAccountCode(String accountCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM LedgerAccount a WHERE a.id IN :accountIds ORDER BY a.id")
    List<LedgerAccount> findAllByIdInForUpdate(
        @Param("accountIds") List<UUID> accountIds
    );
}