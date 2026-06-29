package com.enterprise.restapi.service;

import com.enterprise.restapi.dto.ProductFilter;
import com.enterprise.restapi.dto.ProductRequest;
import com.enterprise.restapi.dto.ProductResponse;
import com.enterprise.restapi.entity.Product;
import com.enterprise.restapi.entity.ProductStatus;
import com.enterprise.restapi.exception.BusinessException;
import com.enterprise.restapi.exception.ResourceNotFoundException;
import com.enterprise.restapi.repository.ProductRepository;
import com.enterprise.restapi.service.impl.ProductServiceImpl;
import com.enterprise.restapi.service.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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

    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(repository, new ProductMapper());
    }

    private Product product(Long id, String sku) {
        Product p = new Product();
        p.setId(id);
        p.setName("Widget");
        p.setSku(sku);
        p.setPrice(new BigDecimal("19.99"));
        p.setStatus(ProductStatus.ACTIVE);
        return p;
    }

    private ProductRequest request(String sku) {
        return new ProductRequest("Widget", sku, "A widget", new BigDecimal("19.99"), null);
    }

    @Test
    void createPersistsWhenSkuFree() {
        when(repository.existsBySku("SKU-1")).thenReturn(false);
        when(repository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        ProductResponse response = service.create(request("SKU-1"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void createRejectsDuplicateSku() {
        when(repository.existsBySku("DUP")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("DUP"))).isInstanceOf(BusinessException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listAppliesSpecificationAndMaps() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(product(1L, "SKU-1")), pageable, 1);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<ProductResponse> result = service.list(new ProductFilter(null, ProductStatus.ACTIVE, null, null), pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).sku()).isEqualTo("SKU-1");
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }
}
