package com.enterprise.restapi.controller;

import com.enterprise.restapi.common.ApiResponse;
import com.enterprise.restapi.common.PagedResponse;
import com.enterprise.restapi.dto.ProductFilter;
import com.enterprise.restapi.dto.ProductRequest;
import com.enterprise.restapi.dto.ProductResponse;
import com.enterprise.restapi.service.IdempotencyService;
import com.enterprise.restapi.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Versioned Product API ({@code /api/v1}) demonstrating REST best practices: pagination,
 * filtering &amp; sorting, HATEOAS-lite links, idempotency keys, and ETag conditional GETs.
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService service;
    private final IdempotencyService idempotencyService;

    public ProductController(ProductService service, IdempotencyService idempotencyService) {
        this.service = service;
        this.idempotencyService = idempotencyService;
    }

    /** Create — honours an optional {@code Idempotency-Key} so retries don't duplicate. */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody ProductRequest request,
            UriComponentsBuilder uriBuilder) {
        ProductResponse created = idempotencyService.createProduct(idempotencyKey, request);
        URI location = uriBuilder.path("/api/v1/products/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(ApiResponse.success(created, "Product created successfully"));
    }

    /** Get one — returns an ETag; honours {@code If-None-Match} with a 304. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(
            @PathVariable Long id,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        ProductResponse product = service.getById(id);
        String etag = "\"" + product.version() + "\"";
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }
        return ResponseEntity.ok().eTag(etag).body(ApiResponse.success(product));
    }

    /** List — paginated, sortable ({@code ?sort=price,desc}), filterable, with navigation links. */
    @GetMapping
    public ApiResponse<PagedResponse<ProductResponse>> list(
            ProductFilter filter,
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest request) {
        Page<ProductResponse> page = service.list(filter, pageable);
        return ApiResponse.success(PagedResponse.from(page, request.getRequestURI()));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ApiResponse.success(service.update(id, request), "Product updated successfully");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
