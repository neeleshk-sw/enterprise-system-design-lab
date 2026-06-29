package com.enterprise.restapi.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Paged collection envelope with HATEOAS-lite navigation links ({@code self}/{@code next}/
 * {@code prev}). Page-navigation links are included; per-item links are omitted for brevity.
 */
public record PagedResponse<T>(List<T> content, PageMeta page, Links links) {

    public record PageMeta(int number, int size, long totalElements, int totalPages) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Links(String self, String next, String prev) {
    }

    /**
     * Build from a {@link Page}, deriving navigation links from the request path and page size.
     *
     * @param basePath request URI (e.g. {@code /api/v1/products})
     */
    public static <T> PagedResponse<T> from(Page<T> page, String basePath) {
        int number = page.getNumber();
        int size = page.getSize();
        String self = pageLink(basePath, number, size);
        String next = page.hasNext() ? pageLink(basePath, number + 1, size) : null;
        String prev = page.hasPrevious() ? pageLink(basePath, number - 1, size) : null;
        PageMeta meta = new PageMeta(number, size, page.getTotalElements(), page.getTotalPages());
        return new PagedResponse<>(page.getContent(), meta, new Links(self, next, prev));
    }

    private static String pageLink(String basePath, int page, int size) {
        return basePath + "?page=" + page + "&size=" + size;
    }
}
