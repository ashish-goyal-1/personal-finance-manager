package com.syfe.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syfe.finance.dto.*;
import com.syfe.finance.entity.User;
import com.syfe.finance.service.AuthService;
import com.syfe.finance.service.CustomUserDetailsService;
import com.syfe.finance.service.SavingsGoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SavingsGoalController.class)
@AutoConfigureMockMvc(addFilters = false)
class SavingsGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SavingsGoalService savingsGoalService;

    @MockBean
    private AuthService authService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private AuthenticationManager authenticationManager;

    private User user;
    private GoalRequest goalRequest;
    private GoalResponse goalResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("test@example.com")
                .build();

        goalRequest = GoalRequest.builder()
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("10000.00"))
                .targetDate(LocalDate.now().plusMonths(6))
                .startDate(LocalDate.now())
                .build();

        goalResponse = GoalResponse.builder()
                .id(1L)
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("10000.00"))
                .targetDate(LocalDate.now().plusMonths(6))
                .startDate(LocalDate.now())
                .currentProgress(new BigDecimal("3000.00"))
                .progressPercentage(30.0)
                .remainingAmount(new BigDecimal("7000.00"))
                .build();
    }

    @Test
    @DisplayName("POST /api/goals - Create goal returns 201")
    void createGoal_Success() throws Exception {
        when(authService.getCurrentUser()).thenReturn(user);
        when(savingsGoalService.createGoal(any(GoalRequest.class), any(User.class)))
                .thenReturn(goalResponse);

        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"))
                .andExpect(jsonPath("$.progressPercentage").value(30.0));
    }

    @Test
    @DisplayName("GET /api/goals - Returns list with progress")
    void getAllGoals_Success() throws Exception {
        GoalListResponse listResponse = GoalListResponse.builder()
                .goals(Arrays.asList(goalResponse))
                .build();

        when(authService.getCurrentUser()).thenReturn(user);
        when(savingsGoalService.getAllGoals(any(User.class))).thenReturn(listResponse);

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals").isArray())
                .andExpect(jsonPath("$.goals[0].progressPercentage").value(30.0));
    }

    @Test
    @DisplayName("GET /api/goals/{id} - Returns single goal with progress")
    void getGoalById_Success() throws Exception {
        when(authService.getCurrentUser()).thenReturn(user);
        when(savingsGoalService.getGoalById(1L, user)).thenReturn(goalResponse);

        mockMvc.perform(get("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.currentProgress").value(3000.00));
    }

    @Test
    @DisplayName("PUT /api/goals/{id} - Update goal")
    void updateGoal_Success() throws Exception {
        GoalUpdateRequest updateRequest = GoalUpdateRequest.builder()
                .targetAmount(new BigDecimal("15000.00"))
                .build();

        when(authService.getCurrentUser()).thenReturn(user);
        when(savingsGoalService.updateGoal(eq(1L), any(GoalUpdateRequest.class), any(User.class)))
                .thenReturn(goalResponse);

        mockMvc.perform(put("/api/goals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/goals/{id} - Delete goal returns 200")
    void deleteGoal_Success() throws Exception {
        when(authService.getCurrentUser()).thenReturn(user);
        doNothing().when(savingsGoalService).deleteGoal(1L, user);

        mockMvc.perform(delete("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goal deleted successfully"));
    }
}
