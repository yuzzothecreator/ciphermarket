package com.ciphermarket.api.product.dto;

import com.ciphermarket.api.common.enums.ProductType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateProductRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank
        @Size(min = 3, max = 128)
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$")
        String slug,
        @NotNull ProductType productType,
        UUID categoryId,
        @Size(max = 500) String shortDescription,
        @Min(0) long priceCents,
        @Size(min = 3, max = 3) String currency
) {
}
