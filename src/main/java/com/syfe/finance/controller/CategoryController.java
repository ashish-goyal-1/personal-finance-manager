package com.syfe.finance.controller;

import com.syfe.finance.dto.*;
import com.syfe.finance.entity.User;
import com.syfe.finance.service.AuthService;
import com.syfe.finance.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing transaction categories.
 * Allows retrieving default/custom categories and managing custom categories.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthService authService;

    /**
     * Retrieves all categories available to the authenticated user.
     * Includes both system default categories and user-specific custom categories.
     *
     * @return a list of categories
     */
    @GetMapping
    public ResponseEntity<CategoryListResponse> getAllCategories() {
        User currentUser = authService.getCurrentUser();
        CategoryListResponse response = categoryService.getAllCategories(currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new custom category for the authenticated user.
     *
     * @param request the category creation request
     * @return the created category details
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        User currentUser = authService.getCurrentUser();
        CategoryResponse response = categoryService.createCategory(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Deletes a custom category by its name.
     * Default categories cannot be deleted.
     *
     * @param name the name of the category to delete
     * @return a success message
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<MessageResponse> deleteCategory(@PathVariable String name) {
        User currentUser = authService.getCurrentUser();
        categoryService.deleteCategory(name, currentUser);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Category deleted successfully")
                .build());
    }
}
