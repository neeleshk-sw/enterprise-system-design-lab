package com.enterprise.common.response;

/**
 * Standard success envelope wrapping every successful API payload.
 *
 * <pre>
 * {
 *   "success": true,
 *   "data": { ... },
 *   "message": "Customer created successfully"
 * }
 * </pre>
 *
 * @param <T> type of the wrapped payload
 */
public record ApiResponse<T>(boolean success, T data, String message) {

    /** Success response carrying both a payload and a human-readable message. */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    /** Success response carrying only a payload. */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /** Success response carrying only a message (e.g. for deletes / acknowledgements). */
    public static <T> ApiResponse<T> message(String message) {
        return new ApiResponse<>(true, null, message);
    }
}
