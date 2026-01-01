package com.syfe.finance.dto;

import com.syfe.finance.entity.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    // ==================== RegisterRequest ====================
    @Test
    @DisplayName("RegisterRequest - all methods")
    void testRegisterRequest() {
        // No-args constructor + setters
        RegisterRequest r1 = new RegisterRequest();
        r1.setUsername("test@example.com");
        r1.setPassword("password123");
        r1.setFullName("Test User");
        r1.setPhoneNumber("+1234567890");

        assertEquals("test@example.com", r1.getUsername());
        assertEquals("password123", r1.getPassword());
        assertEquals("Test User", r1.getFullName());
        assertEquals("+1234567890", r1.getPhoneNumber());

        // All-args constructor
        RegisterRequest r2 = new RegisterRequest("test@example.com", "password123", "Test User", "+1234567890");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotNull(r1.toString());

        // Builder
        RegisterRequest r3 = RegisterRequest.builder()
                .username("test@example.com")
                .password("password123")
                .fullName("Test User")
                .phoneNumber("+1234567890")
                .build();
        assertEquals(r1, r3);

        // Inequality
        RegisterRequest r4 = new RegisterRequest("other@example.com", "pass", "Other", "123");
        assertNotEquals(r1, r4);
        assertNotEquals(r1, null);
        assertNotEquals(r1, "string");
    }

    // ==================== LoginRequest ====================
    @Test
    @DisplayName("LoginRequest - all methods")
    void testLoginRequest() {
        LoginRequest l1 = new LoginRequest();
        l1.setUsername("test@example.com");
        l1.setPassword("password123");

        assertEquals("test@example.com", l1.getUsername());
        assertEquals("password123", l1.getPassword());

        LoginRequest l2 = new LoginRequest("test@example.com", "password123");
        assertEquals(l1, l2);
        assertEquals(l1.hashCode(), l2.hashCode());

        LoginRequest l3 = LoginRequest.builder().username("test@example.com").password("password123").build();
        assertEquals(l1, l3);

        assertNotNull(l1.toString());
        assertNotEquals(l1, new LoginRequest("other", "pass"));
    }

    // ==================== AuthResponse ====================
    @Test
    @DisplayName("AuthResponse - all methods")
    void testAuthResponse() {
        AuthResponse a1 = new AuthResponse();
        a1.setMessage("Success");
        a1.setUserId(1L);

        assertEquals("Success", a1.getMessage());
        assertEquals(1L, a1.getUserId());

        AuthResponse a2 = new AuthResponse("Success", 1L);
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());

        AuthResponse a3 = AuthResponse.builder().message("Success").userId(1L).build();
        assertEquals(a1, a3);
        assertNotNull(a1.toString());
    }

    // ==================== TransactionRequest ====================
    @Test
    @DisplayName("TransactionRequest - all methods")
    void testTransactionRequest() {
        TransactionRequest t1 = new TransactionRequest();
        t1.setAmount(new BigDecimal("5000.00"));
        t1.setDate(LocalDate.of(2026, 1, 15));
        t1.setCategory("Salary");
        t1.setDescription("Jan salary");

        assertEquals(new BigDecimal("5000.00"), t1.getAmount());
        assertEquals(LocalDate.of(2026, 1, 15), t1.getDate());
        assertEquals("Salary", t1.getCategory());
        assertEquals("Jan salary", t1.getDescription());

        TransactionRequest t2 = new TransactionRequest(new BigDecimal("5000.00"), LocalDate.of(2026, 1, 15), "Salary", "Jan salary");
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());

        TransactionRequest t3 = TransactionRequest.builder()
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.of(2026, 1, 15))
                .category("Salary")
                .description("Jan salary")
                .build();
        assertEquals(t1, t3);
        assertNotNull(t1.toString());
    }

    // ==================== TransactionUpdateRequest ====================
    @Test
    @DisplayName("TransactionUpdateRequest - all methods")
    void testTransactionUpdateRequest() {
        TransactionUpdateRequest t1 = new TransactionUpdateRequest();
        t1.setAmount(new BigDecimal("6000.00"));
        t1.setCategory("Bonus");
        t1.setDescription("Updated");

        TransactionUpdateRequest t2 = new TransactionUpdateRequest(new BigDecimal("6000.00"), "Bonus", "Updated");
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());

        TransactionUpdateRequest t3 = TransactionUpdateRequest.builder()
                .amount(new BigDecimal("6000.00"))
                .category("Bonus")
                .description("Updated")
                .build();
        assertEquals(t1, t3);
        assertNotNull(t1.toString());
    }

    // ==================== TransactionResponse ====================
    @Test
    @DisplayName("TransactionResponse - all methods")
    void testTransactionResponse() {
        TransactionResponse t1 = new TransactionResponse();
        t1.setId(1L);
        t1.setAmount(new BigDecimal("5000.00"));
        t1.setDate(LocalDate.of(2026, 1, 15));
        t1.setCategory("Salary");
        t1.setDescription("Jan salary");
        t1.setType("INCOME");

        TransactionResponse t2 = new TransactionResponse(1L, new BigDecimal("5000.00"), LocalDate.of(2026, 1, 15), "Salary", "Jan salary", "INCOME");
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());

        TransactionResponse t3 = TransactionResponse.builder()
                .id(1L)
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.of(2026, 1, 15))
                .category("Salary")
                .description("Jan salary")
                .type("INCOME")
                .build();
        assertEquals(t1, t3);
        assertNotNull(t1.toString());
    }

    // ==================== TransactionListResponse ====================
    @Test
    @DisplayName("TransactionListResponse - all methods")
    void testTransactionListResponse() {
        List<TransactionResponse> list = Arrays.asList(TransactionResponse.builder().id(1L).build());
        
        TransactionListResponse t1 = new TransactionListResponse();
        t1.setTransactions(list);

        TransactionListResponse t2 = new TransactionListResponse(list);
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());

        TransactionListResponse t3 = TransactionListResponse.builder().transactions(list).build();
        assertEquals(t1, t3);
        assertNotNull(t1.toString());
    }

    // ==================== CreateCategoryRequest ====================
    @Test
    @DisplayName("CreateCategoryRequest - all methods")
    void testCreateCategoryRequest() {
        CreateCategoryRequest c1 = new CreateCategoryRequest();
        c1.setName("Investments");
        c1.setType(TransactionType.INCOME);

        CreateCategoryRequest c2 = new CreateCategoryRequest("Investments", TransactionType.INCOME);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        CreateCategoryRequest c3 = CreateCategoryRequest.builder()
                .name("Investments")
                .type(TransactionType.INCOME)
                .build();
        assertEquals(c1, c3);
        assertNotNull(c1.toString());
    }

    // ==================== CategoryResponse ====================
    @Test
    @DisplayName("CategoryResponse - all methods")
    void testCategoryResponse() {
        CategoryResponse c1 = new CategoryResponse();
        c1.setName("Salary");
        c1.setType("INCOME");
        c1.setCustom(false);

        CategoryResponse c2 = new CategoryResponse("Salary", "INCOME", false);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        CategoryResponse c3 = CategoryResponse.builder()
                .name("Salary")
                .type("INCOME")
                .isCustom(false)
                .build();
        assertEquals(c1, c3);
        assertNotNull(c1.toString());
        assertFalse(c1.isCustom());
    }

    // ==================== CategoryListResponse ====================
    @Test
    @DisplayName("CategoryListResponse - all methods")
    void testCategoryListResponse() {
        List<CategoryResponse> list = Arrays.asList(CategoryResponse.builder().name("Test").build());
        
        CategoryListResponse c1 = new CategoryListResponse();
        c1.setCategories(list);

        CategoryListResponse c2 = new CategoryListResponse(list);
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());

        CategoryListResponse c3 = CategoryListResponse.builder().categories(list).build();
        assertEquals(c1, c3);
        assertNotNull(c1.toString());
    }

    // ==================== GoalRequest ====================
    @Test
    @DisplayName("GoalRequest - all methods")
    void testGoalRequest() {
        GoalRequest g1 = new GoalRequest();
        g1.setGoalName("Emergency Fund");
        g1.setTargetAmount(new BigDecimal("10000.00"));
        g1.setTargetDate(LocalDate.of(2026, 12, 31));
        g1.setStartDate(LocalDate.of(2026, 1, 1));

        GoalRequest g2 = new GoalRequest("Emergency Fund", new BigDecimal("10000.00"), LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1));
        assertEquals(g1, g2);
        assertEquals(g1.hashCode(), g2.hashCode());

        GoalRequest g3 = GoalRequest.builder()
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("10000.00"))
                .targetDate(LocalDate.of(2026, 12, 31))
                .startDate(LocalDate.of(2026, 1, 1))
                .build();
        assertEquals(g1, g3);
        assertNotNull(g1.toString());
    }

    // ==================== GoalUpdateRequest ====================
    @Test
    @DisplayName("GoalUpdateRequest - all methods")
    void testGoalUpdateRequest() {
        GoalUpdateRequest g1 = new GoalUpdateRequest();
        g1.setTargetAmount(new BigDecimal("15000.00"));
        g1.setTargetDate(LocalDate.of(2027, 6, 30));

        GoalUpdateRequest g2 = new GoalUpdateRequest(new BigDecimal("15000.00"), LocalDate.of(2027, 6, 30));
        assertEquals(g1, g2);
        assertEquals(g1.hashCode(), g2.hashCode());

        GoalUpdateRequest g3 = GoalUpdateRequest.builder()
                .targetAmount(new BigDecimal("15000.00"))
                .targetDate(LocalDate.of(2027, 6, 30))
                .build();
        assertEquals(g1, g3);
        assertNotNull(g1.toString());
    }

    // ==================== GoalResponse ====================
    @Test
    @DisplayName("GoalResponse - all methods")
    void testGoalResponse() {
        GoalResponse g1 = new GoalResponse();
        g1.setId(1L);
        g1.setGoalName("Emergency Fund");
        g1.setTargetAmount(new BigDecimal("10000.00"));
        g1.setTargetDate(LocalDate.of(2026, 12, 31));
        g1.setStartDate(LocalDate.of(2026, 1, 1));
        g1.setCurrentProgress(new BigDecimal("3000.00"));
        g1.setProgressPercentage(30.0);
        g1.setRemainingAmount(new BigDecimal("7000.00"));

        GoalResponse g2 = new GoalResponse(1L, "Emergency Fund", new BigDecimal("10000.00"),
                LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1),
                new BigDecimal("3000.00"), 30.0, new BigDecimal("7000.00"));
        assertEquals(g1, g2);
        assertEquals(g1.hashCode(), g2.hashCode());

        GoalResponse g3 = GoalResponse.builder()
                .id(1L)
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("10000.00"))
                .targetDate(LocalDate.of(2026, 12, 31))
                .startDate(LocalDate.of(2026, 1, 1))
                .currentProgress(new BigDecimal("3000.00"))
                .progressPercentage(30.0)
                .remainingAmount(new BigDecimal("7000.00"))
                .build();
        assertEquals(g1, g3);
        assertNotNull(g1.toString());
    }

    // ==================== GoalListResponse ====================
    @Test
    @DisplayName("GoalListResponse - all methods")
    void testGoalListResponse() {
        List<GoalResponse> list = Arrays.asList(GoalResponse.builder().id(1L).build());
        
        GoalListResponse g1 = new GoalListResponse();
        g1.setGoals(list);

        GoalListResponse g2 = new GoalListResponse(list);
        assertEquals(g1, g2);
        assertEquals(g1.hashCode(), g2.hashCode());

        GoalListResponse g3 = GoalListResponse.builder().goals(list).build();
        assertEquals(g1, g3);
        assertNotNull(g1.toString());
    }

    // ==================== MonthlyReportResponse ====================
    @Test
    @DisplayName("MonthlyReportResponse - all methods")
    void testMonthlyReportResponse() {
        Map<String, BigDecimal> income = new HashMap<>();
        income.put("Salary", new BigDecimal("5000.00"));
        Map<String, BigDecimal> expenses = new HashMap<>();
        expenses.put("Food", new BigDecimal("500.00"));

        MonthlyReportResponse m1 = new MonthlyReportResponse();
        m1.setMonth(1);
        m1.setYear(2026);
        m1.setTotalIncome(income);
        m1.setTotalExpenses(expenses);
        m1.setNetSavings(new BigDecimal("4500.00"));

        MonthlyReportResponse m2 = new MonthlyReportResponse(1, 2026, income, expenses, new BigDecimal("4500.00"));
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());

        MonthlyReportResponse m3 = MonthlyReportResponse.builder()
                .month(1)
                .year(2026)
                .totalIncome(income)
                .totalExpenses(expenses)
                .netSavings(new BigDecimal("4500.00"))
                .build();
        assertEquals(m1, m3);
        assertNotNull(m1.toString());
    }

    // ==================== YearlyReportResponse ====================
    @Test
    @DisplayName("YearlyReportResponse - all methods")
    void testYearlyReportResponse() {
        Map<String, BigDecimal> income = new HashMap<>();
        Map<String, BigDecimal> expenses = new HashMap<>();

        YearlyReportResponse y1 = new YearlyReportResponse();
        y1.setYear(2026);
        y1.setTotalIncome(income);
        y1.setTotalExpenses(expenses);
        y1.setNetSavings(new BigDecimal("36000.00"));

        YearlyReportResponse y2 = new YearlyReportResponse(2026, income, expenses, new BigDecimal("36000.00"));
        assertEquals(y1, y2);
        assertEquals(y1.hashCode(), y2.hashCode());

        YearlyReportResponse y3 = YearlyReportResponse.builder()
                .year(2026)
                .totalIncome(income)
                .totalExpenses(expenses)
                .netSavings(new BigDecimal("36000.00"))
                .build();
        assertEquals(y1, y3);
        assertNotNull(y1.toString());
    }

    // ==================== MessageResponse ====================
    @Test
    @DisplayName("MessageResponse - all methods")
    void testMessageResponse() {
        MessageResponse m1 = new MessageResponse();
        m1.setMessage("Success");

        MessageResponse m2 = new MessageResponse("Success");
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());

        MessageResponse m3 = MessageResponse.builder().message("Success").build();
        assertEquals(m1, m3);
        assertNotNull(m1.toString());
    }

    // ==================== Edge cases for equals ====================
    @Test
    @DisplayName("DTO equals edge cases")
    void testEqualsEdgeCases() {
        RegisterRequest r1 = RegisterRequest.builder().username("a@b.com").password("p").fullName("N").phoneNumber("1").build();
        RegisterRequest r2 = RegisterRequest.builder().username("a@b.com").password("p").fullName("N").phoneNumber("1").build();
        
        // Same object
        assertEquals(r1, r1);
        // Equal objects
        assertEquals(r1, r2);
        // Null
        assertNotEquals(r1, null);
        // Different type
        assertNotEquals(r1, "string");
        
        // Test with some null fields
        RegisterRequest r3 = RegisterRequest.builder().username("a@b.com").password("p").build();
        RegisterRequest r4 = RegisterRequest.builder().username("a@b.com").password("p").build();
        assertEquals(r3, r4);
    }
}
