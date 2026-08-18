package com.ciphermarket.api.delivery.dto;

import java.time.Instant;
import java.util.UUID;

public record DownloadEventResponse(
        UUID id,
        UUID productId,
        UUID productAssetId,
        String outcome,
        Instant createdAt
) {
}
