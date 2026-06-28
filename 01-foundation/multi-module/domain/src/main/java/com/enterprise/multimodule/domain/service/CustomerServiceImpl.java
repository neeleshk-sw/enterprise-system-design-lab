package com.enterprise.multimodule.domain.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.multimodule.domain.common.CustomerErrorCodes;
import com.enterprise.multimodule.domain.entity.Customer;
import com.enterprise.multimodule.domain.entity.CustomerStatus;
import com.enterprise.multimodule.domain.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default {@link CustomerService}. Transactions are declared here (service layer only).
 * Depends only on the {@link CustomerRepository} port — the persistence implementation is
 * injected by Spring.
 */
@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository repository;

    public CustomerServiceImpl(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Customer create(Customer customer) {
        if (customer.getStatus() == null) {
            customer.setStatus(CustomerStatus.ACTIVE);
        }
        if (repository.existsByEmail(customer.getEmail())) {
            throw emailExists(customer.getEmail());
        }
        Customer saved = repository.save(customer);
        log.info("Created customer id={} email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Customer getById(Long id) {
        return findOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return repository.findAll();
    }

    @Override
    public Customer update(Long id, Customer changes) {
        Customer customer = findOrThrow(id);
        if (!customer.getEmail().equals(changes.getEmail()) && repository.existsByEmail(changes.getEmail())) {
            throw emailExists(changes.getEmail());
        }
        customer.setFirstName(changes.getFirstName());
        customer.setLastName(changes.getLastName());
        customer.setEmail(changes.getEmail());
        if (changes.getStatus() != null) {
            customer.setStatus(changes.getStatus());
        }
        return repository.save(customer);
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
