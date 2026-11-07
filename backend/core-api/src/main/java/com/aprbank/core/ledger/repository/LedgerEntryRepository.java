package com.aprbank.core.ledger.repository;

import com.aprbank.core.ledger.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository
        extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByTransaction_Id(UUID transactionId);

    List<LedgerEntry> findByLedgerAccount_Id(UUID accountId);

    @Query("""
        SELECT COALESCE(SUM(
            CASE
                WHEN e.entrySide = com.aprbank.core.ledger.enums.EntrySide.DEBIT
                THEN e.amount
                ELSE -e.amount
            END
        ), 0)
        FROM LedgerEntry e
        WHERE e.ledgerAccount.id = :accountId
        """)
    BigDecimal calculateNetDebitBalance(@Param("accountId") UUID accountId);
}