package com.enterprise.restapi.service;

import com.enterprise.restapi.dto.ProductFilter;
import com.enterprise.restapi.dto.ProductRequest;
import com.enterprise.restapi.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Product business operations.
 */
public interface ProductService {

    ProductResponse create(ProductRequest request);

    ProductResponse getById(Long id);

    Page<ProductResponse> list(ProductFilter filter, Pageable pageable);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);
}
