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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing savings goals.
 * Calculates progress based on income and expenses.
 */
@Service
@RequiredArgsConstructor
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Creates a new savings goal.
     *
     * @param request the goal request
     * @param user    the authenticated user
     * @return the created goal response
     */
    @Transactional
    public GoalResponse createGoal(GoalRequest request, User user) {
        // Validate target date is in the future
        if (!request.getTargetDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Target date must be in the future");
        }

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();

        SavingsGoal goal = SavingsGoal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .startDate(startDate)
                .user(user)
                .build();

        SavingsGoal savedGoal = savingsGoalRepository.save(goal);
        return toGoalResponse(savedGoal, user.getId());
    }

    /**
     * Retrieves all goals for a user with calculated progress.
     *
     * @param user the authenticated user
     * @return a list response of goals
     */
    public GoalListResponse getAllGoals(User user) {
        List<SavingsGoal> goals = savingsGoalRepository.findAllByUserId(user.getId());

        List<GoalResponse> goalResponses = goals.stream()
                .map(goal -> toGoalResponse(goal, user.getId()))
                .collect(Collectors.toList());

        return GoalListResponse.builder()
                .goals(goalResponses)
                .build();
    }

    /**
     * Retrieves a single goal by ID with calculated progress.
     *
     * @param goalId the goal ID
     * @param user   the authenticated user
     * @return the goal response
     */
    public GoalResponse getGoalById(Long goalId, User user) {
        SavingsGoal goal = findGoalWithOwnershipCheck(goalId, user);
        return toGoalResponse(goal, user.getId());
    }

    /**
     * Updates an existing goal.
     *
     * @param goalId  the goal ID
     * @param request the update request
     * @param user    the authenticated user
     * @return the updated goal response
     */
    @Transactional
    public GoalResponse updateGoal(Long goalId, GoalUpdateRequest request, User user) {
        SavingsGoal goal = findGoalWithOwnershipCheck(goalId, user);

        // Update target amount if provided
        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }

        // Update target date if provided
        if (request.getTargetDate() != null) {
            if (!request.getTargetDate().isAfter(LocalDate.now())) {
                throw new ValidationException("Target date must be in the future");
            }
            goal.setTargetDate(request.getTargetDate());
        }

        SavingsGoal updatedGoal = savingsGoalRepository.save(goal);
        return toGoalResponse(updatedGoal, user.getId());
    }

    /**
     * Deletes a savings goal.
     *
     * @param goalId the goal ID
     * @param user   the authenticated user
     */
    @Transactional
    public void deleteGoal(Long goalId, User user) {
        SavingsGoal goal = findGoalWithOwnershipCheck(goalId, user);
        savingsGoalRepository.delete(goal);
    }

    private SavingsGoal findGoalWithOwnershipCheck(Long goalId, User user) {
        SavingsGoal goal = savingsGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", goalId));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Goal", goalId);
        }

        return goal;
    }

    /**
     * Calculate net savings (total income - total expenses) since the goal's start
     * date.
     */
    private BigDecimal calculateNetSavings(Long userId, LocalDate startDate) {
        // Get all income transactions since start date
        List<Transaction> incomeTransactions = transactionRepository
                .findByUserIdAndDateAfterAndType(userId, startDate, TransactionType.INCOME);

        BigDecimal totalIncome = incomeTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get all expense transactions since start date
        List<Transaction> expenseTransactions = transactionRepository
                .findByUserIdAndDateAfterAndType(userId, startDate, TransactionType.EXPENSE);

        BigDecimal totalExpenses = expenseTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalIncome.subtract(totalExpenses);
    }

    private GoalResponse toGoalResponse(SavingsGoal goal, Long userId) {
        BigDecimal netSavings = calculateNetSavings(userId, goal.getStartDate());

        // Don't show negative progress
        BigDecimal currentProgress = netSavings.max(BigDecimal.ZERO);

        // Calculate remaining amount
        BigDecimal remaining = goal.getTargetAmount().subtract(currentProgress).max(BigDecimal.ZERO);

        // Calculate percentage (cap at 100%)
        double percentage = 0.0;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentage = currentProgress
                    .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
            percentage = Math.min(percentage, 100.0);
        }

        return GoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .targetDate(goal.getTargetDate())
                .startDate(goal.getStartDate())
                .currentProgress(currentProgress)
                .progressPercentage(Math.round(percentage * 100.0) / 100.0) // Round to 2 decimal places
                .remainingAmount(remaining)
                .build();
    }
}
