package com.ciphermarket.api.commerce.dto;

import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        List<CartItemResponse> items,
        long subtotalCents,
        String currency,
        int itemCount
) {
}
