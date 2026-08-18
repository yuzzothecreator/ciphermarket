package com.ciphermarket.api.delivery.dto;

import java.time.Instant;
import java.util.UUID;

public record AccessGrantResponse(
        UUID id,
        String accessToken,
        Instant expiresAt,
        int maxUses,
        int useCount
) {
}
