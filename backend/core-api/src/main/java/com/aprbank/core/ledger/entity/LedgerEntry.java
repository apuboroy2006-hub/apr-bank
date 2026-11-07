package com.aprbank.core.ledger.entity;

import com.aprbank.core.ledger.enums.EntrySide;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private LedgerTransaction transaction;

    @Column(name = "ledger_account_id", nullable = false)
    private UUID ledgerAccountId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
        @JoinColumn(
            name = "ledger_account_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false
        ),
        @JoinColumn(
            name = "currency_code",
            referencedColumnName = "currency_code",
            insertable = false,
            updatable = false
        )
    })
    private LedgerAccount ledgerAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_side", nullable = false, length = 10)
    private EntrySide entrySide;

    @Column(name = "amount", nullable = false, precision = 20, scale = 4)
    private BigDecimal amount;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
    @Column(
        name = "currency_code",
        nullable = false,
        length = 3,
        columnDefinition = "char(3)"
    )
    private String currencyCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LedgerEntry() {
    }

    public static LedgerEntry create(
            LedgerTransaction transaction,
            LedgerAccount account,
            EntrySide side,
            BigDecimal amount,
            String currencyCode) {

        LedgerEntry entry = new LedgerEntry();
        entry.setTransaction(transaction);
        entry.setLedgerAccount(account);
        entry.setEntrySide(side);
        entry.setAmount(amount);
        entry.setCurrencyCode(currencyCode);

        return entry;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public LedgerTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(LedgerTransaction transaction) {
        this.transaction = transaction;
    }

    public LedgerAccount getLedgerAccount() {
        return ledgerAccount;
    }

    public void setLedgerAccount(LedgerAccount ledgerAccount) {
        this.ledgerAccount = ledgerAccount;
        this.ledgerAccountId = ledgerAccount != null
                ? ledgerAccount.getId()
                : null;
    }

    public EntrySide getEntrySide() {
        return entrySide;
    }

    public void setEntrySide(EntrySide entrySide) {
        this.entrySide = entrySide;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getCurrencyCodeFromAccount() {
        return ledgerAccount != null ? ledgerAccount.getCurrencyCode() : null;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}