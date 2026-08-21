package com.ciphermarket.api.disclosure.domain;

import com.ciphermarket.api.common.enums.DisclosureDocumentStatus;
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
@Table(name = "disclosure_documents")
public class DisclosureDocument {

    @Id
    private UUID id;

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "sanitized_file_name", nullable = false)
    private String sanitizedFileName;

    @Column(name = "declared_content_type", nullable = false)
    private String declaredContentType;

    @Column(name = "detected_content_type")
    private String detectedContentType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "sha256_checksum")
    private String sha256Checksum;

    @Column(name = "document_version", nullable = false)
    private int documentVersion = 1;

    @Column(name = "storage_bucket")
    private String storageBucket;

    @Column(name = "storage_object_key")
    private String storageObjectKey;

    @Column(name = "quarantine_object_key")
    private String quarantineObjectKey;

    @Column(nullable = false)
    private boolean encrypted = false;

    @Column(name = "wrapped_dek", columnDefinition = "TEXT")
    private String wrappedDek;

    @Column(name = "dek_key_version")
    private String dekKeyVersion;

    @Column(name = "encryption_nonce")
    private String encryptionNonce;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisclosureDocumentStatus status = DisclosureDocumentStatus.PROCESSING;

    @Column(name = "scan_status")
    private String scanStatus;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected DisclosureDocument() {
    }

    public DisclosureDocument(
            UUID organisationId,
            UUID createdByUserId,
            String title,
            String description,
            String originalFileName,
            String sanitizedFileName,
            String declaredContentType
    ) {
        this.id = UUID.randomUUID();
        this.organisationId = organisationId;
        this.createdByUserId = createdByUserId;
        this.title = title;
        this.description = description;
        this.originalFileName = originalFileName;
        this.sanitizedFileName = sanitizedFileName;
        this.declaredContentType = declaredContentType;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
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

    public UUID getOrganisationId() {
        return organisationId;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getSanitizedFileName() {
        return sanitizedFileName;
    }

    public String getDeclaredContentType() {
        return declaredContentType;
    }

    public String getDetectedContentType() {
        return detectedContentType;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getSha256Checksum() {
        return sha256Checksum;
    }

    public int getDocumentVersion() {
        return documentVersion;
    }

    public String getStorageBucket() {
        return storageBucket;
    }

    public String getStorageObjectKey() {
        return storageObjectKey;
    }

    public String getQuarantineObjectKey() {
        return quarantineObjectKey;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public String getWrappedDek() {
        return wrappedDek;
    }

    public String getDekKeyVersion() {
        return dekKeyVersion;
    }

    public String getEncryptionNonce() {
        return encryptionNonce;
    }

    public DisclosureDocumentStatus getStatus() {
        return status;
    }

    public String getScanStatus() {
        return scanStatus;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setQuarantineObjectKey(String quarantineObjectKey) {
        this.quarantineObjectKey = quarantineObjectKey;
    }

    public void setDetectedContentType(String detectedContentType) {
        this.detectedContentType = detectedContentType;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public void setSha256Checksum(String sha256Checksum) {
        this.sha256Checksum = sha256Checksum;
    }

    public void setStorageBucket(String storageBucket) {
        this.storageBucket = storageBucket;
    }

    public void setStorageObjectKey(String storageObjectKey) {
        this.storageObjectKey = storageObjectKey;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public void setWrappedDek(String wrappedDek) {
        this.wrappedDek = wrappedDek;
    }

    public void setDekKeyVersion(String dekKeyVersion) {
        this.dekKeyVersion = dekKeyVersion;
    }

    public void setEncryptionNonce(String encryptionNonce) {
        this.encryptionNonce = encryptionNonce;
    }

    public void setStatus(DisclosureDocumentStatus status) {
        this.status = status;
    }

    public void setScanStatus(String scanStatus) {
        this.scanStatus = scanStatus;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
