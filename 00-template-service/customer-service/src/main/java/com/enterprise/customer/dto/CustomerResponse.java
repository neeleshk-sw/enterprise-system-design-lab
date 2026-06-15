package com.enterprise.customer.dto;

import com.enterprise.customer.entity.CustomerStatus;

import java.time.Instant;

/**
 * Outbound representation of a customer. Entities are never exposed directly.
 */
public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        CustomerStatus status,
        Instant createdAt,
        Instant updatedAt) {
}
