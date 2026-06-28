package com.enterprise.multimodule.domain.repository;

import com.enterprise.multimodule.domain.entity.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Repository port defined by the domain. The {@code persistence} module provides the
 * implementation (a Spring Data repository), so the domain has no dependency on JPA/Spring Data
 * APIs beyond what {@code common-library} already brings in.
 */
public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(Long id);

    List<Customer> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);
}
