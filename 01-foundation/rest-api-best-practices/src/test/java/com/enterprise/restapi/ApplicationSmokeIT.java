package com.enterprise.restapi;

import com.enterprise.restapi.dto.ProductRequest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack smoke test exercising the headline behaviours: idempotent create, ETag
 * conditional GET, and filtered listing with HATEOAS-lite links.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ApplicationSmokeIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate rest;

    private HttpEntity<ProductRequest> withIdempotencyKey(ProductRequest body, String key) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", key);
        return new HttpEntity<>(body, headers);
    }

    @Test
    void idempotentCreateReturnsSameProduct() {
        ProductRequest request = new ProductRequest("Widget", "SKU-IDEM", "A widget", new BigDecimal("19.99"), null);
        HttpEntity<ProductRequest> entity = withIdempotencyKey(request, "key-123");

        ResponseEntity<String> first = rest.postForEntity("/api/v1/products", entity, String.class);
        ResponseEntity<String> second = rest.postForEntity("/api/v1/products", entity, String.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Number firstId = JsonPath.read(first.getBody(), "$.data.id");
        Number secondId = JsonPath.read(second.getBody(), "$.data.id");
        assertThat(secondId).isEqualTo(firstId);
    }

    @Test
    void conditionalGetReturns304ForMatchingEtag() {
        ProductRequest request = new ProductRequest("Etag Widget", "SKU-ETAG", null, new BigDecimal("9.99"), null);
        Number id = JsonPath.read(
                rest.postForEntity("/api/v1/products", request, String.class).getBody(), "$.data.id");

        ResponseEntity<String> fetched = rest.getForEntity("/api/v1/products/" + id, String.class);
        String etag = fetched.getHeaders().getETag();
        assertThat(etag).isNotNull();

        HttpHeaders headers = new HttpHeaders();
        headers.setIfNoneMatch(etag);
        ResponseEntity<String> notModified = rest.exchange(
                "/api/v1/products/" + id, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(notModified.getStatusCode()).isEqualTo(HttpStatus.NOT_MODIFIED);
    }

    @Test
    void listSupportsFilteringAndExposesLinks() {
        rest.postForEntity("/api/v1/products",
                new ProductRequest("Filterable", "SKU-FILTER", null, new BigDecimal("15.00"), null), String.class);

        ResponseEntity<String> response = rest.getForEntity("/api/v1/products?status=ACTIVE&minPrice=10", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"links\"").contains("\"self\"");
    }
}
