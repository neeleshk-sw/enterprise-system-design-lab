package com.enterprise.customer;

import com.enterprise.common.constant.Constants;
import com.enterprise.customer.dto.CustomerRequest;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack smoke test: boots the entire application against a real PostgreSQL
 * and exercises the HTTP layer, the request-logging filter (trace header),
 * Actuator, and the OpenAPI document.
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
    void createsCustomerThroughFullStackAndSetsTraceHeader() {
        CustomerRequest request = new CustomerRequest("Ada", "Lovelace", "ada@example.com", "+1-555-0100", null);

        ResponseEntity<String> response = rest.postForEntity("/api/v1/customers", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("\"success\":true").contains("ada@example.com");
        assertThat(response.getHeaders().getFirst(Constants.TRACE_ID_HEADER)).isNotBlank();
    }

    @Test
    void actuatorHealthIsUp() {
        ResponseEntity<String> response = rest.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void actuatorInfoAndMetricsRespond() {
        assertThat(rest.getForEntity("/actuator/info", String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rest.getForEntity("/actuator/metrics", String.class).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void openApiDocumentIsAvailable() {
        ResponseEntity<String> response = rest.getForEntity("/v3/api-docs", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Customer Service API");
    }
}
