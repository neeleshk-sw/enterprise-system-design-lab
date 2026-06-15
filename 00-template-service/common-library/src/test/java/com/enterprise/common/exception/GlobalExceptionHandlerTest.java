package com.enterprise.common.exception;

import com.enterprise.common.constant.Constants;
import com.enterprise.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private HttpServletRequest requestFor(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }

    @Test
    void resourceNotFoundMapsTo404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBase(new ResourceNotFoundException("Customer not found with id: '42'"),
                        requestFor("/api/v1/customers/42"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isFalse();
        assertThat(body.errorCode()).isEqualTo(Constants.RESOURCE_NOT_FOUND);
        assertThat(body.path()).isEqualTo("/api/v1/customers/42");
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void businessExceptionMapsTo409() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBase(new BusinessException("Customer already exists"),
                        requestFor("/api/v1/customers"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().errorCode()).isEqualTo(Constants.BUSINESS_RULE_VIOLATION);
    }

    @Test
    void validationExceptionMapsTo400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBase(new ValidationException("age must be positive"),
                        requestFor("/api/v1/customers"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().errorCode()).isEqualTo(Constants.VALIDATION_ERROR);
    }

    @Test
    void customErrorCodeIsPreserved() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBase(new ResourceNotFoundException("CUSTOMER_NOT_FOUND", "no customer"),
                        requestFor("/api/v1/customers/9"));

        assertThat(response.getBody().errorCode()).isEqualTo("CUSTOMER_NOT_FOUND");
    }

    @Test
    void methodArgumentNotValidAggregatesFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("customerRequest", "email", "must not be blank"),
                new FieldError("customerRequest", "name", "must not be null")));

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodArgumentNotValid(ex, requestFor("/api/v1/customers"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body.errorCode()).isEqualTo(Constants.VALIDATION_ERROR);
        assertThat(body.errors()).hasSize(2);
        assertThat(body.errors()).extracting(ErrorResponse.FieldError::field)
                .containsExactly("email", "name");
    }

    @Test
    void unexpectedExceptionMapsTo500AndHidesDetail() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnexpected(new IllegalStateException("boom — internal detail"),
                        requestFor("/api/v1/customers"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse body = response.getBody();
        assertThat(body.errorCode()).isEqualTo(Constants.INTERNAL_ERROR);
        assertThat(body.message()).doesNotContain("boom");
    }
}
