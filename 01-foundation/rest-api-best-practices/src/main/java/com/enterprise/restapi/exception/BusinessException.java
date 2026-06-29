package com.enterprise.restapi.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a business rule is violated. Maps to HTTP 409 (Conflict).
 */
public class BusinessException extends ApiException {

    public BusinessException(String errorCode, String message) {
        super(HttpStatus.CONFLICT, errorCode, message);
    }
}
