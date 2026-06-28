package com.enterprise.multimodule.api.controller;

import com.enterprise.common.response.ApiResponse;
import com.enterprise.multimodule.api.dto.CustomerRequest;
import com.enterprise.multimodule.api.dto.CustomerResponse;
import com.enterprise.multimodule.api.mapper.CustomerMapper;
import com.enterprise.multimodule.domain.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API for customers. Lives in the {@code api} module; delegates to the domain
 * {@link CustomerService} and wraps payloads in {@link ApiResponse}.
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService service;
    private final CustomerMapper mapper;

    public CustomerController(CustomerService service, CustomerMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse created = mapper.toResponse(service.create(mapper.toEntity(request)));
        return ApiResponse.success(created, "Customer created successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(mapper.toResponse(service.getById(id)));
    }

    @GetMapping
    public ApiResponse<List<CustomerResponse>> list() {
        List<CustomerResponse> customers = service.findAll().stream().map(mapper::toResponse).toList();
        return ApiResponse.success(customers);
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        CustomerResponse updated = mapper.toResponse(service.update(id, mapper.toEntity(request)));
        return ApiResponse.success(updated, "Customer updated successfully");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.message("Customer deleted successfully");
    }
}
