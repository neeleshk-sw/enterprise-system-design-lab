package com.enterprise.restapi.service;

import com.enterprise.restapi.dto.ProductRequest;
import com.enterprise.restapi.dto.ProductResponse;
import com.enterprise.restapi.entity.IdempotencyKey;
import com.enterprise.restapi.repository.IdempotencyKeyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Wraps product creation with idempotency-key handling: a retried POST carrying the same
 * {@code Idempotency-Key} returns the original product instead of creating a duplicate.
 */
@Service
public class IdempotencyService {

    private final ProductService productService;
    private final IdempotencyKeyRepository keyRepository;

    public IdempotencyService(ProductService productService, IdempotencyKeyRepository keyRepository) {
        this.productService = productService;
        this.keyRepository = keyRepository;
    }

    @Transactional
    public ProductResponse createProduct(String idempotencyKey, ProductRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return productService.create(request);
        }
        Optional<IdempotencyKey> existing = keyRepository.findById(idempotencyKey);
        if (existing.isPresent()) {
            return productService.getById(existing.get().getProductId());
        }
        ProductResponse created = productService.create(request);
        keyRepository.save(new IdempotencyKey(idempotencyKey, created.id()));
        return created;
    }
}
