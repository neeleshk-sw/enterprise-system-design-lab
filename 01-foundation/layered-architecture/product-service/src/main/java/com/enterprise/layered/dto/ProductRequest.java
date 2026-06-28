package com.enterprise.layered.dto;

import com.enterprise.layered.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Inbound payload for creating or updating a product.
 *
 * <p>{@code status} is optional; on create it defaults to {@code ACTIVE}.
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

        ProductStatus status,

        @NotNull
        Long categoryId) {
}
