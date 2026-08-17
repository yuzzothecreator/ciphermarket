package com.ciphermarket.api.product.dto;

import com.ciphermarket.api.common.enums.UploadSessionStatus;
import com.ciphermarket.api.product.domain.UploadSession;

import java.time.Instant;
import java.util.UUID;

public record UploadSessionResponse(
        UUID id,
        UUID productId,
        UUID productVersionId,
        UUID assetId,
        UploadSessionStatus status,
        String quarantineObjectKey,
        long maxSizeBytes,
        Instant expiresAt
) {
    public static UploadSessionResponse from(UploadSession session) {
        return new UploadSessionResponse(
                session.getId(),
                session.getProductId(),
                session.getProductVersionId(),
                session.getAssetId(),
                session.getStatus(),
                session.getQuarantineObjectKey(),
                session.getMaxSizeBytes(),
                session.getExpiresAt()
        );
    }
}
