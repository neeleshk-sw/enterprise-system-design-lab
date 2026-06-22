package com.enterprise.basiccrud.repository;

import com.enterprise.basiccrud.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repository integration test against a real PostgreSQL (Testcontainers).
 * Flyway creates the schema; Hibernate validates against it.
 */
@DataJpaTest
@Testcontainers
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
        return customer;
    }

    @Test
    void savesAndPopulatesTimestamps() {
        Customer saved = repository.saveAndFlush(newCustomer("ada@example.com"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByEmailReturnsMatchOrEmpty() {
        repository.saveAndFlush(newCustomer("grace@example.com"));

        assertThat(repository.findByEmail("grace@example.com")).isPresent();
        assertThat(repository.findByEmail("missing@example.com")).isEmpty();
    }

    @Test
    void existsByEmailReflectsPresence() {
        repository.saveAndFlush(newCustomer("alan@example.com"));

        assertThat(repository.existsByEmail("alan@example.com")).isTrue();
        assertThat(repository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    void enforcesEmailUniqueness() {
        repository.saveAndFlush(newCustomer("dup@example.com"));

        assertThatThrownBy(() -> repository.saveAndFlush(newCustomer("dup@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
