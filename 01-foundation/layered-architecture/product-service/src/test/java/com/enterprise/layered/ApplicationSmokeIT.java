package com.enterprise.layered;

import com.enterprise.layered.dto.CategoryRequest;
import com.enterprise.layered.dto.ProductRequest;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack smoke test: boots the application against a real PostgreSQL and
 * exercises the category → product flow plus Actuator and OpenAPI.
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
    void createsCategoryThenProductThroughFullStack() {
        ResponseEntity<String> categoryResponse = rest.postForEntity(
                "/api/v1/categories", new CategoryRequest("Electronics"), String.class);
        assertThat(categoryResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Number categoryId = JsonPath.read(categoryResponse.getBody(), "$.data.id");

        ProductRequest productRequest = new ProductRequest(
                "Widget", "SKU-SMOKE-1", "A widget", new BigDecimal("19.99"), null, categoryId.longValue());
        ResponseEntity<String> productResponse = rest.postForEntity(
                "/api/v1/products", productRequest, String.class);

        assertThat(productResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(productResponse.getBody()).contains("\"success\":true").contains("SKU-SMOKE-1");

        Number productId = JsonPath.read(productResponse.getBody(), "$.data.id");
        assertThat(rest.getForEntity("/api/v1/products/" + productId, String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void actuatorHealthIsUp() {
        ResponseEntity<String> response = rest.getForEntity("/actuator/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void openApiDocumentIsAvailable() {
        ResponseEntity<String> response = rest.getForEntity("/v3/api-docs", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Product Service API");
    }
}
