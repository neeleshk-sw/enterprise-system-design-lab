package com.enterprise.cleanarch.application;

import com.enterprise.cleanarch.application.port.in.PlaceOrderCommand;
import com.enterprise.cleanarch.application.port.out.OrderRepositoryPort;
import com.enterprise.cleanarch.domain.Order;
import com.enterprise.cleanarch.domain.OrderItem;
import com.enterprise.cleanarch.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepositoryPort repository;

    private OrderService service() {
        return new OrderService(repository);
    }

    private PlaceOrderCommand command() {
        return new PlaceOrderCommand("CUST-1",
                List.of(new PlaceOrderCommand.Item("SKU-1", 2, new BigDecimal("10.00"))));
    }

    private Order existingOrder(OrderStatus status) {
        return new Order(1L, "CUST-1",
                List.of(new OrderItem("SKU-1", 2, new BigDecimal("10.00"))), status);
    }

    @Test
    void placeOrderBuildsDomainAndSaves() {
        when(repository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.assignId(1L);
            return o;
        });

        Order result = service().placeOrder(command());

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(result.totalAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().getById(99L)).isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void cancelLoadsMutatesAndSaves() {
        when(repository.findById(1L)).thenReturn(Optional.of(existingOrder(OrderStatus.NEW)));
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = service().cancel(1L);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(repository).save(any(Order.class));
    }
}
