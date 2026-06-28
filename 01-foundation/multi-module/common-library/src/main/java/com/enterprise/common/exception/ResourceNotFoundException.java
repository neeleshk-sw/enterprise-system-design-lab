package com.enterprise.common.exception;

import com.enterprise.common.constant.Constants;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource does not exist. Maps to HTTP 404.
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, Constants.RESOURCE_NOT_FOUND, message);
    }

    /** Allows a domain-specific error code (e.g. {@code CUSTOMER_NOT_FOUND}). */
    public ResourceNotFoundException(String errorCode, String message) {
        super(HttpStatus.NOT_FOUND, errorCode, message);
    }

    /** Convenience factory: {@code Customer not found with id: '42'}. */
    public static ResourceNotFoundException of(String resource, String field, Object value) {
        return new ResourceNotFoundException(
                String.format("%s not found with %s: '%s'", resource, field, value));
    }
}
