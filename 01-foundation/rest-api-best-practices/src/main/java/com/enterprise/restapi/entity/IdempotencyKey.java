package com.enterprise.restapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Records a processed {@code Idempotency-Key} and the product it created, so a retried
 * POST with the same key returns the original result instead of creating a duplicate.
 */
@Entity
@Table(name = "idempotency_keys")
@Getter
@Setter
public class IdempotencyKey {

    @Id
    @Column(name = "idempotency_key", length = 128)
    private String key;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected IdempotencyKey() {
        // for JPA
    }

    public IdempotencyKey(String key, Long productId) {
        this.key = key;
        this.productId = productId;
    }
}
