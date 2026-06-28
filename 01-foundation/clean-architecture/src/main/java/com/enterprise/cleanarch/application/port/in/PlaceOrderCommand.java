package com.enterprise.cleanarch.application.port.in;

import java.math.BigDecimal;
import java.util.List;

/**
 * Input data for placing an order. Pure application type — no framework dependencies.
 */
public record PlaceOrderCommand(String customerReference, List<Item> items) {

    public record Item(String sku, int quantity, BigDecimal unitPrice) {
    }
}
