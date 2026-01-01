package com.syfe.finance.service;

import com.syfe.finance.dto.*;
import com.syfe.finance.entity.SavingsGoal;
import com.syfe.finance.entity.Transaction;
import com.syfe.finance.entity.TransactionType;
import com.syfe.finance.entity.User;
import com.syfe.finance.exception.ResourceNotFoundException;
import com.syfe.finance.exception.UnauthorizedAccessException;
import com.syfe.finance.exception.ValidationException;
import com.syfe.finance.repository.SavingsGoalRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsGoalServiceTest {

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private SavingsGoalService savingsGoalService;

    private User user;
    private User otherUser;
    private SavingsGoal goal;
    private GoalRequest goalRequest;

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

        goal = SavingsGoal.builder()
                .id(1L)
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("10000.00"))
                .targetDate(LocalDate.now().plusMonths(6))
                .startDate(LocalDate.now().minusMonths(1))
                .user(user)
                .build();

        goalRequest = GoalRequest.builder()
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("10000.00"))
                .targetDate(LocalDate.now().plusMonths(6))
                .startDate(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Should create goal successfully")
    void createGoal_Success() {
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(goal);
        when(transactionRepository.findByUserIdAndDateAfterAndType(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        GoalResponse response = savingsGoalService.createGoal(goalRequest, user);

        assertNotNull(response);
        assertEquals("Emergency Fund", response.getGoalName());
        assertEquals(new BigDecimal("10000.00"), response.getTargetAmount());
        verify(savingsGoalRepository).save(any(SavingsGoal.class));
    }

    @Test
    @DisplayName("Should throw ValidationException for past target date")
    void createGoal_PastTargetDate() {
        GoalRequest pastRequest = GoalRequest.builder()
                .goalName("Past Goal")
                .targetAmount(new BigDecimal("5000.00"))
                .targetDate(LocalDate.now().minusDays(1))
                .build();

        assertThrows(ValidationException.class, 
                () -> savingsGoalService.createGoal(pastRequest, user));
        verify(savingsGoalRepository, never()).save(any(SavingsGoal.class));
    }

    @Test
    @DisplayName("Should get all goals for user")
    void getAllGoals_Success() {
        List<SavingsGoal> goals = Arrays.asList(goal);
        when(savingsGoalRepository.findAllByUserId(user.getId())).thenReturn(goals);
        when(transactionRepository.findByUserIdAndDateAfterAndType(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        GoalListResponse response = savingsGoalService.getAllGoals(user);

        assertNotNull(response);
        assertEquals(1, response.getGoals().size());
    }

    @Test
    @DisplayName("Should calculate progress correctly with income and expenses")
    void getGoalById_CalculatesProgressCorrectly() {
        List<Transaction> incomeTransactions = Arrays.asList(
                Transaction.builder().amount(new BigDecimal("5000.00")).type(TransactionType.INCOME).build(),
                Transaction.builder().amount(new BigDecimal("3000.00")).type(TransactionType.INCOME).build()
        );
        List<Transaction> expenseTransactions = Arrays.asList(
                Transaction.builder().amount(new BigDecimal("2000.00")).type(TransactionType.EXPENSE).build()
        );

        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(transactionRepository.findByUserIdAndDateAfterAndType(user.getId(), goal.getStartDate(), TransactionType.INCOME))
                .thenReturn(incomeTransactions);
        when(transactionRepository.findByUserIdAndDateAfterAndType(user.getId(), goal.getStartDate(), TransactionType.EXPENSE))
                .thenReturn(expenseTransactions);

        GoalResponse response = savingsGoalService.getGoalById(1L, user);

        assertNotNull(response);
        // Income: 5000 + 3000 = 8000, Expenses: 2000, Net: 6000
        assertEquals(new BigDecimal("6000.00"), response.getCurrentProgress());
        assertEquals(new BigDecimal("4000.00"), response.getRemainingAmount());
        assertEquals(60.0, response.getProgressPercentage());
    }

    @Test
    @DisplayName("Should cap progress percentage at 100%")
    void getGoalById_ProgressCappedAt100() {
        List<Transaction> incomeTransactions = Arrays.asList(
                Transaction.builder().amount(new BigDecimal("15000.00")).type(TransactionType.INCOME).build()
        );

        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(transactionRepository.findByUserIdAndDateAfterAndType(user.getId(), goal.getStartDate(), TransactionType.INCOME))
                .thenReturn(incomeTransactions);
        when(transactionRepository.findByUserIdAndDateAfterAndType(user.getId(), goal.getStartDate(), TransactionType.EXPENSE))
                .thenReturn(Collections.emptyList());

        GoalResponse response = savingsGoalService.getGoalById(1L, user);

        assertNotNull(response);
        assertEquals(100.0, response.getProgressPercentage());
        assertEquals(BigDecimal.ZERO, response.getRemainingAmount());
    }

    @Test
    @DisplayName("Should not show negative progress")
    void getGoalById_NoNegativeProgress() {
        List<Transaction> expenseTransactions = Arrays.asList(
                Transaction.builder().amount(new BigDecimal("5000.00")).type(TransactionType.EXPENSE).build()
        );

        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(transactionRepository.findByUserIdAndDateAfterAndType(user.getId(), goal.getStartDate(), TransactionType.INCOME))
                .thenReturn(Collections.emptyList());
        when(transactionRepository.findByUserIdAndDateAfterAndType(user.getId(), goal.getStartDate(), TransactionType.EXPENSE))
                .thenReturn(expenseTransactions);

        GoalResponse response = savingsGoalService.getGoalById(1L, user);

        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getCurrentProgress());
        assertEquals(0.0, response.getProgressPercentage());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for unknown goal")
    void getGoalById_NotFound() {
        when(savingsGoalRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> savingsGoalService.getGoalById(999L, user));
    }

    @Test
    @DisplayName("Should throw UnauthorizedAccessException when accessing another user's goal")
    void getGoalById_Unauthorized() {
        SavingsGoal otherUserGoal = SavingsGoal.builder()
                .id(2L)
                .user(otherUser)
                .build();
        
        when(savingsGoalRepository.findById(2L)).thenReturn(Optional.of(otherUserGoal));

        assertThrows(UnauthorizedAccessException.class, 
                () -> savingsGoalService.getGoalById(2L, user));
    }

    @Test
    @DisplayName("Should update goal successfully")
    void updateGoal_Success() {
        GoalUpdateRequest updateRequest = GoalUpdateRequest.builder()
                .targetAmount(new BigDecimal("15000.00"))
                .targetDate(LocalDate.now().plusMonths(12))
                .build();

        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(goal);
        when(transactionRepository.findByUserIdAndDateAfterAndType(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        GoalResponse response = savingsGoalService.updateGoal(1L, updateRequest, user);

        assertNotNull(response);
        verify(savingsGoalRepository).save(any(SavingsGoal.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when updating with past target date")
    void updateGoal_PastTargetDate() {
        GoalUpdateRequest updateRequest = GoalUpdateRequest.builder()
                .targetDate(LocalDate.now().minusDays(1))
                .build();

        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(ValidationException.class, 
                () -> savingsGoalService.updateGoal(1L, updateRequest, user));
    }

    @Test
    @DisplayName("Should delete goal successfully")
    void deleteGoal_Success() {
        when(savingsGoalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertDoesNotThrow(() -> savingsGoalService.deleteGoal(1L, user));
        verify(savingsGoalRepository).delete(goal);
    }

    @Test
    @DisplayName("Should throw UnauthorizedAccessException when deleting another user's goal")
    void deleteGoal_Unauthorized() {
        SavingsGoal otherUserGoal = SavingsGoal.builder()
                .id(2L)
                .user(otherUser)
                .build();
        
        when(savingsGoalRepository.findById(2L)).thenReturn(Optional.of(otherUserGoal));

        assertThrows(UnauthorizedAccessException.class, 
                () -> savingsGoalService.deleteGoal(2L, user));
        verify(savingsGoalRepository, never()).delete(any(SavingsGoal.class));
    }
}
