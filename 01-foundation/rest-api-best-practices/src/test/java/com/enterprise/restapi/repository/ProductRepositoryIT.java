package com.enterprise.restapi.repository;

import com.enterprise.restapi.dto.ProductFilter;
import com.enterprise.restapi.entity.Product;
import com.enterprise.restapi.entity.ProductStatus;
import com.enterprise.restapi.repository.spec.ProductSpecifications;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository integration test (Testcontainers) — verifies dynamic filtering via Specifications
 * and SKU uniqueness against a real PostgreSQL.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ProductRepository repository;

    private Product product(String name, String sku, String price, ProductStatus status) {
        Product p = new Product();
        p.setName(name);
        p.setSku(sku);
        p.setPrice(new BigDecimal(price));
        p.setStatus(status);
        return p;
    }

    @Test
    void filtersByStatusAndPriceRange() {
        repository.saveAndFlush(product("Cheap Widget", "SKU-1", "5.00", ProductStatus.ACTIVE));
        repository.saveAndFlush(product("Pricey Widget", "SKU-2", "50.00", ProductStatus.ACTIVE));
        repository.saveAndFlush(product("Old Widget", "SKU-3", "20.00", ProductStatus.DISCONTINUED));

        ProductFilter filter = new ProductFilter(null, ProductStatus.ACTIVE, new BigDecimal("10.00"), new BigDecimal("100.00"));
        Page<Product> result = repository.findAll(ProductSpecifications.from(filter), PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Product::getSku).containsExactly("SKU-2");
    }

    @Test
    void filtersByNameContains() {
        repository.saveAndFlush(product("Red Gadget", "SKU-10", "5.00", ProductStatus.ACTIVE));
        repository.saveAndFlush(product("Blue Widget", "SKU-11", "5.00", ProductStatus.ACTIVE));

        ProductFilter filter = new ProductFilter("gadget", null, null, null);
        Page<Product> result = repository.findAll(ProductSpecifications.from(filter), PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Product::getSku).containsExactly("SKU-10");
    }

    @Test
    void existsBySkuReflectsPresence() {
        repository.saveAndFlush(product("Widget", "SKU-20", "5.00", ProductStatus.ACTIVE));

        assertThat(repository.existsBySku("SKU-20")).isTrue();
        assertThat(repository.existsBySku("MISSING")).isFalse();
    }
}
