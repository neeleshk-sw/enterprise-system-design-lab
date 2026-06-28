package com.enterprise.layered.dto;

import com.enterprise.layered.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Outbound representation of a product, including its category reference.
 */
public record ProductResponse(
        Long id,
        String name,
        String sku,
        String description,
        BigDecimal price,
        ProductStatus status,
        Long categoryId,
        String categoryName,
        Instant createdAt,
        Instant updatedAt) {
}
