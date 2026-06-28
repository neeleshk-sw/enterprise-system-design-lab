package com.enterprise.cleanarch.application.port.in;

import com.enterprise.cleanarch.domain.Order;

/**
 * Input port: cancel an existing order.
 */
public interface CancelOrderUseCase {

    Order cancel(Long id);
}
