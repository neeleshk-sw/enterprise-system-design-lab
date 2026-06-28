package com.enterprise.cleanarch.application.port.in;

import com.enterprise.cleanarch.domain.Order;

/**
 * Input port: place a new order.
 */
public interface PlaceOrderUseCase {

    Order placeOrder(PlaceOrderCommand command);
}
