package com.enterprise.customer.service;

import com.enterprise.customer.dto.CustomerRequest;
import com.enterprise.customer.dto.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Customer business operations. Implementations own transactions and business rules.
 */
public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    CustomerResponse getById(Long id);

    Page<CustomerResponse> list(Pageable pageable);

    CustomerResponse update(Long id, CustomerRequest request);

    void delete(Long id);
}
