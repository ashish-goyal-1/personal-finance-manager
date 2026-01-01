package com.syfe.finance.controller;

import com.syfe.finance.dto.AuthResponse;
import com.syfe.finance.dto.LoginRequest;
import com.syfe.finance.dto.MessageResponse;
import com.syfe.finance.dto.RegisterRequest;
import com.syfe.finance.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller handling user authentication and registration.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user with the provided details.
     *
     * @param request the registration request containing user details
     * @return the authentication response with user ID
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user and establishes a session.
     *
     * @param request     the login request containing credentials
     * @param httpRequest the HTTP request to establish session
     * @return the response message
     */
    /**
     * Authenticates a user and establishes a session.
     *
     * @param request     the login request containing credentials
     * @param httpRequest the HTTP request to establish session
     * @return the response message
     */
    @PostMapping("/login")
    public ResponseEntity<MessageResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        authService.login(request, httpRequest);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Login successful")
                .build());
    }

    // Logout is handled by SecurityConfig, but we can add a placeholder if needed
    // The actual logout logic is in SecurityConfig.filterChain()
}
