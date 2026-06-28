package com.enterprise.common.constant;

/**
 * Shared, application-agnostic constants reused across services derived from the template.
 *
 * <p>Kept deliberately small and free of domain-specific values so the library stays generic.
 */
public final class Constants {

    private Constants() {
        // utility holder — not instantiable
    }

    // --- Tracing / structured logging ---
    /** MDC key under which the per-request trace id is stored. */
    public static final String TRACE_ID = "traceId";
    /** HTTP header used to propagate / echo the trace id. */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    // --- Standard machine-readable error codes (used in ErrorResponse.errorCode) ---
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String BUSINESS_RULE_VIOLATION = "BUSINESS_RULE_VIOLATION";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
}
