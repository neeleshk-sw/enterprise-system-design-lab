package com.enterprise.layered.service.impl;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.layered.common.ProductErrorCodes;
import com.enterprise.layered.dto.ProductRequest;
import com.enterprise.layered.dto.ProductResponse;
import com.enterprise.layered.entity.Category;
import com.enterprise.layered.entity.Product;
import com.enterprise.layered.repository.CategoryRepository;
import com.enterprise.layered.repository.ProductRepository;
import com.enterprise.layered.service.ProductService;
import com.enterprise.layered.service.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link ProductService}. Transactions are declared here (service layer only);
 * reads are {@code readOnly}. Business rules: unique SKU, category must exist.
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper mapper;

    public ProductServiceImpl(ProductRepository repository, CategoryRepository categoryRepository,
                              ProductMapper mapper) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Override
    public ProductResponse create(ProductRequest request) {
        if (repository.existsBySku(request.sku())) {
            throw skuExists(request.sku());
        }
        Category category = resolveCategory(request.categoryId());
        Product saved = repository.save(mapper.toEntity(request, category));
        log.info("Created product id={} sku={}", saved.getId(), saved.getSku());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findOrThrow(id);
        if (!product.getSku().equals(request.sku()) && repository.existsBySku(request.sku())) {
            throw skuExists(request.sku());
        }
        Category category = resolveCategory(request.categoryId());
        mapper.updateEntity(product, request, category);
        Product saved = repository.save(product);
        log.info("Updated product id={}", saved.getId());
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw productNotFound(id);
        }
        repository.deleteById(id);
        log.info("Deleted product id={}", id);
    }

    private Product findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> productNotFound(id));
    }

    private Category resolveCategory(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException(
                ProductErrorCodes.CATEGORY_NOT_FOUND, "Category not found with id: '" + categoryId + "'"));
    }

    private static ResourceNotFoundException productNotFound(Long id) {
        return new ResourceNotFoundException(ProductErrorCodes.PRODUCT_NOT_FOUND,
                "Product not found with id: '" + id + "'");
    }

    private static BusinessException skuExists(String sku) {
        return new BusinessException(ProductErrorCodes.PRODUCT_SKU_EXISTS,
                "Product already exists with sku: '" + sku + "'");
    }
}
