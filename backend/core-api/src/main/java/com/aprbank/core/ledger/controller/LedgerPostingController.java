package com.aprbank.core.ledger.controller;

import com.aprbank.core.ledger.dto.request.LedgerPostingRequest;
import com.aprbank.core.ledger.dto.response.LedgerTransactionResponse;
import com.aprbank.core.ledger.entity.LedgerTransaction;
import com.aprbank.core.ledger.service.LedgerPostingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledger/transactions")
public class LedgerPostingController {

    private final LedgerPostingService postingService;

    public LedgerPostingController(LedgerPostingService postingService) {
        this.postingService = postingService;
    }

    @PostMapping("/{transactionId}/post")
    public ResponseEntity<LedgerTransactionResponse> postTransaction(
            @PathVariable UUID transactionId,
            @Valid @RequestBody LedgerPostingRequest request) {

        if (!transactionId.equals(request.transactionId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "URL transaction ID does not match request transaction ID."
            );
        }

        LedgerTransaction transaction =
                postingService.postTransaction(request);

        return ResponseEntity.ok(
                LedgerTransactionResponse.from(transaction)
        );
    }
}