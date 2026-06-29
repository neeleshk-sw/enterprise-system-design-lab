package com.enterprise.restapi.repository;

import com.enterprise.restapi.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for processed idempotency keys (keyed by the header value).
 */
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
}
