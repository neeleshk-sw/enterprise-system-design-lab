package com.enterprise.customer.service.mapper;

import com.enterprise.customer.dto.CustomerRequest;
import com.enterprise.customer.dto.CustomerResponse;
import com.enterprise.customer.entity.Customer;
import com.enterprise.customer.entity.CustomerStatus;
import org.springframework.stereotype.Component;

/**
 * Maps between the {@link Customer} entity and its request/response DTOs.
 * Pure, side-effect-free translation — no persistence or business rules here.
 */
@Component
public class CustomerMapper {

    /** Build a new entity from a create request (status defaults to ACTIVE). */
    public Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setStatus(request.status() != null ? request.status() : CustomerStatus.ACTIVE);
        return customer;
    }

    /** Apply an update request onto an existing entity (status only if provided). */
    public void updateEntity(Customer customer, CustomerRequest request) {
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        if (request.status() != null) {
            customer.setStatus(request.status());
        }
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }
}
