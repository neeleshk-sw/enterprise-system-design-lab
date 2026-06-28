package com.enterprise.cleanarch.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Order aggregate root. Holds business rules and invariants; pure domain — no Spring,
 * no JPA. Identity is assigned by the persistence adapter after the first save.
 */
public class Order {

    private Long id;
    private final String customerReference;
    private final List<OrderItem> items;
    private OrderStatus status;

    public Order(Long id, String customerReference, List<OrderItem> items, OrderStatus status) {
        if (customerReference == null || customerReference.isBlank()) {
            throw new IllegalArgumentException("Customer reference must not be blank");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        this.id = id;
        this.customerReference = customerReference;
        this.items = List.copyOf(items);
        this.status = status;
    }

    /** Factory for a brand-new order (status {@code NEW}, no id yet). */
    public static Order place(String customerReference, List<OrderItem> items) {
        return new Order(null, customerReference, items, OrderStatus.NEW);
    }

    /** Cancel the order. Shipped or already-cancelled orders cannot be cancelled. */
    public void cancel() {
        if (status == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Cannot cancel an order that has shipped");
        }
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }

    /** Total amount across all line items. */
    public BigDecimal totalAmount() {
        return items.stream().map(OrderItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Assigned by the persistence adapter after the order is first stored. */
    public void assignId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
