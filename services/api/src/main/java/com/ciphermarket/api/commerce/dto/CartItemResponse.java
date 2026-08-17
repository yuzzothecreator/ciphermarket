package com.ciphermarket.api.commerce.dto;

import com.ciphermarket.api.commerce.domain.CartItem;
import com.ciphermarket.api.product.domain.Product;

import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID productId,
        String productName,
        String productSlug,
        long unitPriceCents,
        String currency,
        int quantity,
        long lineTotalCents
) {
    public static CartItemResponse from(CartItem item, Product product) {
        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getPriceCents(),
                product.getCurrency(),
                item.getQuantity(),
                product.getPriceCents() * item.getQuantity()
        );
    }
}
