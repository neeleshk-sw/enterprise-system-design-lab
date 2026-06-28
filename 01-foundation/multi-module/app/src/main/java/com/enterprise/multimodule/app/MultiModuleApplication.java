package com.enterprise.multimodule.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Runnable application that composes the sibling modules. Component scanning is rooted at
 * {@code com.enterprise} so beans across all modules and {@code common-library} are found.
 *
 * <p>The JPA scan boundaries ({@code @EntityScan} on the domain module, {@code @EnableJpaRepositories}
 * on the persistence module) live in {@link com.enterprise.multimodule.app.config.JpaConfig} rather
 * than here — so web-slice tests ({@code @WebMvcTest}) don't try to bootstrap JPA.
 */
@SpringBootApplication(scanBasePackages = "com.enterprise")
public class MultiModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiModuleApplication.class, args);
    }
}
