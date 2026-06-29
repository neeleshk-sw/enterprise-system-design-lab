package com.enterprise.restapi.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Standard success envelope.
 *
 * @param <T> wrapped payload type
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, String message) {

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }
}
