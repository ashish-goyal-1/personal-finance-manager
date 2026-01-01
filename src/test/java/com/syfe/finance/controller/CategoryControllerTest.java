package com.syfe.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syfe.finance.dto.*;
import com.syfe.finance.entity.TransactionType;
import com.syfe.finance.entity.User;
import com.syfe.finance.exception.UnauthorizedAccessException;
import com.syfe.finance.exception.ValidationException;
import com.syfe.finance.service.AuthService;
import com.syfe.finance.service.CategoryService;
import com.syfe.finance.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

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
    @DisplayName("GET /api/categories - Returns list of categories")
    void getAllCategories_Success() throws Exception {
        CategoryListResponse listResponse = CategoryListResponse.builder()
                .categories(Arrays.asList(
                        CategoryResponse.builder().name("Salary").type("INCOME").isCustom(false).build(),
                        CategoryResponse.builder().name("Food").type("EXPENSE").isCustom(false).build()
                ))
                .build();

        when(authService.getCurrentUser()).thenReturn(user);
        when(categoryService.getAllCategories(1L)).thenReturn(listResponse);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray())
                .andExpect(jsonPath("$.categories[0].name").value("Salary"));
    }

    @Test
    @DisplayName("POST /api/categories - Create custom category returns 201")
    void createCategory_Success() throws Exception {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Investments")
                .type(TransactionType.INCOME)
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .name("Investments")
                .type("INCOME")
                .isCustom(true)
                .build();

        when(authService.getCurrentUser()).thenReturn(user);
        when(categoryService.createCategory(any(CreateCategoryRequest.class), any(User.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Investments"))
                .andExpect(jsonPath("$.custom").value(true));
    }

    @Test
    @DisplayName("DELETE /api/categories/{name} - Delete custom category")
    void deleteCategory_Success() throws Exception {
        when(authService.getCurrentUser()).thenReturn(user);
        doNothing().when(categoryService).deleteCategory("CustomCategory", user);

        mockMvc.perform(delete("/api/categories/CustomCategory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/categories/{name} - Delete default category returns 403")
    void deleteCategory_DefaultCategory() throws Exception {
        when(authService.getCurrentUser()).thenReturn(user);
        doThrow(new UnauthorizedAccessException("Cannot delete default category"))
                .when(categoryService).deleteCategory("Salary", user);

        mockMvc.perform(delete("/api/categories/Salary"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/categories/{name} - Delete category in use returns 400")
    void deleteCategory_InUse() throws Exception {
        when(authService.getCurrentUser()).thenReturn(user);
        doThrow(new ValidationException("Category is in use"))
                .when(categoryService).deleteCategory("Food", user);

        mockMvc.perform(delete("/api/categories/Food"))
                .andExpect(status().isBadRequest());
    }
}
