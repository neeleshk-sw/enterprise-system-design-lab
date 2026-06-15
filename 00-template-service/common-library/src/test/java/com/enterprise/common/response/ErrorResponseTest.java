package com.enterprise.common.response;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void ofWithoutFieldErrors() {
        ErrorResponse response = ErrorResponse.of("CUSTOMER_NOT_FOUND", "Customer not found", "/api/v1/customers/42");

        assertThat(response.success()).isFalse();
        assertThat(response.errorCode()).isEqualTo("CUSTOMER_NOT_FOUND");
        assertThat(response.message()).isEqualTo("Customer not found");
        assertThat(response.path()).isEqualTo("/api/v1/customers/42");
        assertThat(response.timestamp()).isNotNull();
        assertThat(response.errors()).isNull();
    }

    @Test
    void emptyFieldErrorsCollapseToNull() {
        ErrorResponse response = ErrorResponse.of("VALIDATION_ERROR", "bad", "/x", List.of());

        assertThat(response.errors()).isNull();
    }

    @Test
    void ofWithFieldErrorsKeepsThem() {
        ErrorResponse response = ErrorResponse.of(
                "VALIDATION_ERROR", "Validation failed", "/api/v1/customers",
                List.of(new ErrorResponse.FieldError("email", "must not be blank")));

        assertThat(response.errors()).hasSize(1);
        assertThat(response.errors().get(0).field()).isEqualTo("email");
        assertThat(response.errors().get(0).message()).isEqualTo("must not be blank");
    }
}
