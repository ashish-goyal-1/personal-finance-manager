package com.syfe.finance.service;

import com.syfe.finance.dto.MonthlyReportResponse;
import com.syfe.finance.dto.YearlyReportResponse;
import com.syfe.finance.entity.Category;
import com.syfe.finance.entity.Transaction;
import com.syfe.finance.entity.TransactionType;
import com.syfe.finance.entity.User;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    private User user;
    private Category salaryCategory;
    private Category foodCategory;
    private Category rentCategory;
    private Transaction incomeTransaction;
    private Transaction expenseTransaction1;
    private Transaction expenseTransaction2;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("test@example.com")
                .build();

        salaryCategory = Category.builder()
                .id(1L)
                .name("Salary")
                .type(TransactionType.INCOME)
                .build();

        foodCategory = Category.builder()
                .id(2L)
                .name("Food")
                .type(TransactionType.EXPENSE)
                .build();

        rentCategory = Category.builder()
                .id(3L)
                .name("Rent")
                .type(TransactionType.EXPENSE)
                .build();

        incomeTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.of(2026, 1, 15))
                .type(TransactionType.INCOME)
                .category(salaryCategory)
                .user(user)
                .build();

        expenseTransaction1 = Transaction.builder()
                .id(2L)
                .amount(new BigDecimal("500.00"))
                .date(LocalDate.of(2026, 1, 10))
                .type(TransactionType.EXPENSE)
                .category(foodCategory)
                .user(user)
                .build();

        expenseTransaction2 = Transaction.builder()
                .id(3L)
                .amount(new BigDecimal("1500.00"))
                .date(LocalDate.of(2026, 1, 5))
                .type(TransactionType.EXPENSE)
                .category(rentCategory)
                .user(user)
                .build();
    }

    @Test
    @DisplayName("Should get monthly report with data")
    void getMonthlyReport_WithData() {
        List<Transaction> transactions = Arrays.asList(incomeTransaction, expenseTransaction1, expenseTransaction2);
        when(transactionRepository.findByUserIdAndYearAndMonth(user.getId(), 2026, 1))
                .thenReturn(transactions);

        MonthlyReportResponse response = reportService.getMonthlyReport(user.getId(), 2026, 1);

        assertNotNull(response);
        assertEquals(1, response.getMonth());
        assertEquals(2026, response.getYear());
        
        // Verify income breakdown
        assertEquals(1, response.getTotalIncome().size());
        assertEquals(new BigDecimal("5000.00"), response.getTotalIncome().get("Salary"));
        
        // Verify expense breakdown
        assertEquals(2, response.getTotalExpenses().size());
        assertEquals(new BigDecimal("500.00"), response.getTotalExpenses().get("Food"));
        assertEquals(new BigDecimal("1500.00"), response.getTotalExpenses().get("Rent"));
        
        // Verify net savings: 5000 - 500 - 1500 = 3000
        assertEquals(new BigDecimal("3000.00"), response.getNetSavings());
    }

    @Test
    @DisplayName("Should get monthly report with no data")
    void getMonthlyReport_NoData() {
        when(transactionRepository.findByUserIdAndYearAndMonth(user.getId(), 2026, 2))
                .thenReturn(Collections.emptyList());

        MonthlyReportResponse response = reportService.getMonthlyReport(user.getId(), 2026, 2);

        assertNotNull(response);
        assertEquals(2, response.getMonth());
        assertEquals(2026, response.getYear());
        assertTrue(response.getTotalIncome().isEmpty());
        assertTrue(response.getTotalExpenses().isEmpty());
        assertEquals(BigDecimal.ZERO, response.getNetSavings());
    }

    @Test
    @DisplayName("Should aggregate multiple transactions in same category")
    void getMonthlyReport_AggregatesSameCategory() {
        Transaction anotherFoodExpense = Transaction.builder()
                .id(4L)
                .amount(new BigDecimal("300.00"))
                .date(LocalDate.of(2026, 1, 20))
                .type(TransactionType.EXPENSE)
                .category(foodCategory)
                .user(user)
                .build();

        List<Transaction> transactions = Arrays.asList(expenseTransaction1, anotherFoodExpense);
        when(transactionRepository.findByUserIdAndYearAndMonth(user.getId(), 2026, 1))
                .thenReturn(transactions);

        MonthlyReportResponse response = reportService.getMonthlyReport(user.getId(), 2026, 1);

        assertNotNull(response);
        // Food: 500 + 300 = 800
        assertEquals(new BigDecimal("800.00"), response.getTotalExpenses().get("Food"));
    }

    @Test
    @DisplayName("Should get yearly report with data")
    void getYearlyReport_WithData() {
        List<Transaction> transactions = Arrays.asList(incomeTransaction, expenseTransaction1, expenseTransaction2);
        when(transactionRepository.findByUserIdAndYear(user.getId(), 2026))
                .thenReturn(transactions);

        YearlyReportResponse response = reportService.getYearlyReport(user.getId(), 2026);

        assertNotNull(response);
        assertEquals(2026, response.getYear());
        
        // Verify income breakdown
        assertEquals(1, response.getTotalIncome().size());
        assertEquals(new BigDecimal("5000.00"), response.getTotalIncome().get("Salary"));
        
        // Verify expense breakdown
        assertEquals(2, response.getTotalExpenses().size());
        assertEquals(new BigDecimal("500.00"), response.getTotalExpenses().get("Food"));
        assertEquals(new BigDecimal("1500.00"), response.getTotalExpenses().get("Rent"));
        
        // Verify net savings: 5000 - 500 - 1500 = 3000
        assertEquals(new BigDecimal("3000.00"), response.getNetSavings());
    }

    @Test
    @DisplayName("Should get yearly report with no data")
    void getYearlyReport_NoData() {
        when(transactionRepository.findByUserIdAndYear(user.getId(), 2025))
                .thenReturn(Collections.emptyList());

        YearlyReportResponse response = reportService.getYearlyReport(user.getId(), 2025);

        assertNotNull(response);
        assertEquals(2025, response.getYear());
        assertTrue(response.getTotalIncome().isEmpty());
        assertTrue(response.getTotalExpenses().isEmpty());
        assertEquals(BigDecimal.ZERO, response.getNetSavings());
    }

    @Test
    @DisplayName("Should handle negative net savings (more expenses than income)")
    void getMonthlyReport_NegativeNetSavings() {
        Transaction smallIncome = Transaction.builder()
                .id(5L)
                .amount(new BigDecimal("1000.00"))
                .date(LocalDate.of(2026, 1, 15))
                .type(TransactionType.INCOME)
                .category(salaryCategory)
                .user(user)
                .build();

        List<Transaction> transactions = Arrays.asList(smallIncome, expenseTransaction2);
        when(transactionRepository.findByUserIdAndYearAndMonth(user.getId(), 2026, 1))
                .thenReturn(transactions);

        MonthlyReportResponse response = reportService.getMonthlyReport(user.getId(), 2026, 1);

        assertNotNull(response);
        // Net savings: 1000 - 1500 = -500
        assertEquals(new BigDecimal("-500.00"), response.getNetSavings());
    }
}
