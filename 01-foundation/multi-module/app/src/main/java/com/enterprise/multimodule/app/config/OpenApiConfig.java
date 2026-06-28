package com.enterprise.multimodule.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata. springdoc serves the spec at {@code /v3/api-docs} and Swagger UI
 * at {@code /swagger-ui.html}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI multiModuleOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Multi-Module Customer API")
                .description("01-foundation — Maven multi-module split (domain/persistence/api/app)")
                .version("v1"));
    }
}
