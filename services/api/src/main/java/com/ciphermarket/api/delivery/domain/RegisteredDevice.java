package com.ciphermarket.api.delivery.domain;

import com.ciphermarket.api.common.enums.DeviceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "registered_devices")
public class RegisteredDevice {

    @Id
    private UUID id;

    @Column(name = "buyer_user_id", nullable = false)
    private UUID buyerUserId;

    @Column(name = "fingerprint_hash", nullable = false)
    private String fingerprintHash;

    @Column(nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceStatus status = DeviceStatus.ACTIVE;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected RegisteredDevice() {
    }

    public RegisteredDevice(UUID buyerUserId, String fingerprintHash, String label) {
        this.id = UUID.randomUUID();
        this.buyerUserId = buyerUserId;
        this.fingerprintHash = fingerprintHash;
        this.label = label;
        this.registeredAt = Instant.now();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (registeredAt == null) {
            registeredAt = now;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getBuyerUserId() {
        return buyerUserId;
    }

    public String getFingerprintHash() {
        return fingerprintHash;
    }

    public String getLabel() {
        return label;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void markSeen() {
        this.lastSeenAt = Instant.now();
    }

    public void revoke() {
        this.status = DeviceStatus.REVOKED;
        this.revokedAt = Instant.now();
    }
}
