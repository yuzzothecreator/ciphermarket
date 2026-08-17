package com.ciphermarket.api.commerce.dto;

import java.util.UUID;

public record MockPaymentWebhookPayload(
        String eventType,
        UUID paymentId,
        String externalReference,
        long amountCents,
        String currency,
        long timestamp
) {
}
