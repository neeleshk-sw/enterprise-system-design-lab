package com.enterprise.cleanarch.adapter.in.web.response;

/**
 * Success envelope for the web adapter. Local to the adapter — this project does not
 * share the lab's common-library, to keep the clean-architecture example self-contained.
 *
 * @param <T> wrapped payload type
 */
public record ApiResponse<T>(boolean success, T data, String message) {

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }
}
