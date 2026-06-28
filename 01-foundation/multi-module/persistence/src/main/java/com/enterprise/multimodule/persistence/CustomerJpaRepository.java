package com.enterprise.multimodule.persistence;

import com.enterprise.multimodule.domain.entity.Customer;
import com.enterprise.multimodule.domain.repository.CustomerRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository that fulfils the domain's {@link CustomerRepository} port.
 * The CRUD methods come from {@link JpaRepository}; {@code findByEmail}/{@code existsByEmail}
 * are derived queries — together they satisfy the port, so the domain stays free of Spring Data.
 */
public interface CustomerJpaRepository extends JpaRepository<Customer, Long>, CustomerRepository {
}
