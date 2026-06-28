package com.enterprise.layered.controller;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.GlobalExceptionHandler;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.layered.common.ProductErrorCodes;
import com.enterprise.layered.dto.ProductRequest;
import com.enterprise.layered.dto.ProductResponse;
import com.enterprise.layered.entity.ProductStatus;
import com.enterprise.layered.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService service;

    private ProductResponse sample(Long id) {
        return new ProductResponse(id, "Widget", "SKU-1", "A widget", new BigDecimal("19.99"),
                ProductStatus.ACTIVE, 1L, "Electronics", Instant.now(), Instant.now());
    }

    private ProductRequest validRequest() {
        return new ProductRequest("Widget", "SKU-1", "A widget", new BigDecimal("19.99"), null, 1L);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @Test
    void createReturns201WithEnvelope() throws Exception {
        when(service.create(any())).thenReturn(sample(10L));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.sku").value("SKU-1"))
                .andExpect(jsonPath("$.message").value("Product created successfully"));
    }

    @Test
    void createWithInvalidBodyReturns400() throws Exception {
        // blank name, negative price, null categoryId
        ProductRequest invalid = new ProductRequest("", "SKU-1", null, new BigDecimal("-1"), null, null);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    void getByIdReturns200() throws Exception {
        when(service.getById(10L)).thenReturn(sample(10L));

        mockMvc.perform(get("/api/v1/products/{id}", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    void getByIdNotFoundReturns404() throws Exception {
        when(service.getById(99L)).thenThrow(
                new ResourceNotFoundException(ProductErrorCodes.PRODUCT_NOT_FOUND, "Product not found with id: '99'"));

        mockMvc.perform(get("/api/v1/products/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void listReturnsPagedEnvelope() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        when(service.list(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sample(10L)), pageable, 1));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].sku").value("SKU-1"))
                .andExpect(jsonPath("$.data.page.totalElements").value(1));
    }

    @Test
    void updateReturns200() throws Exception {
        when(service.update(eq(10L), any())).thenReturn(sample(10L));

        mockMvc.perform(put("/api/v1/products/{id}", 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product updated successfully"));
    }

    @Test
    void duplicateSkuReturns409() throws Exception {
        when(service.create(any())).thenThrow(
                new BusinessException(ProductErrorCodes.PRODUCT_SKU_EXISTS, "Product already exists with sku: 'SKU-1'"));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_SKU_EXISTS"));
    }

    @Test
    void deleteReturns200WithMessage() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product deleted successfully"));
    }
}
