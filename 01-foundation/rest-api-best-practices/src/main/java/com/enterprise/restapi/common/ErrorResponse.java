package com.enterprise.restapi.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Unified error envelope.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        String errorCode,
        String message,
        Instant timestamp,
        String path,
        List<String> errors) {

    public static ErrorResponse of(String errorCode, String message, String path) {
        return new ErrorResponse(false, errorCode, message, Instant.now(), path, null);
    }

    public static ErrorResponse of(String errorCode, String message, String path, List<String> errors) {
        return new ErrorResponse(false, errorCode, message, Instant.now(), path,
                (errors == null || errors.isEmpty()) ? null : errors);
    }
}
