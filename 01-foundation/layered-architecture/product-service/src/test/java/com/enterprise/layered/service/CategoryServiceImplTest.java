package com.enterprise.layered.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.layered.dto.CategoryRequest;
import com.enterprise.layered.dto.CategoryResponse;
import com.enterprise.layered.entity.Category;
import com.enterprise.layered.repository.CategoryRepository;
import com.enterprise.layered.service.impl.CategoryServiceImpl;
import com.enterprise.layered.service.mapper.CategoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository repository;

    private CategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CategoryServiceImpl(repository, new CategoryMapper());
    }

    @Test
    void createSavesWhenNameFree() {
        when(repository.existsByName("Electronics")).thenReturn(false);
        when(repository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CategoryResponse response = service.create(new CategoryRequest("Electronics"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Electronics");
    }

    @Test
    void createRejectsDuplicateName() {
        when(repository.existsByName("Electronics")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CategoryRequest("Electronics")))
                .isInstanceOf(BusinessException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
