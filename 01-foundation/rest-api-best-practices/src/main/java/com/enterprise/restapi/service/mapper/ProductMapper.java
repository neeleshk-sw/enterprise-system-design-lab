package com.enterprise.restapi.service.mapper;

import com.enterprise.restapi.dto.ProductRequest;
import com.enterprise.restapi.dto.ProductResponse;
import com.enterprise.restapi.entity.Product;
import com.enterprise.restapi.entity.ProductStatus;
import org.springframework.stereotype.Component;

/**
 * Maps between the {@link Product} entity and its DTOs.
 */
@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return product;
    }

    public void updateEntity(Product product, ProductRequest request) {
        apply(product, request);
    }

    private void apply(Product product, ProductRequest request) {
        product.setName(request.name());
        product.setSku(request.sku());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStatus(request.status() != null ? request.status() : ProductStatus.ACTIVE);
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getDescription(),
                product.getPrice(),
                product.getStatus(),
                product.getVersion(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
