package com.syfe.finance.service;

import com.syfe.finance.dto.*;
import com.syfe.finance.entity.Category;
import com.syfe.finance.entity.Transaction;
import com.syfe.finance.entity.TransactionType;
import com.syfe.finance.entity.User;
import com.syfe.finance.exception.ResourceNotFoundException;
import com.syfe.finance.exception.UnauthorizedAccessException;
import com.syfe.finance.exception.ValidationException;
import com.syfe.finance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private User otherUser;
    private Category category;
    private Transaction transaction;
    private TransactionRequest transactionRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("test@example.com")
                .build();

        otherUser = User.builder()
                .id(2L)
                .username("other@example.com")
                .build();

        category = Category.builder()
                .id(1L)
                .name("Salary")
                .type(TransactionType.INCOME)
                .user(null)
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.now().minusDays(1))
                .description("January Salary")
                .type(TransactionType.INCOME)
                .user(user)
                .category(category)
                .build();

        transactionRequest = TransactionRequest.builder()
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.now().minusDays(1))
                .category("Salary")
                .description("January Salary")
                .build();
    }

    @Test
    @DisplayName("Should create transaction successfully")
    void createTransaction_Success() {
        when(categoryService.findCategoryByNameForUser("Salary", user.getId())).thenReturn(category);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.createTransaction(transactionRequest, user);

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.getAmount());
        assertEquals("Salary", response.getCategory());
        assertEquals("INCOME", response.getType());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw ValidationException for future date")
    void createTransaction_FutureDate() {
        TransactionRequest futureRequest = TransactionRequest.builder()
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.now().plusDays(1))
                .category("Salary")
                .description("Future")
                .build();

        assertThrows(ValidationException.class, 
                () -> transactionService.createTransaction(futureRequest, user));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should get all transactions for user")
    void getAllTransactions_NoFilters() {
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionRepository.findAllByUserIdOrderByDateDesc(user.getId())).thenReturn(transactions);

        TransactionListResponse response = transactionService.getAllTransactions(
                user.getId(), null, null, null);

        assertNotNull(response);
        assertEquals(1, response.getTransactions().size());
    }

    @Test
    @DisplayName("Should get transactions filtered by date range")
    void getAllTransactions_WithDateFilter() {
        List<Transaction> transactions = Arrays.asList(transaction);
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        when(transactionRepository.findAllByUserIdAndDateBetweenOrderByDateDesc(
                user.getId(), startDate, endDate)).thenReturn(transactions);

        TransactionListResponse response = transactionService.getAllTransactions(
                user.getId(), startDate, endDate, null);

        assertNotNull(response);
        assertEquals(1, response.getTransactions().size());
    }

    @Test
    @DisplayName("Should get transactions filtered by category")
    void getAllTransactions_WithCategoryFilter() {
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionRepository.findAllByUserIdAndCategoryIdOrderByDateDesc(user.getId(), 1L))
                .thenReturn(transactions);

        TransactionListResponse response = transactionService.getAllTransactions(
                user.getId(), null, null, 1L);

        assertNotNull(response);
        assertEquals(1, response.getTransactions().size());
    }

    @Test
    @DisplayName("Should get transactions with all filters")
    void getAllTransactions_WithAllFilters() {
        List<Transaction> transactions = Arrays.asList(transaction);
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        when(transactionRepository.findAllByUserIdAndDateBetweenAndCategoryIdOrderByDateDesc(
                user.getId(), startDate, endDate, 1L)).thenReturn(transactions);

        TransactionListResponse response = transactionService.getAllTransactions(
                user.getId(), startDate, endDate, 1L);

        assertNotNull(response);
        assertEquals(1, response.getTransactions().size());
    }

    @Test
    @DisplayName("Should get transaction by ID")
    void getTransactionById_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        TransactionResponse response = transactionService.getTransactionById(1L, user);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for unknown transaction")
    void getTransactionById_NotFound() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> transactionService.getTransactionById(999L, user));
    }

    @Test
    @DisplayName("Should throw UnauthorizedAccessException when accessing another user's transaction")
    void getTransactionById_Unauthorized() {
        Transaction otherUserTransaction = Transaction.builder()
                .id(2L)
                .user(otherUser)
                .build();
        
        when(transactionRepository.findById(2L)).thenReturn(Optional.of(otherUserTransaction));

        assertThrows(UnauthorizedAccessException.class, 
                () -> transactionService.getTransactionById(2L, user));
    }

    @Test
    @DisplayName("Should update transaction successfully")
    void updateTransaction_Success() {
        TransactionUpdateRequest updateRequest = TransactionUpdateRequest.builder()
                .amount(new BigDecimal("6000.00"))
                .description("Updated Salary")
                .build();

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.updateTransaction(1L, updateRequest, user);

        assertNotNull(response);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should update transaction category")
    void updateTransaction_ChangeCategory() {
        Category newCategory = Category.builder()
                .id(2L)
                .name("Bonus")
                .type(TransactionType.INCOME)
                .build();

        TransactionUpdateRequest updateRequest = TransactionUpdateRequest.builder()
                .category("Bonus")
                .build();

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(categoryService.findCategoryByNameForUser("Bonus", user.getId())).thenReturn(newCategory);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.updateTransaction(1L, updateRequest, user);

        assertNotNull(response);
        verify(categoryService).findCategoryByNameForUser("Bonus", user.getId());
    }

    @Test
    @DisplayName("Should delete transaction successfully")
    void deleteTransaction_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        assertDoesNotThrow(() -> transactionService.deleteTransaction(1L, user));
        verify(transactionRepository).delete(transaction);
    }

    @Test
    @DisplayName("Should throw UnauthorizedAccessException when deleting another user's transaction")
    void deleteTransaction_Unauthorized() {
        Transaction otherUserTransaction = Transaction.builder()
                .id(2L)
                .user(otherUser)
                .build();
        
        when(transactionRepository.findById(2L)).thenReturn(Optional.of(otherUserTransaction));

        assertThrows(UnauthorizedAccessException.class, 
                () -> transactionService.deleteTransaction(2L, user));
        verify(transactionRepository, never()).delete(any(Transaction.class));
    }
}
