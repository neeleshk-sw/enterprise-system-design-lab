package com.enterprise.cleanarch.adapter.out.persistence;

import com.enterprise.cleanarch.domain.Order;
import com.enterprise.cleanarch.domain.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Translates between the {@link Order} domain model and the JPA persistence model.
 */
@Component
class OrderPersistenceMapper {

    OrderJpaEntity toNewJpa(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setCustomerReference(order.getCustomerReference());
        entity.setStatus(order.getStatus());
        order.getItems().forEach(item -> {
            OrderItemJpaEntity jpaItem = new OrderItemJpaEntity();
            jpaItem.setProductSku(item.getProductSku());
            jpaItem.setQuantity(item.getQuantity());
            jpaItem.setUnitPrice(item.getUnitPrice());
            entity.addItem(jpaItem);
        });
        return entity;
    }

    Order toDomain(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems().stream()
                .map(i -> new OrderItem(i.getProductSku(), i.getQuantity(), i.getUnitPrice()))
                .toList();
        return new Order(entity.getId(), entity.getCustomerReference(), items, entity.getStatus());
    }
}
