package com.enterprise.multimodule.api.dto;

import com.enterprise.multimodule.domain.entity.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for creating or updating a customer. {@code status} is optional
 * (defaults to {@code ACTIVE} on create).
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

        CustomerStatus status) {
}
