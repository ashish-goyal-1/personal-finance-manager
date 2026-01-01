package com.syfe.finance.service;

import com.syfe.finance.dto.CategoryListResponse;
import com.syfe.finance.dto.CategoryResponse;
import com.syfe.finance.dto.CreateCategoryRequest;
import com.syfe.finance.entity.Category;
import com.syfe.finance.entity.User;
import com.syfe.finance.exception.DuplicateResourceException;
import com.syfe.finance.exception.ResourceNotFoundException;
import com.syfe.finance.exception.UnauthorizedAccessException;
import com.syfe.finance.exception.ValidationException;
import com.syfe.finance.repository.CategoryRepository;
import com.syfe.finance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing transaction categories.
 * Handles default categories and user-specific custom categories.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Retrieves all categories available to a user (default + custom).
     *
     * @param userId the user ID
     * @return a list response of categories
     */
    public CategoryListResponse getAllCategories(Long userId) {
        // Get both default categories (user=null) and user's custom categories
        List<Category> categories = categoryRepository.findByUserIdOrUserIsNull(userId);

        List<CategoryResponse> categoryResponses = categories.stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());

        return CategoryListResponse.builder()
                .categories(categoryResponses)
                .build();
    }

    /**
     * Creates a new custom category.
     * Ensures uniqueness of category name for the user.
     *
     * @param request the creation request
     * @param user    the authenticated user
     * @return the created category response
     */
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request, User user) {
        // Check if category name already exists for this user
        if (categoryRepository.existsByNameAndUserId(request.getName(), user.getId())) {
            throw new DuplicateResourceException("Category", "name", request.getName());
        }

        // Check if category name conflicts with a default category
        if (categoryRepository.findByNameAndUserIsNull(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Category", "name",
                    request.getName() + " (conflicts with default category)");
        }

        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .user(user)
                .build();

        Category savedCategory = categoryRepository.save(category);
        return toCategoryResponse(savedCategory);
    }

    /**
     * Deletes a custom category.
     * Prevents deletion of default categories or categories in active use.
     *
     * @param categoryName the name of the category to delete
     * @param user         the authenticated user
     */
    @Transactional
    public void deleteCategory(String categoryName, User user) {
        // Find the category by name for this user
        Category category = categoryRepository.findByNameAndUserIdOrDefault(categoryName, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "name", categoryName));

        // Cannot delete default categories
        if (category.isDefault()) {
            throw new UnauthorizedAccessException("Cannot delete default category: " + categoryName);
        }

        // Cannot delete custom category belonging to another user
        if (!category.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("Category", category.getId());
        }

        // Check if category is used in any transaction
        if (transactionRepository.existsByCategoryId(category.getId())) {
            throw new ValidationException(
                    "Cannot delete category '" + categoryName + "' because it is used in transactions");
        }

        categoryRepository.delete(category);
    }

    /**
     * Finds a category by name for a specific user.
     *
     * @param categoryName the category name
     * @param userId       the user ID
     * @return the category entity
     */
    public Category findCategoryByNameForUser(String categoryName, Long userId) {
        return categoryRepository.findByNameAndUserIdOrDefault(categoryName, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "name", categoryName));
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .name(category.getName())
                .type(category.getType().name())
                .isCustom(category.isCustom())
                .build();
    }
}
