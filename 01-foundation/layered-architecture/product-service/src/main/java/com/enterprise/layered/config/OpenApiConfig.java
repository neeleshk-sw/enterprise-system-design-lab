package com.enterprise.layered.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata. springdoc auto-exposes the spec at {@code /v3/api-docs}
 * and Swagger UI at {@code /swagger-ui.html}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productServiceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Product Service API")
                .description("01-foundation — layered architecture (Product/Category)")
                .version("v1"));
    }
}
