package com.enterprise.layered.dto;

import java.time.Instant;

/**
 * Outbound representation of a category.
 */
public record CategoryResponse(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt) {
}
