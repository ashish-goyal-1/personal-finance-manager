package com.syfe.finance.repository;

import com.syfe.finance.entity.Transaction;
import com.syfe.finance.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for managing Transaction entities.
 * Support custom queries for filtering by date, category and reporting
 * aggregation.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

        List<Transaction> findAllByUserIdOrderByDateDesc(Long userId);

        List<Transaction> findAllByUserIdAndDateBetweenOrderByDateDesc(
                        Long userId, LocalDate startDate, LocalDate endDate);

        List<Transaction> findAllByUserIdAndCategoryIdOrderByDateDesc(
                        Long userId, Long categoryId);

        /**
         * Finds transactions filtered by date range and category.
         */
        List<Transaction> findAllByUserIdAndDateBetweenAndCategoryIdOrderByDateDesc(
                        Long userId, LocalDate startDate, LocalDate endDate, Long categoryId);

        /**
         * Finds transactions for a specific month and year (used for monthly reports).
         */
        @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
                        "AND YEAR(t.date) = :year AND MONTH(t.date) = :month")
        List<Transaction> findByUserIdAndYearAndMonth(
                        @Param("userId") Long userId,
                        @Param("year") int year,
                        @Param("month") int month);

        /**
         * Finds transactions for a specific year (used for yearly reports).
         */
        @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND YEAR(t.date) = :year")
        List<Transaction> findByUserIdAndYear(
                        @Param("userId") Long userId,
                        @Param("year") int year);

        @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
                        "AND t.date >= :startDate AND t.type = :type")
        List<Transaction> findByUserIdAndDateAfterAndType(
                        @Param("userId") Long userId,
                        @Param("startDate") LocalDate startDate,
                        @Param("type") TransactionType type);

        boolean existsByCategoryId(Long categoryId);
}
