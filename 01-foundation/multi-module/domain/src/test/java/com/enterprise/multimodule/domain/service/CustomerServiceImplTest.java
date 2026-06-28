package com.enterprise.multimodule.domain.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.multimodule.domain.entity.Customer;
import com.enterprise.multimodule.domain.entity.CustomerStatus;
import com.enterprise.multimodule.domain.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository repository;

    private CustomerServiceImpl service() {
        return new CustomerServiceImpl(repository);
    }

    private Customer customer(Long id, String email, CustomerStatus status) {
        Customer c = new Customer();
        c.setId(id);
        c.setFirstName("Ada");
        c.setLastName("Lovelace");
        c.setEmail(email);
        c.setStatus(status);
        return c;
    }

    @Test
    void createDefaultsStatusAndSaves() {
        when(repository.existsByEmail("ada@example.com")).thenReturn(false);
        when(repository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        Customer result = service().create(customer(null, "ada@example.com", null));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(repository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service().create(customer(null, "dup@example.com", CustomerStatus.ACTIVE)))
                .isInstanceOf(BusinessException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().getById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllReturnsList() {
        when(repository.findAll()).thenReturn(List.of(customer(1L, "ada@example.com", CustomerStatus.ACTIVE)));

        assertThat(service().findAll()).hasSize(1);
    }

    @Test
    void updateAppliesChanges() {
        when(repository.findById(1L)).thenReturn(Optional.of(customer(1L, "ada@example.com", CustomerStatus.ACTIVE)));
        when(repository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer result = service().update(1L, customer(null, "ada@example.com", CustomerStatus.INACTIVE));

        assertThat(result.getStatus()).isEqualTo(CustomerStatus.INACTIVE);
    }

    @Test
    void updateRejectsEmailTakenByAnother() {
        when(repository.findById(1L)).thenReturn(Optional.of(customer(1L, "old@example.com", CustomerStatus.ACTIVE)));
        when(repository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service().update(1L, customer(null, "taken@example.com", CustomerStatus.ACTIVE)))
                .isInstanceOf(BusinessException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service().delete(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }
}
