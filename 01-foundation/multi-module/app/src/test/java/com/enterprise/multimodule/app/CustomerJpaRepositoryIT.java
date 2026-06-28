package com.enterprise.multimodule.app;

import com.enterprise.multimodule.app.config.JpaConfig;
import com.enterprise.multimodule.domain.entity.Customer;
import com.enterprise.multimodule.domain.entity.CustomerStatus;
import com.enterprise.multimodule.persistence.CustomerJpaRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repository integration test against a real PostgreSQL (Testcontainers). The repository
 * lives in the persistence module and is enabled by the app's @EnableJpaRepositories.
 */
@DataJpaTest
@Testcontainers
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerJpaRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CustomerJpaRepository repository;

    private Customer newCustomer(String email) {
        Customer customer = new Customer();
        customer.setFirstName("Ada");
        customer.setLastName("Lovelace");
        customer.setEmail(email);
        customer.setStatus(CustomerStatus.ACTIVE);
        return customer;
    }

    @Test
    void savesAndPopulatesAuditFields() {
        Customer saved = repository.saveAndFlush(newCustomer("ada@example.com"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getVersion()).isZero();
    }

    @Test
    void findByEmailAndExistsByEmailWork() {
        repository.saveAndFlush(newCustomer("grace@example.com"));

        assertThat(repository.findByEmail("grace@example.com")).isPresent();
        assertThat(repository.existsByEmail("grace@example.com")).isTrue();
        assertThat(repository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    void enforcesEmailUniqueness() {
        repository.saveAndFlush(newCustomer("dup@example.com"));

        assertThatThrownBy(() -> repository.saveAndFlush(newCustomer("dup@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
