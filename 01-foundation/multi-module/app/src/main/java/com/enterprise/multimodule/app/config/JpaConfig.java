package com.enterprise.multimodule.app.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA wiring for the multi-module app, kept out of the application class so web-slice
 * tests don't bootstrap JPA:
 * <ul>
 *   <li>{@code @EntityScan} — JPA entities in the domain module</li>
 *   <li>{@code @EnableJpaRepositories} — Spring Data repositories in the persistence module</li>
 *   <li>{@code @EnableJpaAuditing} — populate created/updated timestamps</li>
 * </ul>
 */
@Configuration
@EnableJpaAuditing
@EntityScan("com.enterprise.multimodule.domain.entity")
@EnableJpaRepositories("com.enterprise.multimodule.persistence")
public class JpaConfig {
}
