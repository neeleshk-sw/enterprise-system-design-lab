package com.enterprise.cleanarch.application.port.in;

import com.enterprise.cleanarch.domain.Order;

import java.util.List;

/**
 * Input port: read orders.
 */
public interface OrderQueryUseCase {

    Order getById(Long id);

    List<Order> findAll();
}
