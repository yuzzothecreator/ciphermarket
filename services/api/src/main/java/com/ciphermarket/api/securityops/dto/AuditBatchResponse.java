package com.ciphermarket.api.securityops.dto;

import com.ciphermarket.api.securityops.domain.AuditBatch;

import java.time.Instant;
import java.util.UUID;

public record AuditBatchResponse(
        UUID id,
        UUID firstEventId,
        UUID lastEventId,
        int eventCount,
        String rootHash,
        String previousBatchHash,
        UUID sealedByUserId,
        Instant sealedAt
) {
    public static AuditBatchResponse from(AuditBatch batch) {
        return new AuditBatchResponse(
                batch.getId(),
                batch.getFirstEventId(),
                batch.getLastEventId(),
                batch.getEventCount(),
                batch.getRootHash(),
                batch.getPreviousBatchHash(),
                batch.getSealedByUserId(),
                batch.getSealedAt()
        );
    }
}
