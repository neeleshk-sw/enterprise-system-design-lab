package com.enterprise.basiccrud.dto;

import java.time.Instant;

/**
 * Outbound representation of a customer. Returned directly (no response envelope)
 * — that is the deliberate contrast with the layered/template projects.
 */
public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Instant createdAt,
        Instant updatedAt) {
}
