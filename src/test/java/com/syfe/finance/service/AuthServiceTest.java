package com.syfe.finance.service;

import com.syfe.finance.dto.AuthResponse;
import com.syfe.finance.dto.LoginRequest;
import com.syfe.finance.dto.RegisterRequest;
import com.syfe.finance.entity.User;
import com.syfe.finance.exception.DuplicateResourceException;
import com.syfe.finance.exception.ResourceNotFoundException;
import com.syfe.finance.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpSession httpSession;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

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

        user = User.builder()
                .id(1L)
                .username("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .phoneNumber("+1234567890")
                .build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void register_Success() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("User registered successfully", response.getMessage());
        assertEquals(1L, response.getUserId());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(registerRequest.getPassword());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when username exists")
    void register_DuplicateUsername() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should login user successfully")
    void login_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(httpRequest.getSession(true)).thenReturn(httpSession);

        assertDoesNotThrow(() -> authService.login(loginRequest, httpRequest));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should throw BadCredentialsException for invalid credentials")
    void login_InvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest, httpRequest));
    }

    @Test
    @DisplayName("Should get current user successfully")
    void getCurrentUser_Success() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));

        User result = authService.getCurrentUser();

        assertNotNull(result);
        assertEquals("test@example.com", result.getUsername());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void getCurrentUser_UserNotFound() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("unknown@example.com");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsername("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getCurrentUser());
    }

    @Test
    @DisplayName("Should get user by username successfully")
    void getUserByUsername_Success() {
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));

        User result = authService.getUserByUsername("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getUsername());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when getting user by unknown username")
    void getUserByUsername_NotFound() {
        when(userRepository.findByUsername("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> authService.getUserByUsername("unknown@example.com"));
    }
}
