package com.enterprise.cleanarch.adapter.out.persistence;

import com.enterprise.cleanarch.application.port.out.OrderRepositoryPort;
import com.enterprise.cleanarch.domain.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter implementing the {@link OrderRepositoryPort} output port.
 * Transactions live here (an infrastructure concern); the application layer stays pure.
 */
@Component
@Transactional
class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final SpringDataOrderRepository repository;
    private final OrderPersistenceMapper mapper;

    OrderPersistenceAdapter(SpringDataOrderRepository repository, OrderPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        if (order.getId() == null) {
            OrderJpaEntity saved = repository.save(mapper.toNewJpa(order));
            order.assignId(saved.getId());
            return mapper.toDomain(saved);
        }
        // Existing aggregate: update mutable state (status) on the managed entity.
        OrderJpaEntity entity = repository.findById(order.getId())
                .orElseGet(() -> mapper.toNewJpa(order));
        entity.setStatus(order.getStatus());
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }
}
