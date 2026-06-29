package com.enterprise.restapi.controller;

import com.enterprise.restapi.common.ProductErrorCodes;
import com.enterprise.restapi.dto.ProductFilter;
import com.enterprise.restapi.dto.ProductRequest;
import com.enterprise.restapi.dto.ProductResponse;
import com.enterprise.restapi.entity.ProductStatus;
import com.enterprise.restapi.exception.GlobalExceptionHandler;
import com.enterprise.restapi.exception.ResourceNotFoundException;
import com.enterprise.restapi.service.IdempotencyService;
import com.enterprise.restapi.service.ProductService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
    @MockitoBean
    private IdempotencyService idempotencyService;

    private ProductResponse sample(Long id, long version) {
        return new ProductResponse(id, "Widget", "SKU-1", "A widget", new BigDecimal("19.99"),
                ProductStatus.ACTIVE, version, Instant.now(), Instant.now());
    }

    private ProductRequest validRequest() {
        return new ProductRequest("Widget", "SKU-1", "A widget", new BigDecimal("19.99"), null);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(idempotencyService.createProduct(any(), any())).thenReturn(sample(10L, 0));

        mockMvc.perform(post("/api/v1/products")
                        .header("Idempotency-Key", "key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/products/10"))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.message").value("Product created successfully"));
    }

    @Test
    void createWithInvalidBodyReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ProductRequest("", "SKU-1", null, new BigDecimal("-1"), null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void getByIdReturns200WithEtag() throws Exception {
        when(service.getById(10L)).thenReturn(sample(10L, 3));

        mockMvc.perform(get("/api/v1/products/{id}", 10))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"3\""))
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    void getByIdReturns304WhenIfNoneMatchMatches() throws Exception {
        when(service.getById(10L)).thenReturn(sample(10L, 3));

        mockMvc.perform(get("/api/v1/products/{id}", 10).header("If-None-Match", "\"3\""))
                .andExpect(status().isNotModified());
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
    void listReturnsPagedResponseWithLinks() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        when(service.list(any(ProductFilter.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sample(10L, 0)), pageable, 1));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].sku").value("SKU-1"))
                .andExpect(jsonPath("$.data.page.totalElements").value(1))
                .andExpect(jsonPath("$.data.links.self").value("/api/v1/products?page=0&size=20"));
    }

    @Test
    void updateReturns200() throws Exception {
        when(service.update(eq(10L), any())).thenReturn(sample(10L, 1));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/v1/products/{id}", 10)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product updated successfully"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", 10))
                .andExpect(status().isNoContent());
    }
}
