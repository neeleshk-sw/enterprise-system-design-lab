package com.enterprise.cleanarch.application;

/**
 * Raised by use-cases when an order does not exist. Framework-free; the web adapter
 * maps it to HTTP 404.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long id) {
        super("Order not found with id: " + id);
    }
}
