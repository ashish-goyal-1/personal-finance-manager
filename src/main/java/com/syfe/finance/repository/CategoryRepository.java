package com.syfe.finance.repository;

import com.syfe.finance.entity.Category;
import com.syfe.finance.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Category entities.
 * Supports finding defaults and user-specific categories.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Fetch both default (user is null) and user's custom categories
    @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.user IS NULL")
    List<Category> findByUserIdOrUserIsNull(@Param("userId") Long userId);

    // Find category by name for a specific user (including defaults)
    @Query("SELECT c FROM Category c WHERE c.name = :name AND (c.user.id = :userId OR c.user IS NULL)")
    Optional<Category> findByNameAndUserIdOrDefault(
            @Param("name") String name,
            @Param("userId") Long userId);

    // Check if custom category name exists for user
    boolean existsByNameAndUserId(String name, Long userId);

    // Find only custom categories for a user
    List<Category> findByUserId(Long userId);

    // Find default categories only
    List<Category> findByUserIsNull();

    // Find by name and type for a user (for validation)
    Optional<Category> findByNameAndTypeAndUserId(String name, TransactionType type, Long userId);

    // Find default category by name
    Optional<Category> findByNameAndUserIsNull(String name);
}
