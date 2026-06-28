package com.enterprise.layered.service;

import com.enterprise.layered.dto.CategoryRequest;
import com.enterprise.layered.dto.CategoryResponse;

import java.util.List;

/**
 * Category business operations (supporting lookup for products).
 */
public interface CategoryService {

    CategoryResponse create(CategoryRequest request);

    CategoryResponse getById(Long id);

    List<CategoryResponse> list();
}
