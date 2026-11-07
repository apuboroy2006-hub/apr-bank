package com.aprbank.core.ledger.controller;

import com.aprbank.core.ledger.entity.LedgerTransaction;
import com.aprbank.core.ledger.enums.LedgerTransactionStatus;
import com.aprbank.core.ledger.service.LedgerTransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LedgerTransactionController.class)
class LedgerTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LedgerTransactionService transactionService;

    @Test
    void shouldCreateTransactionSuccessfully() throws Exception {

        LedgerTransaction transaction = LedgerTransaction.create(
                "APR-TEST-001",
                "CASH_DEPOSIT",
                "Test deposit",
                "IDEMPOTENCY-001"
        );

        transaction.setStatus(LedgerTransactionStatus.PENDING);

        when(transactionService.createTransaction(
                "CASH_DEPOSIT",
                "Test deposit",
                "IDEMPOTENCY-001"
        )).thenReturn(transaction);

        mockMvc.perform(post("/api/v1/ledger/transactions")
                        .with(user("test-user"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transactionType": "CASH_DEPOSIT",
                                  "description": "Test deposit",
                                  "idempotencyKey": "IDEMPOTENCY-001"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.referenceNumber")
                        .value("APR-TEST-001"))
                .andExpect(jsonPath("$.transactionType")
                        .value("CASH_DEPOSIT"))
                .andExpect(jsonPath("$.status")
                        .value("PENDING"));

        verify(transactionService).createTransaction(
                "CASH_DEPOSIT",
                "Test deposit",
                "IDEMPOTENCY-001"
        );
    }

    @Test
    void shouldRejectInvalidTransactionRequest() throws Exception {

        mockMvc.perform(post("/api/v1/ledger/transactions")
                        .with(user("test-user"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transactionType": "",
                                  "description": "Invalid request",
                                  "idempotencyKey": " "
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }
}