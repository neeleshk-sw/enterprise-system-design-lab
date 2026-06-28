package com.enterprise.cleanarch.config;

import com.enterprise.cleanarch.application.OrderService;
import com.enterprise.cleanarch.application.port.out.OrderRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires framework-free use-cases as Spring beans, so the application layer itself
 * carries no Spring annotations. The single {@link OrderService} bean satisfies all
 * three input-port interfaces it implements.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public OrderService orderService(OrderRepositoryPort orderRepositoryPort) {
        return new OrderService(orderRepositoryPort);
    }
}
