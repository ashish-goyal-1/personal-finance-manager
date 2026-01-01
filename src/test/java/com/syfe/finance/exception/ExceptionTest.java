package com.syfe.finance.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExceptionTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("ResourceNotFoundException - all constructors")
    void testResourceNotFoundException() {
        ResourceNotFoundException ex1 = new ResourceNotFoundException("Resource not found");
        assertEquals("Resource not found", ex1.getMessage());

        ResourceNotFoundException ex2 = new ResourceNotFoundException("User", 123L);
        assertTrue(ex2.getMessage().contains("User"));
        assertTrue(ex2.getMessage().contains("123"));

        ResourceNotFoundException ex3 = new ResourceNotFoundException("User", "email", "test@example.com");
        assertTrue(ex3.getMessage().contains("email"));
        assertTrue(ex3.getMessage().contains("test@example.com"));
    }

    @Test
    @DisplayName("DuplicateResourceException - all constructors")
    void testDuplicateResourceException() {
        DuplicateResourceException ex1 = new DuplicateResourceException("Already exists");
        assertEquals("Already exists", ex1.getMessage());

        DuplicateResourceException ex2 = new DuplicateResourceException("User", "email", "test@example.com");
        assertTrue(ex2.getMessage().contains("User"));
        assertTrue(ex2.getMessage().contains("email"));
    }

    @Test
    @DisplayName("UnauthorizedAccessException - all constructors")
    void testUnauthorizedAccessException() {
        UnauthorizedAccessException ex1 = new UnauthorizedAccessException("Access denied");
        assertEquals("Access denied", ex1.getMessage());

        UnauthorizedAccessException ex2 = new UnauthorizedAccessException("Transaction", 123L);
        assertTrue(ex2.getMessage().contains("Transaction"));
        assertTrue(ex2.getMessage().contains("123"));
    }

    @Test
    @DisplayName("ValidationException - constructor")
    void testValidationException() {
        ValidationException ex = new ValidationException("Invalid data");
        assertEquals("Invalid data", ex.getMessage());
    }

    @Test
    @DisplayName("ErrorResponse - getters, setters, builder")
    void testErrorResponse() {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(404);
        response.setMessage("Not found");

        assertEquals(404, response.getStatus());
        assertEquals("Not found", response.getMessage());

        ErrorResponse response2 = ErrorResponse.builder()
                .status(400)
                .message("Bad request")
                .build();
        assertEquals(400, response2.getStatus());
        assertNotNull(response2.toString());

        ErrorResponse response3 = new ErrorResponse(500, "Error");
        assertEquals(500, response3.getStatus());
    }

    @Test
    @DisplayName("GlobalExceptionHandler - handles ResourceNotFoundException")
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    @DisplayName("GlobalExceptionHandler - handles DuplicateResourceException")
    void testHandleDuplicateResourceException() {
        DuplicateResourceException ex = new DuplicateResourceException("Duplicate");
        ResponseEntity<ErrorResponse> response = handler.handleDuplicateResourceException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    @DisplayName("GlobalExceptionHandler - handles UnauthorizedAccessException")
    void testHandleUnauthorizedAccessException() {
        UnauthorizedAccessException ex = new UnauthorizedAccessException("Forbidden");
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorizedAccessException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getStatus());
    }

    @Test
    @DisplayName("GlobalExceptionHandler - handles ValidationException")
    void testHandleValidationException() {
        ValidationException ex = new ValidationException("Invalid");
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    @DisplayName("GlobalExceptionHandler - handles BadCredentialsException")
    void testHandleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        ResponseEntity<ErrorResponse> response = handler.handleBadCredentialsException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody().getMessage());
    }

    @Test
    @DisplayName("GlobalExceptionHandler - handles AuthenticationException")
    void testHandleAuthenticationException() {
        AuthenticationException ex = mock(AuthenticationException.class);
        ResponseEntity<ErrorResponse> response = handler.handleAuthenticationException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("GlobalExceptionHandler - handles IllegalArgumentException")
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("GlobalExceptionHandler - handles generic Exception")
    void testHandleGenericException() {
        Exception ex = new Exception("Unexpected error");
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    @DisplayName("GlobalExceptionHandler - handles MethodArgumentNotValidException")
    void testHandleMethodArgumentNotValidException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "field1", "must not be null");
        FieldError fieldError2 = new FieldError("object", "field2", "must be valid");

        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValidException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("must not be null"));
    }

    @Test
    @DisplayName("GlobalExceptionHandler - handles HttpMessageNotReadableException")
    void testHandleHttpMessageNotReadableException() {
        org.springframework.http.converter.HttpMessageNotReadableException ex = mock(
                org.springframework.http.converter.HttpMessageNotReadableException.class);

        ResponseEntity<ErrorResponse> response = handler.handleJsonParseExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request format or value", response.getBody().getMessage());
    }
}
