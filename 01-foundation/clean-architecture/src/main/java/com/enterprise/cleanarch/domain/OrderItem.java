package com.enterprise.cleanarch.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A line item within an {@link Order}. Immutable value object; pure domain.
 */
public final class OrderItem {

    private final String productSku;
    private final int quantity;
    private final BigDecimal unitPrice;

    public OrderItem(String productSku, int quantity, BigDecimal unitPrice) {
        if (productSku == null || productSku.isBlank()) {
            throw new IllegalArgumentException("Product SKU must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        this.productSku = productSku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    /** Line total = unit price × quantity. */
    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getProductSku() {
        return productSku;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderItem other)) {
            return false;
        }
        return quantity == other.quantity
                && productSku.equals(other.productSku)
                && unitPrice.compareTo(other.unitPrice) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(productSku, quantity, unitPrice.stripTrailingZeros());
    }
}
