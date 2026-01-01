package com.syfe.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syfe.finance.dto.AuthResponse;
import com.syfe.finance.dto.LoginRequest;
import com.syfe.finance.dto.MessageResponse;
import com.syfe.finance.dto.RegisterRequest;
import com.syfe.finance.exception.DuplicateResourceException;
import com.syfe.finance.service.AuthService;
import com.syfe.finance.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private AuthenticationManager authenticationManager;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("test@example.com")
                .password("password123")
                .fullName("Test User")
                .phoneNumber("+1234567890")
                .build();

        loginRequest = LoginRequest.builder()
                .username("test@example.com")
                .password("password123")
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/register - Success returns 201")
    void register_Success() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .message("User registered successfully")
                .userId(1L)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("POST /api/auth/register - Duplicate username returns 409")
    void register_DuplicateUsername() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateResourceException("User", "username", "test@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/auth/register - Invalid email returns 400")
    void register_InvalidEmail() throws Exception {
        registerRequest.setUsername("invalid-email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login - Success returns 200")
    void login_Success() throws Exception {
        doNothing().when(authService).login(any(LoginRequest.class), any(HttpServletRequest.class));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Missing password returns 400")
    void login_MissingPassword() throws Exception {
        loginRequest.setPassword(null);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }
}
