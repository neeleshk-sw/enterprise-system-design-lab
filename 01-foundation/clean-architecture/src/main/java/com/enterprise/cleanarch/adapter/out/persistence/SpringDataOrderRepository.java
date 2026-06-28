package com.enterprise.cleanarch.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link OrderJpaEntity}. Internal to the persistence adapter.
 */
public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, Long> {
}
