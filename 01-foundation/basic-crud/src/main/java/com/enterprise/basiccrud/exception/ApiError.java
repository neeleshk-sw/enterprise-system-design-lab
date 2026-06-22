package com.enterprise.basiccrud.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Plain error body for this service: {@code {timestamp, status, error, message, path}}.
 * The optional {@code errors} list carries per-field detail for validation failures.
 * (Deliberately simpler than the template's unified ErrorResponse envelope.)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> errors) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, null);
    }

    public static ApiError of(int status, String error, String message, String path, List<String> errors) {
        return new ApiError(Instant.now(), status, error, message, path,
                (errors == null || errors.isEmpty()) ? null : errors);
    }
}
