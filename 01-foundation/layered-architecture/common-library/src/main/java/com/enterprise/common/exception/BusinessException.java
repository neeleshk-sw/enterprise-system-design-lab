package com.enterprise.common.exception;

import com.enterprise.common.constant.Constants;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a business rule / invariant is violated. Maps to HTTP 409 (Conflict).
 */
public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(HttpStatus.CONFLICT, Constants.BUSINESS_RULE_VIOLATION, message);
    }

    /** Allows a domain-specific error code. */
    public BusinessException(String errorCode, String message) {
        super(HttpStatus.CONFLICT, errorCode, message);
    }
}
