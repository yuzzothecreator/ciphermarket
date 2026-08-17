package com.ciphermarket.api.organisation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateOrganisationRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank
        @Size(min = 3, max = 128)
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase alphanumeric with hyphens")
        String slug,
        @Size(max = 5000) String description
) {
}
