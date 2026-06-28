package com.enterprise.layered.service;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.layered.dto.ProductRequest;
import com.enterprise.layered.dto.ProductResponse;
import com.enterprise.layered.entity.Category;
import com.enterprise.layered.entity.Product;
import com.enterprise.layered.entity.ProductStatus;
import com.enterprise.layered.repository.CategoryRepository;
import com.enterprise.layered.repository.ProductRepository;
import com.enterprise.layered.service.impl.ProductServiceImpl;
import com.enterprise.layered.service.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;
    @Mock
    private CategoryRepository categoryRepository;

    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(repository, categoryRepository, new ProductMapper());
    }

    private Category category(Long id) {
        Category c = new Category();
        c.setId(id);
        c.setName("Electronics");
        return c;
    }

    private Product product(Long id, String sku, Category category) {
        Product p = new Product();
        p.setId(id);
        p.setName("Widget");
        p.setSku(sku);
        p.setPrice(new BigDecimal("19.99"));
        p.setStatus(ProductStatus.ACTIVE);
        p.setCategory(category);
        return p;
    }

    private ProductRequest request(String sku, Long categoryId) {
        return new ProductRequest("Widget", sku, "A widget", new BigDecimal("19.99"), null, categoryId);
    }

    @Test
    void createResolvesCategoryAndDefaultsStatus() {
        when(repository.existsBySku("SKU-1")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category(1L)));
        when(repository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProductResponse response = service.create(request("SKU-1", 1L));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(response.categoryId()).isEqualTo(1L);
        assertThat(response.categoryName()).isEqualTo("Electronics");
    }

    @Test
    void createRejectsDuplicateSku() {
        when(repository.existsBySku("DUP")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("DUP", 1L)))
                .isInstanceOf(BusinessException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void createFailsWhenCategoryMissing() {
        when(repository.existsBySku("SKU-2")).thenReturn(false);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request("SKU-2", 99L)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void getByIdReturnsMapped() {
        when(repository.findById(10L)).thenReturn(Optional.of(product(10L, "SKU-1", category(1L))));

        assertThat(service.getById(10L).sku()).isEqualTo("SKU-1");
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listReturnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(product(10L, "SKU-1", category(1L))), pageable, 1);
        when(repository.findAll(pageable)).thenReturn(page);

        Page<ProductResponse> result = service.list(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).sku()).isEqualTo("SKU-1");
    }

    @Test
    void updateAppliesChanges() {
        when(repository.findById(10L)).thenReturn(Optional.of(product(10L, "SKU-1", category(1L))));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category(1L)));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response = service.update(10L,
                new ProductRequest("Gadget", "SKU-1", "Updated", new BigDecimal("29.99"), ProductStatus.DISCONTINUED, 1L));

        assertThat(response.name()).isEqualTo("Gadget");
        assertThat(response.status()).isEqualTo(ProductStatus.DISCONTINUED);
    }

    @Test
    void updateRejectsSkuTakenByAnother() {
        when(repository.findById(10L)).thenReturn(Optional.of(product(10L, "OLD", category(1L))));
        when(repository.existsBySku("TAKEN")).thenReturn(true);

        assertThatThrownBy(() -> service.update(10L, request("TAKEN", 1L)))
                .isInstanceOf(BusinessException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void deleteRemovesExisting() {
        when(repository.existsById(10L)).thenReturn(true);

        service.delete(10L);

        verify(repository).deleteById(10L);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }
}
