package com.aprbank.core.ledger.controller;

import com.aprbank.core.ledger.dto.request.LedgerAccountCreateRequest;
import com.aprbank.core.ledger.dto.response.LedgerAccountResponse;
import com.aprbank.core.ledger.entity.LedgerAccount;
import com.aprbank.core.ledger.service.LedgerAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ledger/accounts")
public class LedgerAccountController {

    private final LedgerAccountService accountService;

    public LedgerAccountController(LedgerAccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<LedgerAccountResponse> createAccount(
            @Valid @RequestBody LedgerAccountCreateRequest request) {

        LedgerAccount account = accountService.createAccount(
            request.accountCode(),
            request.accountName(),
            request.accountType(),
            request.currencyCode()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(LedgerAccountResponse.from(account));
    }
}