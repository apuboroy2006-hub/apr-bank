package com.aprbank.core.ledger.dto.request;

import com.aprbank.core.ledger.enums.EntrySide;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record LedgerPostingRequest(

    @NotNull
    UUID transactionId,

    @NotNull
    @Size(min = 2, max = 100)
    List<@Valid PostingLine> entries

) {

    public record PostingLine(

        @NotNull
        UUID ledgerAccountId,

        @NotNull
        EntrySide side,

        @NotNull
        @DecimalMin(value = "0.0001")
        BigDecimal amount,

        @NotBlank
        @Pattern(regexp = "^[A-Z]{3}$")
        String currencyCode

    ) {
    }
}