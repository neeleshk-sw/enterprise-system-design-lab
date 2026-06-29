package com.enterprise.restapi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base for application exceptions; carries the HTTP status and a stable error code so the
 * handler can render a uniform {@code ErrorResponse}.
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final transient HttpStatus status;
    private final String errorCode;

    protected ApiException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
