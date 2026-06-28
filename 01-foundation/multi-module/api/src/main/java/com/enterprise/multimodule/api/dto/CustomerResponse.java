package com.enterprise.multimodule.api.dto;

import com.enterprise.multimodule.domain.entity.CustomerStatus;

import java.time.Instant;

/**
 * Outbound representation of a customer.
 */
public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        CustomerStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
