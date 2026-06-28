package com.enterprise.cleanarch.domain;

/**
 * Lifecycle state of an {@link Order}. Pure domain — no framework dependencies.
 */
public enum OrderStatus {
    NEW,
    PAID,
    SHIPPED,
    CANCELLED
}
