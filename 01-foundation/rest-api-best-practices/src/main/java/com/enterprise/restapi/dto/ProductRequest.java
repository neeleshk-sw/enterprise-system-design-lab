package com.enterprise.restapi.dto;

import com.enterprise.restapi.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Inbound payload for creating or updating a product.
 */
public record ProductRequest(

        @NotBlank
        @Size(max = 200)
        String name,

        @NotBlank
        @Size(max = 64)
        String sku,

        @Size(max = 1000)
        String description,

        @NotNull
        @Positive
        BigDecimal price,

        ProductStatus status) {
}
