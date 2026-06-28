package com.enterprise.layered.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for creating a category.
 */
public record CategoryRequest(

        @NotBlank
        @Size(max = 100)
        String name) {
}
