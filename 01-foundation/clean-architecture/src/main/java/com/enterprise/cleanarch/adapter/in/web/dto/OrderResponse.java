package com.enterprise.cleanarch.adapter.in.web.dto;

import com.enterprise.cleanarch.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Outbound representation of an order.
 */
public record OrderResponse(
        Long id,
        String customerReference,
        OrderStatus status,
        List<OrderItemResponse> items,
        BigDecimal totalAmount) {
}
