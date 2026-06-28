package com.enterprise.layered.repository;

import com.enterprise.layered.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Data access for {@link Product}. Lookups return {@link Optional}, never null.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);
}
