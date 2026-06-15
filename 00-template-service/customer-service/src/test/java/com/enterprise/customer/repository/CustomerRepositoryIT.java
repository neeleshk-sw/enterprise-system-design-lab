package com.enterprise.customer.repository;

import com.enterprise.customer.config.JpaConfig;
import com.enterprise.customer.entity.Customer;
import com.enterprise.customer.entity.CustomerStatus;
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

import java.util.Optional;

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
class CustomerRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CustomerRepository repository;

    private Customer newCustomer(String email) {
        Customer customer = new Customer();
        customer.setFirstName("Ada");
        customer.setLastName("Lovelace");
        customer.setEmail(email);
        customer.setPhone("+1-555-0100");
        customer.setStatus(CustomerStatus.ACTIVE);
        return customer;
    }

    @Test
    void savesAndRetrievesById() {
        Customer saved = repository.saveAndFlush(newCustomer("ada@example.com"));
        assertThat(saved.getId()).isNotNull();

        Optional<Customer> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("ada@example.com");
        assertThat(found.get().getStatus()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    void populatesAuditFieldsOnSave() {
        Customer saved = repository.saveAndFlush(newCustomer("grace@example.com"));

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getVersion()).isZero();
    }

    @Test
    void findByEmailReturnsMatchOrEmpty() {
        repository.saveAndFlush(newCustomer("alan@example.com"));

        assertThat(repository.findByEmail("alan@example.com")).isPresent();
        assertThat(repository.findByEmail("missing@example.com")).isEmpty();
    }

    @Test
    void existsByEmailReflectsPresence() {
        repository.saveAndFlush(newCustomer("edsger@example.com"));

        assertThat(repository.existsByEmail("edsger@example.com")).isTrue();
        assertThat(repository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    void enforcesEmailUniqueness() {
        repository.saveAndFlush(newCustomer("dup@example.com"));

        assertThatThrownBy(() -> repository.saveAndFlush(newCustomer("dup@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
