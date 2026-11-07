package com.aprbank.core.ledger.controller;

import com.aprbank.core.ledger.dto.request.LedgerAccountCreateRequest;
import com.aprbank.core.ledger.entity.LedgerAccount;
import com.aprbank.core.ledger.enums.AccountStatus;
import com.aprbank.core.ledger.enums.AccountType;
import com.aprbank.core.ledger.service.LedgerAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LedgerAccountController.class)
@WithMockUser
class LedgerAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LedgerAccountService accountService;

    @Test
    void shouldCreateAccountSuccessfully() throws Exception {
        LedgerAccount account = mock(LedgerAccount.class);

        when(account.getId()).thenReturn(UUID.randomUUID());
        when(account.getAccountCode()).thenReturn("CASH-001");
        when(account.getAccountName()).thenReturn("Cash Account");
        when(account.getAccountType()).thenReturn(AccountType.ASSET);
        when(account.getCurrencyCode()).thenReturn("USD");
        when(account.getStatus()).thenReturn(AccountStatus.ACTIVE);
        when(account.getCreatedAt()).thenReturn(Instant.now());

        when(accountService.createAccount(
            anyString(), anyString(), any(AccountType.class), anyString()
        )).thenReturn(account);

        mockMvc.perform(post("/api/v1/ledger/accounts").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "accountCode": "CASH-001",
                      "accountName": "Cash Account",
                      "accountType": "ASSET",
                      "currencyCode": "USD"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accountCode").value("CASH-001"))
            .andExpect(jsonPath("$.accountType").value("ASSET"))
            .andExpect(jsonPath("$.currencyCode").value("USD"));

        verify(accountService).createAccount(
            "CASH-001", "Cash Account", AccountType.ASSET, "USD"
        );
    }

    @Test
    void shouldRejectInvalidAccountRequest() throws Exception {
        mockMvc.perform(post("/api/v1/ledger/accounts").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "accountCode": "",
                      "accountName": "",
                      "accountType": null,
                      "currencyCode": "INVALID"
                    }
                    """))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(accountService);
    }
}