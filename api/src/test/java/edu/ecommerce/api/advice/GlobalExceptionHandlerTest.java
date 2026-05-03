package edu.ecommerce.api.advice;

import edu.ecommerce.core.exception.DuplicateEmailException;
import edu.ecommerce.core.exception.InvalidOrderTransitionException;
import edu.ecommerce.core.exception.InvalidUserDataException;
import edu.ecommerce.core.exception.OrderNotFoundException;
import edu.ecommerce.core.exception.ProductNotFoundException;
import edu.ecommerce.core.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleUserNotFound() {
        UserNotFoundException ex = new UserNotFoundException("User not found");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleUserNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
            .isNotNull()
            .satisfies(error -> {
                assertThat(error.getCode()).isEqualTo("USER_NOT_FOUND");
                assertThat(error.getMessage()).isEqualTo("User not found");
                assertThat(error.getTimestamp()).isNotNull();
            });
    }

    @Test
    void testHandleProductNotFound() {
        ProductNotFoundException ex = new ProductNotFoundException("Product not found");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleProductNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
            .isNotNull()
            .satisfies(error -> {
                assertThat(error.getCode()).isEqualTo("PRODUCT_NOT_FOUND");
                assertThat(error.getMessage()).isEqualTo("Product not found");
            });
    }

    @Test
    void testHandleDuplicateEmail() {
        DuplicateEmailException ex = new DuplicateEmailException("Email already exists");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleDuplicateEmail(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody())
            .isNotNull()
            .satisfies(error -> {
                assertThat(error.getCode()).isEqualTo("DUPLICATE_EMAIL");
                assertThat(error.getMessage()).isEqualTo("Email already exists");
            });
    }

    @Test
    void testHandleOrderNotFound() {
        OrderNotFoundException ex = new OrderNotFoundException("Order not found");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleOrderNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
            .isNotNull()
            .satisfies(error -> {
                assertThat(error.getCode()).isEqualTo("ORDER_NOT_FOUND");
                assertThat(error.getMessage()).isEqualTo("Order not found");
            });
    }

    @Test
    void testHandleInvalidOrderTransition() {
        InvalidOrderTransitionException ex = new InvalidOrderTransitionException("Invalid transition");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleInvalidOrderTransition(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
            .isNotNull()
            .satisfies(error -> {
                assertThat(error.getCode()).isEqualTo("INVALID_ORDER_TRANSITION");
                assertThat(error.getMessage()).isEqualTo("Invalid transition");
            });
    }

    @Test
    void testHandleInvalidUserData() {
        InvalidUserDataException ex = new InvalidUserDataException("Invalid user data");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleInvalidUserData(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
            .isNotNull()
            .satisfies(error -> {
                assertThat(error.getCode()).isEqualTo("INVALID_USER_DATA");
                assertThat(error.getMessage()).isEqualTo("Invalid user data");
            });
    }

    @Test
    void testHandleValidationError() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        var bindingResult = mock(org.springframework.validation.BindingResult.class);
        FieldError fieldError = new FieldError("User", "email", "Email is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleValidationError(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
            .isNotNull()
            .satisfies(error -> {
                assertThat(error.getCode()).isEqualTo("VALIDATION_ERROR");
                assertThat(error.getMessage()).contains("email");
            });
    }

    @Test
    void testHandleGenericError() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleGenericError(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody())
            .isNotNull()
            .satisfies(error -> {
                assertThat(error.getCode()).isEqualTo("INTERNAL_ERROR");
                assertThat(error.getMessage()).isEqualTo("An unexpected error occurred");
                assertThat(error.getTimestamp()).isNotNull();
            });
    }

    @Test
    void testErrorResponseFields() {
        LocalDateTime now = LocalDateTime.now();
        GlobalExceptionHandler.ErrorResponse response = new GlobalExceptionHandler.ErrorResponse(
            "TEST_CODE",
            "Test message",
            now
        );

        assertThat(response.getCode()).isEqualTo("TEST_CODE");
        assertThat(response.getMessage()).isEqualTo("Test message");
        assertThat(response.getTimestamp()).isEqualTo(now);
    }
}
