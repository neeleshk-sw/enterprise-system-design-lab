package com.enterprise.layered.repository;

import com.enterprise.layered.config.JpaConfig;
import com.enterprise.layered.entity.Category;
import com.enterprise.layered.entity.Product;
import com.enterprise.layered.entity.ProductStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repository integration test against a real PostgreSQL (Testcontainers).
 * Flyway creates the schema; Hibernate validates against it.
 */
@DataJpaTest
@Testcontainers
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private Category persistCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return categoryRepository.saveAndFlush(category);
    }

    private Product newProduct(String sku, Category category) {
        Product product = new Product();
        product.setName("Widget");
        product.setSku(sku);
        product.setPrice(new BigDecimal("19.99"));
        product.setStatus(ProductStatus.ACTIVE);
        product.setCategory(category);
        return product;
    }

    @Test
    void savesProductWithCategoryAndAuditFields() {
        Category category = persistCategory("Electronics");
        Product saved = productRepository.saveAndFlush(newProduct("SKU-1", category));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getVersion()).isZero();
        assertThat(productRepository.findBySku("SKU-1")).isPresent();
        assertThat(productRepository.findBySku("SKU-1").get().getCategory().getName()).isEqualTo("Electronics");
    }

    @Test
    void existsBySkuReflectsPresence() {
        Category category = persistCategory("Books");
        productRepository.saveAndFlush(newProduct("SKU-2", category));

        assertThat(productRepository.existsBySku("SKU-2")).isTrue();
        assertThat(productRepository.existsBySku("MISSING")).isFalse();
    }

    @Test
    void enforcesSkuUniqueness() {
        Category category = persistCategory("Toys");
        productRepository.saveAndFlush(newProduct("DUP", category));

        assertThatThrownBy(() -> productRepository.saveAndFlush(newProduct("DUP", category)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
