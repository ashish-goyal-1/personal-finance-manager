package com.syfe.finance.service;

import com.syfe.finance.dto.*;
import com.syfe.finance.entity.Category;
import com.syfe.finance.entity.Transaction;
import com.syfe.finance.entity.User;
import com.syfe.finance.exception.ResourceNotFoundException;
import com.syfe.finance.exception.UnauthorizedAccessException;
import com.syfe.finance.exception.ValidationException;
import com.syfe.finance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing financial transactions.
 * Handles business logic for creating, retrieving, updating, and deleting
 * transactions.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    /**
     * Creates a new transaction after validating date and category.
     *
     * @param request the transaction request
     * @param user    the authenticated user
     * @return the created transaction response
     */
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, User user) {
        // Validate date is not in the future
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Transaction date cannot be in the future");
        }

        // Find category
        Category category = categoryService.findCategoryByNameForUser(request.getCategory(), user.getId());

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .description(request.getDescription())
                .type(category.getType())
                .user(user)
                .category(category)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return toTransactionResponse(savedTransaction);
    }

    /**
     * Retrieves transactions with applied filters (date range, category).
     *
     * @param userId     the user ID
     * @param startDate  filter start date
     * @param endDate    filter end date
     * @param categoryId filter category ID
     * @return a list response of transactions
     */
    public TransactionListResponse getAllTransactions(Long userId, LocalDate startDate, LocalDate endDate,
            Long categoryId) {
        List<Transaction> transactions;

        if (startDate != null && endDate != null && categoryId != null) {
            transactions = transactionRepository.findAllByUserIdAndDateBetweenAndCategoryIdOrderByDateDesc(
                    userId, startDate, endDate, categoryId);
        } else if (startDate != null && endDate != null) {
            transactions = transactionRepository.findAllByUserIdAndDateBetweenOrderByDateDesc(
                    userId, startDate, endDate);
        } else if (categoryId != null) {
            transactions = transactionRepository.findAllByUserIdAndCategoryIdOrderByDateDesc(userId, categoryId);
        } else {
            transactions = transactionRepository.findAllByUserIdOrderByDateDesc(userId);
        }

        List<TransactionResponse> responses = transactions.stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());

        return TransactionListResponse.builder()
                .transactions(responses)
                .build();
    }

    /**
     * Retrieves a single transaction by ID, ensuring ownership.
     *
     * @param transactionId the transaction ID
     * @param user          the authenticated user
     * @return the transaction response
     */
    public TransactionResponse getTransactionById(Long transactionId, User user) {
        Transaction transaction = findTransactionWithOwnershipCheck(transactionId, user);
        return toTransactionResponse(transaction);
    }

    /**
     * Updates an existing transaction.
     * Date cannot be updated.
     *
     * @param transactionId the transaction ID
     * @param request       the update request
     * @param user          the authenticated user
     * @return the updated transaction response
     */
    @Transactional
    public TransactionResponse updateTransaction(Long transactionId, TransactionUpdateRequest request, User user) {
        Transaction transaction = findTransactionWithOwnershipCheck(transactionId, user);

        // Update amount if provided
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }

        // Update description if provided
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        // Update category if provided
        if (request.getCategory() != null) {
            Category newCategory = categoryService.findCategoryByNameForUser(request.getCategory(), user.getId());
            transaction.setCategory(newCategory);
            transaction.setType(newCategory.getType());
        }

        // Note: Date cannot be updated per specification

        Transaction updatedTransaction = transactionRepository.save(transaction);
        return toTransactionResponse(updatedTransaction);
    }

    /**
     * Deletes a transaction by ID.
     *
     * @param transactionId the transaction ID
     * @param user          the authenticated user
     */
    @Transactional
    public void deleteTransaction(Long transactionId, User user) {
        Transaction transaction = findTransactionWithOwnershipCheck(transactionId, user);
        transactionRepository.delete(transaction);
    }

    private Transaction findTransactionWithOwnershipCheck(Long transactionId, User user) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", transactionId));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Transaction", transactionId);
        }

        return transaction;
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .date(transaction.getDate())
                .category(transaction.getCategory().getName())
                .description(transaction.getDescription())
                .type(transaction.getType().name())
                .build();
    }
}
