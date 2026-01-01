package com.syfe.finance.service;

import com.syfe.finance.dto.CategoryListResponse;
import com.syfe.finance.dto.CategoryResponse;
import com.syfe.finance.dto.CreateCategoryRequest;
import com.syfe.finance.entity.Category;
import com.syfe.finance.entity.TransactionType;
import com.syfe.finance.entity.User;
import com.syfe.finance.exception.DuplicateResourceException;
import com.syfe.finance.exception.ResourceNotFoundException;
import com.syfe.finance.exception.UnauthorizedAccessException;
import com.syfe.finance.exception.ValidationException;
import com.syfe.finance.repository.CategoryRepository;
import com.syfe.finance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User user;
    private Category defaultCategory;
    private Category customCategory;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("test@example.com")
                .build();

        defaultCategory = Category.builder()
                .id(1L)
                .name("Salary")
                .type(TransactionType.INCOME)
                .user(null)
                .build();

        customCategory = Category.builder()
                .id(2L)
                .name("Freelance")
                .type(TransactionType.INCOME)
                .user(user)
                .build();
    }

    @Test
    @DisplayName("Should get all categories including default and custom")
    void getAllCategories_Success() {
        List<Category> categories = Arrays.asList(defaultCategory, customCategory);
        when(categoryRepository.findByUserIdOrUserIsNull(user.getId())).thenReturn(categories);

        CategoryListResponse response = categoryService.getAllCategories(user.getId());

        assertNotNull(response);
        assertEquals(2, response.getCategories().size());
        verify(categoryRepository).findByUserIdOrUserIsNull(user.getId());
    }

    @Test
    @DisplayName("Should create custom category successfully")
    void createCategory_Success() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Investments")
                .type(TransactionType.INCOME)
                .build();

        when(categoryRepository.existsByNameAndUserId(request.getName(), user.getId())).thenReturn(false);
        when(categoryRepository.findByNameAndUserIsNull(request.getName())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(
                Category.builder()
                        .id(3L)
                        .name("Investments")
                        .type(TransactionType.INCOME)
                        .user(user)
                        .build()
        );

        CategoryResponse response = categoryService.createCategory(request, user);

        assertNotNull(response);
        assertEquals("Investments", response.getName());
        assertTrue(response.isCustom());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException for duplicate custom category name")
    void createCategory_DuplicateName() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Freelance")
                .type(TransactionType.INCOME)
                .build();

        when(categoryRepository.existsByNameAndUserId(request.getName(), user.getId())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, 
                () -> categoryService.createCategory(request, user));
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when name conflicts with default category")
    void createCategory_ConflictsWithDefault() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Salary")
                .type(TransactionType.INCOME)
                .build();

        when(categoryRepository.existsByNameAndUserId(request.getName(), user.getId())).thenReturn(false);
        when(categoryRepository.findByNameAndUserIsNull(request.getName())).thenReturn(Optional.of(defaultCategory));

        assertThrows(DuplicateResourceException.class, 
                () -> categoryService.createCategory(request, user));
    }

    @Test
    @DisplayName("Should delete custom category successfully")
    void deleteCategory_Success() {
        when(categoryRepository.findByNameAndUserIdOrDefault("Freelance", user.getId()))
                .thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategoryId(customCategory.getId())).thenReturn(false);

        assertDoesNotThrow(() -> categoryService.deleteCategory("Freelance", user));
        verify(categoryRepository).delete(customCategory);
    }

    @Test
    @DisplayName("Should throw UnauthorizedAccessException when deleting default category")
    void deleteCategory_DefaultCategory() {
        when(categoryRepository.findByNameAndUserIdOrDefault("Salary", user.getId()))
                .thenReturn(Optional.of(defaultCategory));

        assertThrows(UnauthorizedAccessException.class, 
                () -> categoryService.deleteCategory("Salary", user));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when deleting category in use")
    void deleteCategory_InUse() {
        when(categoryRepository.findByNameAndUserIdOrDefault("Freelance", user.getId()))
                .thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategoryId(customCategory.getId())).thenReturn(true);

        assertThrows(ValidationException.class, 
                () -> categoryService.deleteCategory("Freelance", user));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for unknown category")
    void deleteCategory_NotFound() {
        when(categoryRepository.findByNameAndUserIdOrDefault("Unknown", user.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> categoryService.deleteCategory("Unknown", user));
    }

    @Test
    @DisplayName("Should throw UnauthorizedAccessException when deleting another user's category")
    void deleteCategory_AnotherUsersCategory() {
        User otherUser = User.builder().id(2L).username("other@example.com").build();
        Category otherUserCategory = Category.builder()
                .id(3L)
                .name("OtherCategory")
                .type(TransactionType.EXPENSE)
                .user(otherUser)
                .build();

        when(categoryRepository.findByNameAndUserIdOrDefault("OtherCategory", user.getId()))
                .thenReturn(Optional.of(otherUserCategory));

        assertThrows(UnauthorizedAccessException.class, 
                () -> categoryService.deleteCategory("OtherCategory", user));
    }

    @Test
    @DisplayName("Should find category by name for user")
    void findCategoryByNameForUser_Success() {
        when(categoryRepository.findByNameAndUserIdOrDefault("Salary", user.getId()))
                .thenReturn(Optional.of(defaultCategory));

        Category result = categoryService.findCategoryByNameForUser("Salary", user.getId());

        assertNotNull(result);
        assertEquals("Salary", result.getName());
    }
}
