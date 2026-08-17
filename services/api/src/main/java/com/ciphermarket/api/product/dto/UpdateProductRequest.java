package com.ciphermarket.api.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateProductRequest(
        @Size(max = 255) String name,
        @Size(max = 500) String shortDescription,
        String fullDescription,
        UUID categoryId,
        @Min(0) Long priceCents,
        @Size(min = 3, max = 3) String currency,
        String licenceType,
        String usageTerms,
        String refundPolicy
) {
}
