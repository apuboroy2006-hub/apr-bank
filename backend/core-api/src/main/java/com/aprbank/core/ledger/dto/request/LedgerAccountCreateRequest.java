package com.aprbank.core.ledger.dto.request;

import com.aprbank.core.ledger.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LedgerAccountCreateRequest(

    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9_-]*$")
    String accountCode,

    @NotBlank
    @Size(max = 150)
    String accountName,

    @NotNull
    AccountType accountType,

    @NotBlank
    @Pattern(regexp = "^[A-Za-z]{3}$")
    String currencyCode

) {}