package com.ciphermarket.api.securityops.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_batches")
public class AuditBatch {

    @Id
    private UUID id;

    @Column(name = "first_event_id", nullable = false)
    private UUID firstEventId;

    @Column(name = "last_event_id", nullable = false)
    private UUID lastEventId;

    @Column(name = "event_count", nullable = false)
    private int eventCount;

    @Column(name = "root_hash", nullable = false)
    private String rootHash;

    @Column(name = "previous_batch_hash")
    private String previousBatchHash;

    @Column(name = "sealed_by_user_id")
    private UUID sealedByUserId;

    @Column(name = "sealed_at", nullable = false)
    private Instant sealedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AuditBatch() {
    }

    public AuditBatch(
            UUID firstEventId,
            UUID lastEventId,
            int eventCount,
            String rootHash,
            String previousBatchHash,
            UUID sealedByUserId
    ) {
        this.id = UUID.randomUUID();
        this.firstEventId = firstEventId;
        this.lastEventId = lastEventId;
        this.eventCount = eventCount;
        this.rootHash = rootHash;
        this.previousBatchHash = previousBatchHash;
        this.sealedByUserId = sealedByUserId;
        this.sealedAt = Instant.now();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (sealedAt == null) {
            sealedAt = now;
        }
        createdAt = now;
    }

    public UUID getId() {
        return id;
    }

    public UUID getFirstEventId() {
        return firstEventId;
    }

    public UUID getLastEventId() {
        return lastEventId;
    }

    public int getEventCount() {
        return eventCount;
    }

    public String getRootHash() {
        return rootHash;
    }

    public String getPreviousBatchHash() {
        return previousBatchHash;
    }

    public UUID getSealedByUserId() {
        return sealedByUserId;
    }

    public Instant getSealedAt() {
        return sealedAt;
    }
}
