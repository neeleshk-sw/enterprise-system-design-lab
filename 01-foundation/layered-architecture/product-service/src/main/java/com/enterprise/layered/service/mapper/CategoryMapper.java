package com.enterprise.layered.service.mapper;

import com.enterprise.layered.dto.CategoryRequest;
import com.enterprise.layered.dto.CategoryResponse;
import com.enterprise.layered.entity.Category;
import org.springframework.stereotype.Component;

/**
 * Maps between {@link Category} and its DTOs. Pure translation — no persistence or rules.
 */
@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        return category;
    }

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
