package com.enterprise.cleanarch.adapter.in.web;

import com.enterprise.cleanarch.adapter.in.web.dto.OrderItemRequest;
import com.enterprise.cleanarch.adapter.in.web.dto.PlaceOrderRequest;
import com.enterprise.cleanarch.application.OrderNotFoundException;
import com.enterprise.cleanarch.application.port.in.CancelOrderUseCase;
import com.enterprise.cleanarch.application.port.in.OrderQueryUseCase;
import com.enterprise.cleanarch.application.port.in.PlaceOrderUseCase;
import com.enterprise.cleanarch.domain.Order;
import com.enterprise.cleanarch.domain.OrderItem;
import com.enterprise.cleanarch.domain.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({GlobalExceptionHandler.class, OrderWebMapper.class})
class OrderControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlaceOrderUseCase placeOrderUseCase;
    @MockitoBean
    private OrderQueryUseCase orderQueryUseCase;
    @MockitoBean
    private CancelOrderUseCase cancelOrderUseCase;

    private Order order(OrderStatus status) {
        return new Order(1L, "CUST-1",
                List.of(new OrderItem("SKU-1", 2, new BigDecimal("10.00"))), status);
    }

    private PlaceOrderRequest validRequest() {
        return new PlaceOrderRequest("CUST-1", List.of(new OrderItemRequest("SKU-1", 2, new BigDecimal("10.00"))));
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @Test
    void placeReturns201WithEnvelope() throws Exception {
        when(placeOrderUseCase.placeOrder(any())).thenReturn(order(OrderStatus.NEW));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("NEW"))
                .andExpect(jsonPath("$.data.totalAmount").value(20.00))
                .andExpect(jsonPath("$.message").value("Order placed successfully"));
    }

    @Test
    void placeWithNoItemsReturns400() throws Exception {
        PlaceOrderRequest invalid = new PlaceOrderRequest("CUST-1", List.of());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void getByIdReturns200() throws Exception {
        when(orderQueryUseCase.getById(1L)).thenReturn(order(OrderStatus.NEW));

        mockMvc.perform(get("/api/v1/orders/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getByIdNotFoundReturns404() throws Exception {
        when(orderQueryUseCase.getById(99L)).thenThrow(new OrderNotFoundException(99L));

        mockMvc.perform(get("/api/v1/orders/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ORDER_NOT_FOUND"));
    }

    @Test
    void listReturns200() throws Exception {
        when(orderQueryUseCase.findAll()).thenReturn(List.of(order(OrderStatus.NEW)));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void cancelReturns200() throws Exception {
        when(cancelOrderUseCase.cancel(1L)).thenReturn(order(OrderStatus.CANCELLED));

        mockMvc.perform(post("/api/v1/orders/{id}/cancel", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void cancelShippedReturns409() throws Exception {
        when(cancelOrderUseCase.cancel(1L)).thenThrow(new IllegalStateException("Cannot cancel an order that has shipped"));

        mockMvc.perform(post("/api/v1/orders/{id}/cancel", 1))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ORDER_STATE"));
    }
}
