package com.enterprise.restapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the rest-api-best-practices example — demonstrates URI versioning,
 * pagination, filtering &amp; sorting, HATEOAS-lite links, idempotency keys, and ETags.
 */
@SpringBootApplication
public class RestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestApiApplication.class, args);
    }
}
