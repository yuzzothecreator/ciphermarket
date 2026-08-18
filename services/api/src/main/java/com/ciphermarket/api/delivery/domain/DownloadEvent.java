package com.ciphermarket.api.delivery.domain;

import com.ciphermarket.api.common.enums.DownloadOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "download_events")
public class DownloadEvent {

    @Id
    private UUID id;

    @Column(name = "access_grant_id", nullable = false)
    private UUID accessGrantId;

    @Column(name = "buyer_user_id", nullable = false)
    private UUID buyerUserId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_asset_id", nullable = false)
    private UUID productAssetId;

    @Column(name = "device_id")
    private UUID deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DownloadOutcome outcome;

    @Column(name = "client_ip")
    private String clientIp;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "bytes_delivered")
    private Long bytesDelivered;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DownloadEvent() {
    }

    public static DownloadEvent record(
            UUID accessGrantId,
            UUID buyerUserId,
            UUID productId,
            UUID productAssetId,
            UUID deviceId,
            DownloadOutcome outcome,
            String clientIp,
            String userAgent,
            Long bytesDelivered
    ) {
        DownloadEvent event = new DownloadEvent();
        event.id = UUID.randomUUID();
        event.accessGrantId = accessGrantId;
        event.buyerUserId = buyerUserId;
        event.productId = productId;
        event.productAssetId = productAssetId;
        event.deviceId = deviceId;
        event.outcome = outcome;
        event.clientIp = clientIp;
        event.userAgent = userAgent;
        event.bytesDelivered = bytesDelivered;
        event.createdAt = Instant.now();
        return event;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public DownloadOutcome getOutcome() {
        return outcome;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
