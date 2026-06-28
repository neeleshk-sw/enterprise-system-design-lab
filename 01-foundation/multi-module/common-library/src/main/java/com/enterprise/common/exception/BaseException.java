package com.enterprise.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Root of the application's custom exception hierarchy.
 *
 * <p>Each exception carries the HTTP {@link HttpStatus} it maps to and a stable,
 * machine-readable {@code errorCode}, so the {@link GlobalExceptionHandler} can render
 * a uniform error response without type-specific branching.
 */
@Getter
public abstract class BaseException extends RuntimeException {

    private final transient HttpStatus status;
    private final String errorCode;

    protected BaseException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    protected BaseException(HttpStatus status, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }
}
