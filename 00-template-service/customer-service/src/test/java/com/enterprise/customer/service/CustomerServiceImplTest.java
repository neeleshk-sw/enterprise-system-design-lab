package com.enterprise.customer.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.customer.dto.CustomerRequest;
import com.enterprise.customer.dto.CustomerResponse;
import com.enterprise.customer.entity.Customer;
import com.enterprise.customer.entity.CustomerStatus;
import com.enterprise.customer.repository.CustomerRepository;
import com.enterprise.customer.service.impl.CustomerServiceImpl;
import com.enterprise.customer.service.mapper.CustomerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        // Real mapper (pure translation) for stronger assertions; only the repository is mocked.
        service = new CustomerServiceImpl(repository, new CustomerMapper());
    }

    private CustomerRequest request(String email, CustomerStatus status) {
        return new CustomerRequest("Ada", "Lovelace", email, "+1-555-0100", status);
    }

    private Customer entity(Long id, String email, CustomerStatus status) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName("Ada");
        customer.setLastName("Lovelace");
        customer.setEmail(email);
        customer.setPhone("+1-555-0100");
        customer.setStatus(status);
        return customer;
    }

    @Test
    void createPersistsAndDefaultsStatusToActive() {
        when(repository.existsByEmail("ada@example.com")).thenReturn(false);
        when(repository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CustomerResponse response = service.create(request("ada@example.com", null));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(CustomerStatus.ACTIVE);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(repository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("dup@example.com", CustomerStatus.ACTIVE)))
                .isInstanceOf(BusinessException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void getByIdReturnsMappedCustomer() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity(1L, "ada@example.com", CustomerStatus.ACTIVE)));

        CustomerResponse response = service.getById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("ada@example.com");
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listReturnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Customer> page = new PageImpl<>(List.of(entity(1L, "ada@example.com", CustomerStatus.ACTIVE)), pageable, 1);
        when(repository.findAll(pageable)).thenReturn(page);

        Page<CustomerResponse> result = service.list(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).email()).isEqualTo("ada@example.com");
    }

    @Test
    void updateAppliesChanges() {
        Customer existing = entity(1L, "ada@example.com", CustomerStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerResponse response = service.update(1L,
                new CustomerRequest("Grace", "Hopper", "ada@example.com", "+1-555-0199", CustomerStatus.INACTIVE));

        assertThat(response.firstName()).isEqualTo("Grace");
        assertThat(response.lastName()).isEqualTo("Hopper");
        assertThat(response.status()).isEqualTo(CustomerStatus.INACTIVE);
    }

    @Test
    void updateThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request("x@example.com", CustomerStatus.ACTIVE)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateRejectsEmailTakenByAnotherCustomer() {
        Customer existing = entity(1L, "old@example.com", CustomerStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request("taken@example.com", CustomerStatus.ACTIVE)))
                .isInstanceOf(BusinessException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void deleteRemovesExistingCustomer() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
