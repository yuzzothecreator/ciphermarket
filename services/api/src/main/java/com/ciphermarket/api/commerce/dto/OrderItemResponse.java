package com.ciphermarket.api.commerce.dto;

import com.ciphermarket.api.commerce.domain.OrderItem;

import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productId,
        String productName,
        String productSlug,
        long unitPriceCents,
        String currency,
        int quantity,
        long lineTotalCents
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getProductSlug(),
                item.getUnitPriceCents(),
                item.getCurrency(),
                item.getQuantity(),
                item.getLineTotalCents()
        );
    }
}
