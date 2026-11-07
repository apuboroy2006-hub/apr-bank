package com.aprbank.core.ledger.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LedgerTransactionCreateRequest(

        @NotBlank
        @Size(min = 2, max = 40)
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$")
        String transactionType,

        @Size(max = 500)
        String description,

        @NotBlank
        @Size(min = 8, max = 128)
        String idempotencyKey

) {
}