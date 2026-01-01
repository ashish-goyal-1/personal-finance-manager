package com.syfe.finance.controller;

import com.syfe.finance.dto.MonthlyReportResponse;
import com.syfe.finance.dto.YearlyReportResponse;
import com.syfe.finance.entity.User;
import com.syfe.finance.service.AuthService;
import com.syfe.finance.service.CustomUserDetailsService;
import com.syfe.finance.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private AuthService authService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private AuthenticationManager authenticationManager;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("test@example.com")
                .build();
    }

    @Test
    @DisplayName("GET /api/reports/monthly/{year}/{month} - Returns monthly report")
    void getMonthlyReport_Success() throws Exception {
        Map<String, BigDecimal> income = new HashMap<>();
        income.put("Salary", new BigDecimal("5000.00"));

        Map<String, BigDecimal> expenses = new HashMap<>();
        expenses.put("Food", new BigDecimal("500.00"));
        expenses.put("Rent", new BigDecimal("1500.00"));

        MonthlyReportResponse response = MonthlyReportResponse.builder()
                .month(1)
                .year(2026)
                .totalIncome(income)
                .totalExpenses(expenses)
                .netSavings(new BigDecimal("3000.00"))
                .build();

        when(authService.getCurrentUser()).thenReturn(user);
        when(reportService.getMonthlyReport(1L, 2026, 1)).thenReturn(response);

        mockMvc.perform(get("/api/reports/monthly/2026/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(1))
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.netSavings").value(3000.00))
                .andExpect(jsonPath("$.totalIncome.Salary").value(5000.00));
    }

    @Test
    @DisplayName("GET /api/reports/yearly/{year} - Returns yearly report")
    void getYearlyReport_Success() throws Exception {
        Map<String, BigDecimal> income = new HashMap<>();
        income.put("Salary", new BigDecimal("60000.00"));

        Map<String, BigDecimal> expenses = new HashMap<>();
        expenses.put("Food", new BigDecimal("6000.00"));
        expenses.put("Rent", new BigDecimal("18000.00"));

        YearlyReportResponse response = YearlyReportResponse.builder()
                .year(2026)
                .totalIncome(income)
                .totalExpenses(expenses)
                .netSavings(new BigDecimal("36000.00"))
                .build();

        when(authService.getCurrentUser()).thenReturn(user);
        when(reportService.getYearlyReport(1L, 2026)).thenReturn(response);

        mockMvc.perform(get("/api/reports/yearly/2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.netSavings").value(36000.00));
    }
}
