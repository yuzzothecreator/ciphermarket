package com.ciphermarket.api.commerce.dto;

import java.util.List;
import java.util.UUID;

public record SalesAnalyticsResponse(
        long paidOrderCount,
        long unitsSold,
        long revenueCents,
        String currency,
        List<ProductSalesBreakdown> products
) {
    public record ProductSalesBreakdown(
            UUID productId,
            String productName,
            long unitsSold,
            long revenueCents,
            String currency
    ) {
    }
}
