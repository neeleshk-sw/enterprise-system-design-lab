package com.enterprise.multimodule.domain.service;

import com.enterprise.multimodule.domain.entity.Customer;

import java.util.List;

/**
 * Customer business operations. Works in terms of the domain entity; the web layer maps
 * to/from DTOs.
 */
public interface CustomerService {

    Customer create(Customer customer);

    Customer getById(Long id);

    List<Customer> findAll();

    Customer update(Long id, Customer changes);

    void delete(Long id);
}
