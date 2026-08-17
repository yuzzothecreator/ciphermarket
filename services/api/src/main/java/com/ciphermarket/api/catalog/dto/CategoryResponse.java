package com.ciphermarket.api.catalog.dto;

import com.ciphermarket.api.catalog.domain.Category;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        int sortOrder
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getSortOrder()
        );
    }
}
