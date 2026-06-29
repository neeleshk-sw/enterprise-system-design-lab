package com.enterprise.restapi.service.impl;

import com.enterprise.restapi.common.ProductErrorCodes;
import com.enterprise.restapi.dto.ProductFilter;
import com.enterprise.restapi.dto.ProductRequest;
import com.enterprise.restapi.dto.ProductResponse;
import com.enterprise.restapi.entity.Product;
import com.enterprise.restapi.exception.BusinessException;
import com.enterprise.restapi.exception.ResourceNotFoundException;
import com.enterprise.restapi.repository.ProductRepository;
import com.enterprise.restapi.repository.spec.ProductSpecifications;
import com.enterprise.restapi.service.ProductService;
import com.enterprise.restapi.service.mapper.ProductMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductServiceImpl(ProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public ProductResponse create(ProductRequest request) {
        if (repository.existsBySku(request.sku())) {
            throw skuExists(request.sku());
        }
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> list(ProductFilter filter, Pageable pageable) {
        return repository.findAll(ProductSpecifications.from(filter), pageable).map(mapper::toResponse);
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findOrThrow(id);
        if (!product.getSku().equals(request.sku()) && repository.existsBySku(request.sku())) {
            throw skuExists(request.sku());
        }
        mapper.updateEntity(product, request);
        return mapper.toResponse(repository.save(product));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw notFound(id);
        }
        repository.deleteById(id);
    }

    private Product findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> notFound(id));
    }

    private static ResourceNotFoundException notFound(Long id) {
        return new ResourceNotFoundException(ProductErrorCodes.PRODUCT_NOT_FOUND,
                "Product not found with id: '" + id + "'");
    }

    private static BusinessException skuExists(String sku) {
        return new BusinessException(ProductErrorCodes.PRODUCT_SKU_EXISTS,
                "Product already exists with sku: '" + sku + "'");
    }
}
