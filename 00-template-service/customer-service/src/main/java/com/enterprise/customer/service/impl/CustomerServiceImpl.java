package com.enterprise.customer.service.impl;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.customer.common.CustomerErrorCodes;
import com.enterprise.customer.dto.CustomerRequest;
import com.enterprise.customer.dto.CustomerResponse;
import com.enterprise.customer.entity.Customer;
import com.enterprise.customer.repository.CustomerRepository;
import com.enterprise.customer.service.CustomerService;
import com.enterprise.customer.service.mapper.CustomerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link CustomerService} implementation. Transactions are declared here
 * (service layer only); reads are marked {@code readOnly}.
 */
@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public CustomerServiceImpl(CustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CustomerResponse create(CustomerRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw emailExists(request.email());
        }
        Customer saved = repository.save(mapper.toEntity(request));
        log.info("Created customer id={} email={}", saved.getId(), saved.getEmail());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findOrThrow(id);
        if (!customer.getEmail().equals(request.email()) && repository.existsByEmail(request.email())) {
            throw emailExists(request.email());
        }
        mapper.updateEntity(customer, request);
        Customer saved = repository.save(customer);
        log.info("Updated customer id={}", saved.getId());
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw notFound(id);
        }
        repository.deleteById(id);
        log.info("Deleted customer id={}", id);
    }

    private Customer findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> notFound(id));
    }

    private static ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException(CustomerErrorCodes.CUSTOMER_NOT_FOUND,
                "Customer not found with id: '" + id + "'");
    }

    private static BusinessException emailExists(String email) {
        return new BusinessException(CustomerErrorCodes.CUSTOMER_EMAIL_EXISTS,
                "Customer already exists with email: '" + email + "'");
    }
}
