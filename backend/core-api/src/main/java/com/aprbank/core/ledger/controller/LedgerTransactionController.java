package com.aprbank.core.ledger.controller;

import com.aprbank.core.ledger.dto.request.LedgerTransactionCreateRequest;
import com.aprbank.core.ledger.dto.response.LedgerTransactionResponse;
import com.aprbank.core.ledger.entity.LedgerTransaction;
import com.aprbank.core.ledger.service.LedgerTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ledger/transactions")
public class LedgerTransactionController {

    private final LedgerTransactionService transactionService;

    public LedgerTransactionController(
            LedgerTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<LedgerTransactionResponse> createTransaction(
            @Valid @RequestBody LedgerTransactionCreateRequest request) {

        LedgerTransaction transaction =
                transactionService.createTransaction(
                        request.transactionType(),
                        request.description(),
                        request.idempotencyKey()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(LedgerTransactionResponse.from(transaction));
    }
}