package com.enterprise.layered;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the layered-architecture demonstration (Product/Category).
 *
 * <p>Component scanning is rooted at {@code com.enterprise} so shared beans from
 * {@code common-library} (e.g. the global exception handler) are discovered.
 */
@SpringBootApplication(scanBasePackages = "com.enterprise")
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
