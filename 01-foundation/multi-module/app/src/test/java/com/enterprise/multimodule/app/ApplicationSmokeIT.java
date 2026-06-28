package com.enterprise.multimodule.app;

import com.enterprise.multimodule.api.dto.CustomerRequest;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack smoke test: boots the whole reactor as one application against a real
 * PostgreSQL, proving the modules wire together end to end.
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
    void createsAndFetchesCustomerThroughAllModules() {
        CustomerRequest request = new CustomerRequest("Ada", "Lovelace", "ada@example.com", null);

        ResponseEntity<String> created = rest.postForEntity("/api/v1/customers", request, String.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).contains("\"success\":true").contains("ada@example.com");

        Number id = JsonPath.read(created.getBody(), "$.data.id");
        assertThat(rest.getForEntity("/api/v1/customers/" + id, String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void actuatorHealthIsUp() {
        ResponseEntity<String> response = rest.getForEntity("/actuator/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }
}
