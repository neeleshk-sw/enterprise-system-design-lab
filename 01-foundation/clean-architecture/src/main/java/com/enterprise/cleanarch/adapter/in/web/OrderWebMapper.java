package com.enterprise.cleanarch.adapter.in.web;

import com.enterprise.cleanarch.adapter.in.web.dto.OrderItemResponse;
import com.enterprise.cleanarch.adapter.in.web.dto.OrderResponse;
import com.enterprise.cleanarch.adapter.in.web.dto.PlaceOrderRequest;
import com.enterprise.cleanarch.application.port.in.PlaceOrderCommand;
import com.enterprise.cleanarch.domain.Order;
import org.springframework.stereotype.Component;

/**
 * Translates between web DTOs and application/domain types.
 */
@Component
class OrderWebMapper {

    PlaceOrderCommand toCommand(PlaceOrderRequest request) {
        return new PlaceOrderCommand(
                request.customerReference(),
                request.items().stream()
                        .map(i -> new PlaceOrderCommand.Item(i.sku(), i.quantity(), i.unitPrice()))
                        .toList());
    }

    OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerReference(),
                order.getStatus(),
                order.getItems().stream()
                        .map(i -> new OrderItemResponse(i.getProductSku(), i.getQuantity(), i.getUnitPrice(), i.lineTotal()))
                        .toList(),
                order.totalAmount());
    }
}
