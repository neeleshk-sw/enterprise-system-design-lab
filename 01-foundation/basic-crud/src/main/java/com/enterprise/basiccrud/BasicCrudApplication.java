package com.enterprise.basiccrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the basic-crud foundation example — the simplest correct
 * CRUD REST service: single module, no shared library, plain DTO responses.
 */
@SpringBootApplication
public class BasicCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasicCrudApplication.class, args);
    }
}
