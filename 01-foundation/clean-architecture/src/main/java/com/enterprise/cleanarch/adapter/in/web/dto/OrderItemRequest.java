package com.enterprise.cleanarch.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Inbound line item for placing an order.
 */
public record OrderItemRequest(

        @NotBlank
        @Size(max = 64)
        String sku,

        @Positive
        int quantity,

        @NotNull
        @Positive
        BigDecimal unitPrice) {
}
