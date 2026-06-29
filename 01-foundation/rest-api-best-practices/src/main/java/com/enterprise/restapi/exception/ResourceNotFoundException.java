package com.enterprise.restapi.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a resource does not exist. Maps to HTTP 404.
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String errorCode, String message) {
        super(HttpStatus.NOT_FOUND, errorCode, message);
    }
}
