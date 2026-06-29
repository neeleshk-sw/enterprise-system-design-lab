package com.enterprise.restapi.dto;

import com.enterprise.restapi.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Outbound representation of a product. {@code version} backs the ETag for conditional GETs.
 */
public record ProductResponse(
        Long id,
        String name,
        String sku,
        String description,
        BigDecimal price,
        ProductStatus status,
        Long version,
        Instant createdAt,
        Instant updatedAt) {
}
