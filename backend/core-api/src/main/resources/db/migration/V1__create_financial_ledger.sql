-- APR BANK
-- V1: Initial Financial Ledger Schema
-- PostgreSQL 17 / Flyway

CREATE TABLE ledger_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_code VARCHAR(50) NOT NULL UNIQUE,
    account_name VARCHAR(150) NOT NULL,
    account_type VARCHAR(30) NOT NULL
        CHECK (account_type IN (
            'ASSET',
            'LIABILITY',
            'EQUITY',
            'INCOME',
            'EXPENSE'
        )),
    currency_code CHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_ledger_account_currency
        UNIQUE (id, currency_code)
);

CREATE TABLE ledger_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_number VARCHAR(64) NOT NULL UNIQUE,
    transaction_type VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN (
            'PENDING',
            'POSTED',
            'REVERSED',
            'FAILED'
        )),
    description VARCHAR(500),
    idempotency_key VARCHAR(128) UNIQUE,
    initiated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    posted_at TIMESTAMPTZ
);

CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL
        REFERENCES ledger_transactions(id),
    ledger_account_id UUID NOT NULL,
    entry_side VARCHAR(10) NOT NULL
        CHECK (entry_side IN ('DEBIT', 'CREDIT')),
    amount NUMERIC(20,4) NOT NULL
        CHECK (amount > 0),
    currency_code CHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ledger_entry_account_currency
        FOREIGN KEY (ledger_account_id, currency_code)
        REFERENCES ledger_accounts(id, currency_code)
);

CREATE INDEX idx_ledger_transactions_status
    ON ledger_transactions(status);

CREATE INDEX idx_ledger_transactions_initiated_at
    ON ledger_transactions(initiated_at);

CREATE INDEX idx_ledger_entries_transaction
    ON ledger_entries(transaction_id);

CREATE INDEX idx_ledger_entries_account
    ON ledger_entries(ledger_account_id);

CREATE INDEX idx_ledger_entries_created_at
    ON ledger_entries(created_at);