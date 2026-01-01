package com.syfe.finance.controller;

import com.syfe.finance.dto.*;
import com.syfe.finance.entity.User;
import com.syfe.finance.service.AuthService;
import com.syfe.finance.service.SavingsGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing savings goals.
 * Supports creating, retrieving, updating, and deleting goals.
 */
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;
    private final AuthService authService;

    /**
     * Creates a new savings goal.
     *
     * @param request the goal creation request
     * @return the created goal details
     */
    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalRequest request) {
        User currentUser = authService.getCurrentUser();
        GoalResponse response = savingsGoalService.createGoal(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<GoalListResponse> getAllGoals() {
        User currentUser = authService.getCurrentUser();
        GoalListResponse response = savingsGoalService.getAllGoals(currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a specific savings goal by its ID.
     *
     * @param id the goal ID
     * @return the goal details
     */
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoalById(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        GoalResponse response = savingsGoalService.getGoalById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing savings goal.
     *
     * @param id      the goal ID
     * @param request the update request details
     * @return the updated goal details
     */
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody GoalUpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        GoalResponse response = savingsGoalService.updateGoal(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a savings goal by its ID.
     *
     * @param id the goal ID
     * @return a success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteGoal(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        savingsGoalService.deleteGoal(id, currentUser);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Goal deleted successfully")
                .build());
    }
}
