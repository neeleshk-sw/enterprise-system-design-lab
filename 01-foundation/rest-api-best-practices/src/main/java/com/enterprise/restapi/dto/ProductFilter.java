package com.enterprise.restapi.dto;

import com.enterprise.restapi.entity.ProductStatus;

import java.math.BigDecimal;

/**
 * Optional query-parameter filters for listing products. Bound from the request query
 * string (e.g. {@code ?name=widget&status=ACTIVE&minPrice=10&maxPrice=100}); any field
 * may be null.
 */
public record ProductFilter(
        String name,
        ProductStatus status,
        BigDecimal minPrice,
        BigDecimal maxPrice) {
}
