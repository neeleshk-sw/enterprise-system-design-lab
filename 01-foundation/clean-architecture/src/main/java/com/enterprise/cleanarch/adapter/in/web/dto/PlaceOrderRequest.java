package com.enterprise.cleanarch.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Inbound payload for placing an order.
 */
public record PlaceOrderRequest(

        @NotBlank
        @Size(max = 100)
        String customerReference,

        @NotEmpty
        @Valid
        List<OrderItemRequest> items) {
}
