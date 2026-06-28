package com.enterprise.cleanarch;

import com.enterprise.cleanarch.adapter.in.web.dto.OrderItemRequest;
import com.enterprise.cleanarch.adapter.in.web.dto.PlaceOrderRequest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack smoke test: boots the application against a real PostgreSQL and exercises
 * the place → get → cancel flow through every layer, plus Actuator.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ApplicationSmokeIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate rest;

    @Test
    void placeGetAndCancelOrderThroughFullStack() {
        PlaceOrderRequest request = new PlaceOrderRequest("CUST-SMOKE",
                List.of(new OrderItemRequest("SKU-1", 2, new BigDecimal("10.00"))));

        ResponseEntity<String> placed = rest.postForEntity("/api/v1/orders", request, String.class);
        assertThat(placed.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(placed.getBody()).contains("\"success\":true").contains("\"status\":\"NEW\"");
        Number orderId = JsonPath.read(placed.getBody(), "$.data.id");

        ResponseEntity<String> fetched = rest.getForEntity("/api/v1/orders/" + orderId, String.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> cancelled = rest.postForEntity(
                "/api/v1/orders/" + orderId + "/cancel", null, String.class);
        assertThat(cancelled.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelled.getBody()).contains("\"status\":\"CANCELLED\"");
    }

    @Test
    void actuatorHealthIsUp() {
        ResponseEntity<String> response = rest.getForEntity("/actuator/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }
}
