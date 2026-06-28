package com.enterprise.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Unified error envelope returned for every failed request.
 *
 * <pre>
 * {
 *   "success": false,
 *   "errorCode": "CUSTOMER_NOT_FOUND",
 *   "message": "Customer not found",
 *   "timestamp": "2026-06-11T10:15:30Z",
 *   "path": "/api/v1/customers/42"
 * }
 * </pre>
 *
 * <p>For validation failures an optional {@code errors} array carries per-field detail.
 * Null fields are omitted from the JSON ({@code success} stays present because {@code false}
 * is not null).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        String errorCode,
        String message,
        Instant timestamp,
        String path,
        List<FieldError> errors) {

    /** Per-field validation detail. */
    public record FieldError(String field, String message) {
    }

    /** Build an error without field-level detail. */
    public static ErrorResponse of(String errorCode, String message, String path) {
        return new ErrorResponse(false, errorCode, message, Instant.now(), path, null);
    }

    /** Build an error with optional field-level detail (empty/null collapses to no array). */
    public static ErrorResponse of(String errorCode, String message, String path, List<FieldError> errors) {
        List<FieldError> normalized = (errors == null || errors.isEmpty()) ? null : List.copyOf(errors);
        return new ErrorResponse(false, errorCode, message, Instant.now(), path, normalized);
    }
}
