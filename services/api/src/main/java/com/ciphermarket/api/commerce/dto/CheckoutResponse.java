package com.ciphermarket.api.commerce.dto;

import java.util.UUID;

public record CheckoutResponse(
        UUID orderId,
        UUID paymentId,
        String paymentProvider,
        long amountCents,
        String currency,
        String checkoutUrl,
        boolean requiresPayment
) {
}
