package com.enterprise.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the customer-service template application.
 *
 * <p>Component scanning is rooted at {@code com.enterprise} so that shared
 * beans provided by {@code common-library} (e.g. the global exception handler)
 * are picked up alongside this service's own components.
 */
@SpringBootApplication(scanBasePackages = "com.enterprise")
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}
