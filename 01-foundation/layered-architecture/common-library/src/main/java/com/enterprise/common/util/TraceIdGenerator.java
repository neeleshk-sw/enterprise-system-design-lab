package com.enterprise.common.util;

import java.util.UUID;

/**
 * Generates short, opaque trace identifiers for correlating logs within a request.
 */
public final class TraceIdGenerator {

    private TraceIdGenerator() {
        // utility holder — not instantiable
    }

    /** A 16-character hex id derived from a random UUID. */
    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
