package com.syfe.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syfe.finance.dto.*;
import com.syfe.finance.entity.User;
import com.syfe.finance.service.AuthService;
import com.syfe.finance.service.CustomUserDetailsService;
import com.syfe.finance.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private AuthService authService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private AuthenticationManager authenticationManager;

    private User user;
    private TransactionRequest transactionRequest;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("test@example.com")
                .build();

        transactionRequest = TransactionRequest.builder()
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.now().minusDays(1))
                .category("Salary")
                .description("January salary")
                .build();

        transactionResponse = TransactionResponse.builder()
                .id(1L)
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.now().minusDays(1))
                .category("Salary")
                .description("January salary")
                .type("INCOME")
                .build();
    }

    @Test
    @DisplayName("POST /api/transactions - Success returns 201")
    void createTransaction_Success() throws Exception {
        when(authService.getCurrentUser()).thenReturn(user);
        when(transactionService.createTransaction(any(TransactionRequest.class), any(User.class)))
                .thenReturn(transactionResponse);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.category").value("Salary"));
    }

    @Test
    @DisplayName("GET /api/transactions - Returns list")
    void getAllTransactions_Success() throws Exception {
        TransactionListResponse listResponse = TransactionListResponse.builder()
                .transactions(Arrays.asList(transactionResponse))
                .build();

        when(authService.getCurrentUser()).thenReturn(user);
        when(transactionService.getAllTransactions(eq(1L), any(), any(), any()))
                .thenReturn(listResponse);

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/transactions/{id} - Returns single transaction")
    void getTransactionById_Success() throws Exception {
        when(authService.getCurrentUser()).thenReturn(user);
        when(transactionService.getTransactionById(1L, user)).thenReturn(transactionResponse);

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/transactions/{id} - Update success")
    void updateTransaction_Success() throws Exception {
        TransactionUpdateRequest updateRequest = TransactionUpdateRequest.builder()
                .amount(new BigDecimal("6000.00"))
                .build();

        when(authService.getCurrentUser()).thenReturn(user);
        when(transactionService.updateTransaction(eq(1L), any(TransactionUpdateRequest.class), any(User.class)))
                .thenReturn(transactionResponse);

        mockMvc.perform(put("/api/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/transactions/{id} - Success")
    void deleteTransaction_Success() throws Exception {
        when(authService.getCurrentUser()).thenReturn(user);
        doNothing().when(transactionService).deleteTransaction(1L, user);

        mockMvc.perform(delete("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));
    }
}
