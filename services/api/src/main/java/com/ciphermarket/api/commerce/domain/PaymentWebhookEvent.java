package com.ciphermarket.api.commerce.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_webhook_events")
public class PaymentWebhookEvent {

    @Id
    private UUID id;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(nullable = false)
    private String provider;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "payload_hash", nullable = false)
    private String payloadHash;

    @Column
    private String signature;

    @Column(nullable = false)
    private boolean processed;

    @Column(name = "processing_error")
    private String processingError;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected PaymentWebhookEvent() {
    }

    public static PaymentWebhookEvent received(
            UUID paymentId,
            String provider,
            String idempotencyKey,
            String eventType,
            String payloadHash,
            String signature
    ) {
        PaymentWebhookEvent event = new PaymentWebhookEvent();
        event.id = UUID.randomUUID();
        event.paymentId = paymentId;
        event.provider = provider;
        event.idempotencyKey = idempotencyKey;
        event.eventType = eventType;
        event.payloadHash = payloadHash;
        event.signature = signature;
        event.processed = false;
        event.receivedAt = Instant.now();
        return event;
    }

    @PrePersist
    void onCreate() {
        if (receivedAt == null) {
            receivedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public String getProvider() {
        return provider;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getEventType() {
        return eventType;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void markProcessed() {
        this.processed = true;
        this.processedAt = Instant.now();
    }

    public void markFailed(String error) {
        this.processingError = error;
        this.processedAt = Instant.now();
    }
}
