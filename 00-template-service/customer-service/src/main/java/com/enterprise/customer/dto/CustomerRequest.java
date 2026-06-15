package com.enterprise.customer.dto;

import com.enterprise.customer.entity.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for creating or updating a customer.
 *
 * <p>{@code status} is optional; on create it defaults to {@code ACTIVE}.
 */
public record CustomerRequest(

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotBlank
        @Email
        @Size(max = 320)
        String email,

        @Size(max = 30)
        String phone,

        CustomerStatus status) {
}
