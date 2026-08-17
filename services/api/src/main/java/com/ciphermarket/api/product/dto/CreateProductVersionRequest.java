package com.ciphermarket.api.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProductVersionRequest(
        @NotBlank @Size(max = 64) String versionLabel,
        String changelog
) {
}
