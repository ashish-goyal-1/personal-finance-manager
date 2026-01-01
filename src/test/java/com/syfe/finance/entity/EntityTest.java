package com.syfe.finance.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    @DisplayName("User entity - getters, setters, equals, hashCode, toString")
    void testUserEntity() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("test@example.com");
        user1.setPassword("password");
        user1.setFullName("Test User");
        user1.setPhoneNumber("+1234567890");

        User user2 = User.builder()
                .id(1L)
                .username("test@example.com")
                .password("password")
                .fullName("Test User")
                .phoneNumber("+1234567890")
                .build();

        assertEquals(1L, user1.getId());
        assertEquals("test@example.com", user1.getUsername());
        assertEquals("password", user1.getPassword());
        assertEquals("Test User", user1.getFullName());
        assertEquals("+1234567890", user1.getPhoneNumber());
        
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotNull(user1.toString());
    }

    @Test
    @DisplayName("Category entity - getters, setters, isDefault, isCustom")
    void testCategoryEntity() {
        User user = User.builder().id(1L).build();
        
        Category defaultCategory = new Category();
        defaultCategory.setId(1L);
        defaultCategory.setName("Salary");
        defaultCategory.setType(TransactionType.INCOME);
        defaultCategory.setUser(null);

        Category customCategory = Category.builder()
                .id(2L)
                .name("Freelance")
                .type(TransactionType.INCOME)
                .user(user)
                .build();

        assertEquals(1L, defaultCategory.getId());
        assertEquals("Salary", defaultCategory.getName());
        assertEquals(TransactionType.INCOME, defaultCategory.getType());
        assertNull(defaultCategory.getUser());
        assertTrue(defaultCategory.isDefault());
        assertFalse(defaultCategory.isCustom());

        assertFalse(customCategory.isDefault());
        assertTrue(customCategory.isCustom());
        
        assertNotNull(defaultCategory.toString());
        assertNotEquals(defaultCategory, customCategory);
    }

    @Test
    @DisplayName("Transaction entity - getters, setters, equals, hashCode")
    void testTransactionEntity() {
        User user = User.builder().id(1L).build();
        Category category = Category.builder().id(1L).name("Salary").build();
        
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(new BigDecimal("5000.00"));
        transaction.setDate(LocalDate.of(2026, 1, 15));
        transaction.setDescription("January salary");
        transaction.setType(TransactionType.INCOME);
        transaction.setUser(user);
        transaction.setCategory(category);

        Transaction transaction2 = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.of(2026, 1, 15))
                .description("January salary")
                .type(TransactionType.INCOME)
                .user(user)
                .category(category)
                .build();

        assertEquals(1L, transaction.getId());
        assertEquals(new BigDecimal("5000.00"), transaction.getAmount());
        assertEquals(LocalDate.of(2026, 1, 15), transaction.getDate());
        assertEquals("January salary", transaction.getDescription());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertEquals(user, transaction.getUser());
        assertEquals(category, transaction.getCategory());
        
        assertEquals(transaction, transaction2);
        assertEquals(transaction.hashCode(), transaction2.hashCode());
        assertNotNull(transaction.toString());
    }

    @Test
    @DisplayName("SavingsGoal entity - getters, setters, equals, hashCode")
    void testSavingsGoalEntity() {
        User user = User.builder().id(1L).build();
        
        SavingsGoal goal = new SavingsGoal();
        goal.setId(1L);
        goal.setGoalName("Emergency Fund");
        goal.setTargetAmount(new BigDecimal("10000.00"));
        goal.setTargetDate(LocalDate.of(2026, 12, 31));
        goal.setStartDate(LocalDate.of(2026, 1, 1));
        goal.setUser(user);

        SavingsGoal goal2 = SavingsGoal.builder()
                .id(1L)
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("10000.00"))
                .targetDate(LocalDate.of(2026, 12, 31))
                .startDate(LocalDate.of(2026, 1, 1))
                .user(user)
                .build();

        assertEquals(1L, goal.getId());
        assertEquals("Emergency Fund", goal.getGoalName());
        assertEquals(new BigDecimal("10000.00"), goal.getTargetAmount());
        assertEquals(LocalDate.of(2026, 12, 31), goal.getTargetDate());
        assertEquals(LocalDate.of(2026, 1, 1), goal.getStartDate());
        assertEquals(user, goal.getUser());
        
        assertEquals(goal, goal2);
        assertEquals(goal.hashCode(), goal2.hashCode());
        assertNotNull(goal.toString());
    }

    @Test
    @DisplayName("TransactionType enum values")
    void testTransactionTypeEnum() {
        assertEquals("INCOME", TransactionType.INCOME.name());
        assertEquals("EXPENSE", TransactionType.EXPENSE.name());
        assertEquals(2, TransactionType.values().length);
        assertEquals(TransactionType.INCOME, TransactionType.valueOf("INCOME"));
    }
}
