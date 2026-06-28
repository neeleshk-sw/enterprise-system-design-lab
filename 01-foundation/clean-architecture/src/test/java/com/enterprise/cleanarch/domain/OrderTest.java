package com.enterprise.cleanarch.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private List<OrderItem> twoItems() {
        return List.of(
                new OrderItem("SKU-1", 2, new BigDecimal("10.00")),
                new OrderItem("SKU-2", 1, new BigDecimal("5.50")));
    }

    @Test
    void placeCreatesNewOrderAndComputesTotal() {
        Order order = Order.place("CUST-1", twoItems());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(order.getId()).isNull();
        assertThat(order.totalAmount()).isEqualByComparingTo("25.50");
    }

    @Test
    void placeRejectsEmptyItems() {
        assertThatThrownBy(() -> Order.place("CUST-1", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancelTransitionsNewToCancelled() {
        Order order = Order.place("CUST-1", twoItems());

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelRejectsShippedOrder() {
        Order shipped = new Order(1L, "CUST-1", twoItems(), OrderStatus.SHIPPED);

        assertThatThrownBy(shipped::cancel).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void orderItemRejectsNonPositiveQuantity() {
        assertThatThrownBy(() -> new OrderItem("SKU-1", 0, new BigDecimal("10.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
