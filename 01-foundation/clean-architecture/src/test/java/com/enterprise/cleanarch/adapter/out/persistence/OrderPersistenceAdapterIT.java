package com.enterprise.cleanarch.adapter.out.persistence;

import com.enterprise.cleanarch.application.port.out.OrderRepositoryPort;
import com.enterprise.cleanarch.domain.Order;
import com.enterprise.cleanarch.domain.OrderItem;
import com.enterprise.cleanarch.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the persistence adapter against a real PostgreSQL (Testcontainers).
 * Verifies the domain ↔ JPA mapping round-trips and that Flyway/validate agree.
 */
@DataJpaTest
@Testcontainers
@Import({OrderPersistenceAdapter.class, OrderPersistenceMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderPersistenceAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private OrderRepositoryPort port;

    private Order newOrder() {
        return Order.place("CUST-1", List.of(
                new OrderItem("SKU-1", 2, new BigDecimal("10.00")),
                new OrderItem("SKU-2", 1, new BigDecimal("5.50"))));
    }

    @Test
    void savesAndReloadsDomainOrder() {
        Order saved = port.save(newOrder());
        assertThat(saved.getId()).isNotNull();

        Optional<Order> found = port.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCustomerReference()).isEqualTo("CUST-1");
        assertThat(found.get().getItems()).hasSize(2);
        assertThat(found.get().totalAmount()).isEqualByComparingTo("25.50");
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    void cancelRoundTripsThroughPersistence() {
        Order saved = port.save(newOrder());

        Order toCancel = port.findById(saved.getId()).orElseThrow();
        toCancel.cancel();
        port.save(toCancel);

        assertThat(port.findById(saved.getId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void findAllReturnsSavedOrders() {
        port.save(newOrder());

        assertThat(port.findAll()).isNotEmpty();
    }
}
