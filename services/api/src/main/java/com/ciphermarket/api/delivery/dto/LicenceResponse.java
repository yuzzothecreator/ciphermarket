package com.ciphermarket.api.delivery.dto;

import com.ciphermarket.api.delivery.domain.Licence;

import java.time.Instant;
import java.util.UUID;

public record LicenceResponse(
        UUID id,
        UUID entitlementId,
        UUID productId,
        UUID productVersionId,
        String signedToken,
        Instant issuedAt,
        Instant expiresAt,
        String status
) {
    public static LicenceResponse from(Licence licence) {
        return new LicenceResponse(
                licence.getId(),
                licence.getEntitlementId(),
                licence.getProductId(),
                licence.getProductVersionId(),
                licence.getSignedToken(),
                licence.getIssuedAt(),
                licence.getExpiresAt(),
                licence.getStatus().name()
        );
    }
}
