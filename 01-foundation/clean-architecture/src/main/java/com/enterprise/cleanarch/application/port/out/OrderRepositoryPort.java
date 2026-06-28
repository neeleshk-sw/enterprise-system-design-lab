package com.enterprise.cleanarch.application.port.out;

import com.enterprise.cleanarch.domain.Order;

import java.util.List;
import java.util.Optional;

/**
 * Output port: persistence gateway for orders. Implemented by an adapter; the
 * application depends only on this interface, never on JPA/Spring.
 */
public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(Long id);

    List<Order> findAll();
}
