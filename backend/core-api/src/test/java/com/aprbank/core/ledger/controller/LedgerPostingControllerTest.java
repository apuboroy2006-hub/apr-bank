package com.aprbank.core.ledger.controller;

import com.aprbank.core.ledger.dto.request.LedgerPostingRequest;
import com.aprbank.core.ledger.entity.LedgerTransaction;
import com.aprbank.core.ledger.enums.LedgerTransactionStatus;
import com.aprbank.core.ledger.service.LedgerPostingService;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LedgerPostingController.class)
@WithMockUser
@MockitoSettings(strictness = Strictness.LENIENT)
class LedgerPostingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LedgerPostingService postingService;

    @Test
    void shouldPostTransactionSuccessfully() throws Exception {

        UUID transactionId = UUID.randomUUID();

        LedgerTransaction transaction = LedgerTransaction.create(
                "APR-TEST123456789",
                "TRANSFER",
                "Test transfer",
                "test-idempotency-123"
        );

        transaction.setStatus(LedgerTransactionStatus.POSTED);

        when(postingService.postTransaction(any(LedgerPostingRequest.class)))
                .thenReturn(transaction);

        String requestBody = """
                {
                  "transactionId": "%s",
                  "entries": [
                    {
                      "ledgerAccountId": "%s",
                      "side": "DEBIT",
                      "amount": 100.00,
                      "currencyCode": "USD"
                    },
                    {
                      "ledgerAccountId": "%s",
                      "side": "CREDIT",
                      "amount": 100.00,
                      "currencyCode": "USD"
                    }
                  ]
                }
                """.formatted(
                transactionId,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        mockMvc.perform(post(
                        "/api/v1/ledger/transactions/{transactionId}/post",
                        transactionId
                )
                .with(csrf())
                .contentType("application/json")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POSTED"))
                .andExpect(jsonPath("$.referenceNumber")
                        .value("APR-TEST123456789"));

        verify(postingService, times(1))
                .postTransaction(any(LedgerPostingRequest.class));
    }

    @Test
    void shouldRejectMismatchedTransactionIds() throws Exception {

        UUID urlTransactionId = UUID.randomUUID();
        UUID bodyTransactionId = UUID.randomUUID();

        String requestBody = """
                {
                  "transactionId": "%s",
                  "entries": []
                }
                """.formatted(bodyTransactionId);

        mockMvc.perform(post(
                        "/api/v1/ledger/transactions/{transactionId}/post",
                        urlTransactionId
                )
                .with(csrf())
                .contentType("application/json")
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(postingService);
    }

    @Test
    void shouldRejectInvalidPostingRequest() throws Exception {

        UUID transactionId = UUID.randomUUID();

        String requestBody = """
                {
                  "transactionId": "%s",
                  "entries": [
                    {
                      "ledgerAccountId": null,
                      "side": "DEBIT",
                      "amount": 0,
                      "currencyCode": "usd"
                    }
                  ]
                }
                """.formatted(transactionId);

        mockMvc.perform(post(
                        "/api/v1/ledger/transactions/{transactionId}/post",
                        transactionId
                )
                .with(csrf())
                .contentType("application/json")
                .content(requestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(postingService);
    }

    @Test
    void shouldRejectMalformedTransactionId() throws Exception {

        mockMvc.perform(post(
                        "/api/v1/ledger/transactions/{transactionId}/post",
                        "not-a-valid-uuid"
                )
                .with(csrf())
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(postingService);
    }
}