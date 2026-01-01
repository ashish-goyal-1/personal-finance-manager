package com.syfe.finance.controller;

import com.syfe.finance.dto.*;
import com.syfe.finance.entity.User;
import com.syfe.finance.service.AuthService;
import com.syfe.finance.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller for managing financial transactions.
 * Supports creating, retrieving, updating, and deleting transactions.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthService authService;

    /**
     * Creates a new financial transaction.
     *
     * @param request the transaction creation request
     * @return the created transaction details
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        User currentUser = authService.getCurrentUser();
        TransactionResponse response = transactionService.createTransaction(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves a list of transactions for the authenticated user, optionally
     * filtered.
     *
     * @param startDate  optional start date filter
     * @param endDate    optional end date filter
     * @param categoryId optional category ID filter
     * @return a list of transactions
     */
    @GetMapping
    public ResponseEntity<TransactionListResponse> getAllTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId) {
        User currentUser = authService.getCurrentUser();
        TransactionListResponse response = transactionService.getAllTransactions(
                currentUser.getId(), startDate, endDate, categoryId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a specific transaction by its ID.
     *
     * @param id the transaction ID
     * @return the transaction details
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        TransactionResponse response = transactionService.getTransactionById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing transaction.
     * Note: The date cannot be updated.
     *
     * @param id      the transaction ID
     * @param request the update request details
     * @return the updated transaction details
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        TransactionResponse response = transactionService.updateTransaction(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a transaction by its ID.
     *
     * @param id the transaction ID
     * @return a success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTransaction(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        transactionService.deleteTransaction(id, currentUser);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Transaction deleted successfully")
                .build());
    }
}
