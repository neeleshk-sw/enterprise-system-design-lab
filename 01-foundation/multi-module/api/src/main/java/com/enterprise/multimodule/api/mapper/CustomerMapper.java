package com.enterprise.multimodule.api.mapper;

import com.enterprise.multimodule.api.dto.CustomerRequest;
import com.enterprise.multimodule.api.dto.CustomerResponse;
import com.enterprise.multimodule.domain.entity.Customer;
import org.springframework.stereotype.Component;

/**
 * Maps between the {@link Customer} domain entity and the web DTOs.
 */
@Component
public class CustomerMapper {

    /** Build an entity carrying the request's values (used for create and as an update carrier). */
    public Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());
        customer.setStatus(request.status());
        return customer;
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }
}
