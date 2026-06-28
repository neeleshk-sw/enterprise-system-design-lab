package com.enterprise.layered.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing so {@code @CreatedDate} / {@code @LastModifiedDate} on
 * {@link com.enterprise.common.entity.AuditEntity} are populated automatically.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
