package com.enterprise.basiccrud.service;

import com.enterprise.basiccrud.dto.CustomerRequest;
import com.enterprise.basiccrud.dto.CustomerResponse;
import com.enterprise.basiccrud.entity.Customer;
import com.enterprise.basiccrud.exception.NotFoundException;
import com.enterprise.basiccrud.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private CustomerService service;

    private CustomerRequest request(String email) {
        return new CustomerRequest("Ada", "Lovelace", email);
    }

    private Customer entity(Long id, String email) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName("Ada");
        customer.setLastName("Lovelace");
        customer.setEmail(email);
        return customer;
    }

    @Test
    void createSavesAndReturnsResponse() {
        when(repository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CustomerResponse response = service.create(request("ada@example.com"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("ada@example.com");
        verify(repository).save(any(Customer.class));
    }

    @Test
    void getByIdReturnsResponse() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity(1L, "ada@example.com")));

        assertThat(service.getById(1L).email()).isEqualTo("ada@example.com");
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void listReturnsMappedResponses() {
        when(repository.findAll()).thenReturn(List.of(entity(1L, "ada@example.com"), entity(2L, "grace@example.com")));

        List<CustomerResponse> result = service.list();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CustomerResponse::email)
                .containsExactly("ada@example.com", "grace@example.com");
    }

    @Test
    void updateAppliesChanges() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity(1L, "old@example.com")));
        when(repository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerResponse response = service.update(1L, new CustomerRequest("Grace", "Hopper", "grace@example.com"));

        assertThat(response.firstName()).isEqualTo("Grace");
        assertThat(response.email()).isEqualTo("grace@example.com");
    }

    @Test
    void updateThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request("x@example.com")))
                .isInstanceOf(NotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void deleteRemovesExisting() {
        Customer customer = entity(1L, "ada@example.com");
        when(repository.findById(1L)).thenReturn(Optional.of(customer));

        service.delete(1L);

        verify(repository).delete(customer);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(NotFoundException.class);
        verify(repository, never()).delete(any());
    }
}
