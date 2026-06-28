package com.enterprise.layered.service.impl;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.layered.common.ProductErrorCodes;
import com.enterprise.layered.dto.CategoryRequest;
import com.enterprise.layered.dto.CategoryResponse;
import com.enterprise.layered.entity.Category;
import com.enterprise.layered.repository.CategoryRepository;
import com.enterprise.layered.service.CategoryService;
import com.enterprise.layered.service.mapper.CategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default {@link CategoryService}. Transactions declared here (service layer only).
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    public CategoryServiceImpl(CategoryRepository repository, CategoryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CategoryResponse create(CategoryRequest request) {
        if (repository.existsByName(request.name())) {
            throw new BusinessException(ProductErrorCodes.CATEGORY_NAME_EXISTS,
                    "Category already exists with name: '" + request.name() + "'");
        }
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return mapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    private Category findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                ProductErrorCodes.CATEGORY_NOT_FOUND, "Category not found with id: '" + id + "'"));
    }
}
