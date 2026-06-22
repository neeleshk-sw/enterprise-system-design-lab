package com.enterprise.basiccrud.service;

import com.enterprise.basiccrud.dto.CustomerRequest;
import com.enterprise.basiccrud.dto.CustomerResponse;
import com.enterprise.basiccrud.entity.Customer;
import com.enterprise.basiccrud.exception.NotFoundException;
import com.enterprise.basiccrud.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Customer business operations. Transactions live here (service layer only); reads are
 * {@code readOnly}. Entity↔DTO mapping is done inline — kept simple on purpose; the
 * layered-architecture project promotes this to a dedicated mapper.
 */
@Service
@Transactional
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public CustomerResponse create(CustomerRequest request) {
        Customer saved = repository.save(toEntity(request));
        log.info("Created customer id={} email={}", saved.getId(), saved.getEmail());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> list() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findOrThrow(id);
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());
        Customer saved = repository.save(customer);
        log.info("Updated customer id={}", saved.getId());
        return toResponse(saved);
    }

    public void delete(Long id) {
        Customer customer = findOrThrow(id);
        repository.delete(customer);
        log.info("Deleted customer id={}", id);
    }

    private Customer findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + id));
    }

    private Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());
        return customer;
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }
}
