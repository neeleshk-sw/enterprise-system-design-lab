package com.enterprise.cleanarch.application;

import com.enterprise.cleanarch.application.port.in.CancelOrderUseCase;
import com.enterprise.cleanarch.application.port.in.OrderQueryUseCase;
import com.enterprise.cleanarch.application.port.in.PlaceOrderCommand;
import com.enterprise.cleanarch.application.port.in.PlaceOrderUseCase;
import com.enterprise.cleanarch.application.port.out.OrderRepositoryPort;
import com.enterprise.cleanarch.domain.Order;
import com.enterprise.cleanarch.domain.OrderItem;

import java.util.List;

/**
 * Use-case implementation orchestrating the {@link Order} domain through the
 * {@link OrderRepositoryPort}. Deliberately framework-free (no Spring annotations);
 * it is wired as a bean in {@code config.UseCaseConfig}.
 */
public class OrderService implements PlaceOrderUseCase, OrderQueryUseCase, CancelOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    public OrderService(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order placeOrder(PlaceOrderCommand command) {
        List<OrderItem> items = command.items().stream()
                .map(i -> new OrderItem(i.sku(), i.quantity(), i.unitPrice()))
                .toList();
        Order order = Order.place(command.customerReference(), items);
        return orderRepository.save(order);
    }

    @Override
    public Order getById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order cancel(Long id) {
        Order order = getById(id);
        order.cancel();
        return orderRepository.save(order);
    }
}
