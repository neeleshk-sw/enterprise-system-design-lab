package com.enterprise.cleanarch.adapter.in.web;

import com.enterprise.cleanarch.adapter.in.web.dto.OrderResponse;
import com.enterprise.cleanarch.adapter.in.web.dto.PlaceOrderRequest;
import com.enterprise.cleanarch.adapter.in.web.response.ApiResponse;
import com.enterprise.cleanarch.application.port.in.CancelOrderUseCase;
import com.enterprise.cleanarch.application.port.in.OrderQueryUseCase;
import com.enterprise.cleanarch.application.port.in.PlaceOrderUseCase;
import com.enterprise.cleanarch.domain.Order;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Inbound web adapter. Depends only on the application's input ports (use-cases),
 * never on the persistence adapter. Wraps payloads in {@link ApiResponse}.
 */
@RestController
@RequestMapping("/api/v1/orders")
class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final OrderQueryUseCase orderQueryUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final OrderWebMapper mapper;

    OrderController(PlaceOrderUseCase placeOrderUseCase, OrderQueryUseCase orderQueryUseCase,
                    CancelOrderUseCase cancelOrderUseCase, OrderWebMapper mapper) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.orderQueryUseCase = orderQueryUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> place(@Valid @RequestBody PlaceOrderRequest request,
                                                            UriComponentsBuilder uriBuilder) {
        Order order = placeOrderUseCase.placeOrder(mapper.toCommand(request));
        OrderResponse body = mapper.toResponse(order);
        URI location = uriBuilder.path("/api/v1/orders/{id}").buildAndExpand(order.getId()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.success(body, "Order placed successfully"));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(mapper.toResponse(orderQueryUseCase.getById(id)));
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> list() {
        List<OrderResponse> orders = orderQueryUseCase.findAll().stream().map(mapper::toResponse).toList();
        return ApiResponse.success(orders);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancel(@PathVariable Long id) {
        return ApiResponse.success(mapper.toResponse(cancelOrderUseCase.cancel(id)), "Order cancelled");
    }
}
