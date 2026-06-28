package com.enterprise.layered.service;

import com.enterprise.layered.dto.ProductRequest;
import com.enterprise.layered.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Product business operations. Implementations own transactions and business rules.
 */
public interface ProductService {

    ProductResponse create(ProductRequest request);

    ProductResponse getById(Long id);

    Page<ProductResponse> list(Pageable pageable);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);
}
