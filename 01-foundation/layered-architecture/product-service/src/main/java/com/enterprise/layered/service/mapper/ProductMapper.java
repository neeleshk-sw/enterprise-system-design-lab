package com.enterprise.layered.service.mapper;

import com.enterprise.layered.dto.ProductRequest;
import com.enterprise.layered.dto.ProductResponse;
import com.enterprise.layered.entity.Category;
import com.enterprise.layered.entity.Product;
import com.enterprise.layered.entity.ProductStatus;
import org.springframework.stereotype.Component;

/**
 * Maps between {@link Product} and its DTOs. The {@link Category} association is
 * resolved by the service and passed in — the mapper performs no data access.
 */
@Component
public class ProductMapper {

    /** Build a new entity from a create request (status defaults to ACTIVE). */
    public Product toEntity(ProductRequest request, Category category) {
        Product product = new Product();
        apply(product, request, category);
        return product;
    }

    /** Apply an update request onto an existing entity. */
    public void updateEntity(Product product, ProductRequest request, Category category) {
        apply(product, request, category);
    }

    private void apply(Product product, ProductRequest request, Category category) {
        product.setName(request.name());
        product.setSku(request.sku());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStatus(request.status() != null ? request.status() : ProductStatus.ACTIVE);
        product.setCategory(category);
    }

    public ProductResponse toResponse(Product product) {
        Category category = product.getCategory();
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getDescription(),
                product.getPrice(),
                product.getStatus(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
