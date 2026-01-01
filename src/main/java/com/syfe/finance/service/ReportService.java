package com.syfe.finance.service;

import com.syfe.finance.dto.MonthlyReportResponse;
import com.syfe.finance.dto.YearlyReportResponse;
import com.syfe.finance.entity.Transaction;
import com.syfe.finance.entity.TransactionType;
import com.syfe.finance.exception.ValidationException;
import com.syfe.finance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for generating financial reports.
 * Aggregates transaction data into monthly and yearly summaries.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;

    /**
     * Generates a report for a specific month.
     *
     * @param userId the user ID
     * @param year   the year
     * @param month  the month (1-12)
     * @return the monthly report response
     */
    public MonthlyReportResponse getMonthlyReport(Long userId, int year, int month) {
        if (month < 1 || month > 12) {
            throw new ValidationException("Invalid month: " + month);
        }
        List<Transaction> transactions = transactionRepository.findByUserIdAndYearAndMonth(userId, year, month);

        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            String categoryName = transaction.getCategory().getName();
            BigDecimal amount = transaction.getAmount();

            if (transaction.getType() == TransactionType.INCOME) {
                incomeByCategory.merge(categoryName, amount, BigDecimal::add);
                totalIncome = totalIncome.add(amount);
            } else {
                expensesByCategory.merge(categoryName, amount, BigDecimal::add);
                totalExpenses = totalExpenses.add(amount);
            }
        }

        return MonthlyReportResponse.builder()
                .month(month)
                .year(year)
                .totalIncome(incomeByCategory)
                .totalExpenses(expensesByCategory)
                .netSavings(totalIncome.subtract(totalExpenses))
                .build();
    }

    /**
     * Generates a report for a specific year.
     *
     * @param userId the user ID
     * @param year   the year
     * @return the yearly report response
     */
    public YearlyReportResponse getYearlyReport(Long userId, int year) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndYear(userId, year);

        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            String categoryName = transaction.getCategory().getName();
            BigDecimal amount = transaction.getAmount();

            if (transaction.getType() == TransactionType.INCOME) {
                incomeByCategory.merge(categoryName, amount, BigDecimal::add);
                totalIncome = totalIncome.add(amount);
            } else {
                expensesByCategory.merge(categoryName, amount, BigDecimal::add);
                totalExpenses = totalExpenses.add(amount);
            }
        }

        return YearlyReportResponse.builder()
                .year(year)
                .totalIncome(incomeByCategory)
                .totalExpenses(expensesByCategory)
                .netSavings(totalIncome.subtract(totalExpenses))
                .build();
    }
}
