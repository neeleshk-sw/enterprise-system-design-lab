package com.enterprise.cleanarch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the clean-architecture demonstration (Order domain).
 *
 * <p>The package structure follows the dependency rule: {@code domain} and
 * {@code application} are framework-free; Spring/JPA live only in {@code adapter}.
 * Use-cases are wired as beans in {@code config} so the application layer stays pure.
 */
@SpringBootApplication
public class CleanArchitectureApplication {

    public static void main(String[] args) {
        SpringApplication.run(CleanArchitectureApplication.class, args);
    }
}
