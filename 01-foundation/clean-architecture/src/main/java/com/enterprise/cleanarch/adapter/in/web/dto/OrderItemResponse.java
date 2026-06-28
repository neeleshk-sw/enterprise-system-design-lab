package com.enterprise.cleanarch.adapter.in.web.dto;

import java.math.BigDecimal;

/**
 * Outbound line item.
 */
public record OrderItemResponse(
        String sku,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {
}
