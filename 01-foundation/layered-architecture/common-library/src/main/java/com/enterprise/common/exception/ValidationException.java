package com.enterprise.common.exception;

import com.enterprise.common.constant.Constants;
import org.springframework.http.HttpStatus;

/**
 * Thrown for manual / business-level validation failures not covered by Bean Validation.
 * Maps to HTTP 400 (Bad Request).
 */
public class ValidationException extends BaseException {

    public ValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, Constants.VALIDATION_ERROR, message);
    }

    public ValidationException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, errorCode, message);
    }
}
