package com.ciphermarket.api.upload.service;

import com.ciphermarket.api.common.enums.AssetStatus;
import com.ciphermarket.api.common.enums.ProductStatus;
import com.ciphermarket.api.common.enums.ProductVersionStatus;
import com.ciphermarket.api.common.enums.UploadSessionStatus;
import com.ciphermarket.api.config.StorageProperties;
import com.ciphermarket.api.encryption.EncryptedPayload;
import com.ciphermarket.api.encryption.EncryptionProvider;
import com.ciphermarket.api.product.domain.Product;
import com.ciphermarket.api.product.domain.ProductAsset;
import com.ciphermarket.api.product.domain.ProductVersion;
import com.ciphermarket.api.product.domain.UploadSession;
import com.ciphermarket.api.product.repository.ProductAssetRepository;
import com.ciphermarket.api.product.repository.ProductRepository;
import com.ciphermarket.api.product.repository.ProductVersionRepository;
import com.ciphermarket.api.product.repository.UploadSessionRepository;
import com.ciphermarket.api.storage.ObjectStorageProvider;
import com.ciphermarket.api.upload.messaging.UploadProcessingMessage;
import com.ciphermarket.api.upload.scan.ClamAvScanner;
import com.ciphermarket.api.upload.scan.ScanResult;
import com.ciphermarket.api.upload.validation.FileValidationService;
import com.ciphermarket.api.upload.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class UploadProcessingService {

    private static final Logger log = LoggerFactory.getLogger(UploadProcessingService.class);

    private final UploadSessionRepository uploadSessionRepository;
    private final ProductAssetRepository assetRepository;
    private final ProductRepository productRepository;
    private final ProductVersionRepository versionRepository;
    private final ObjectStorageProvider storageProvider;
    private final StorageProperties storageProperties;
    private final FileValidationService fileValidationService;
    private final ClamAvScanner clamAvScanner;
    private final EncryptionProvider encryptionProvider;

    public UploadProcessingService(
            UploadSessionRepository uploadSessionRepository,
            ProductAssetRepository assetRepository,
            ProductRepository productRepository,
            ProductVersionRepository versionRepository,
            ObjectStorageProvider storageProvider,
            StorageProperties storageProperties,
            FileValidationService fileValidationService,
            ClamAvScanner clamAvScanner,
            EncryptionProvider encryptionProvider
    ) {
        this.uploadSessionRepository = uploadSessionRepository;
        this.assetRepository = assetRepository;
        this.productRepository = productRepository;
        this.versionRepository = versionRepository;
        this.storageProvider = storageProvider;
        this.storageProperties = storageProperties;
        this.fileValidationService = fileValidationService;
        this.clamAvScanner = clamAvScanner;
        this.encryptionProvider = encryptionProvider;
    }

    @RabbitListener(queues = "#{@uploadProcessingQueue.name}")
    @Transactional
    public void processUpload(UploadProcessingMessage message) {
        if (message.correlationId() != null) {
            MDC.put("correlationId", message.correlationId());
        }
        try {
            doProcess(message);
        } catch (Exception ex) {
            log.error("Upload processing failed for asset {}", message.assetId(), ex);
            failAsset(message, ex.getMessage());
        } finally {
            MDC.remove("correlationId");
        }
    }

    private void doProcess(UploadProcessingMessage message) throws Exception {
        ProductAsset asset = assetRepository.findById(message.assetId())
                .orElseThrow(() -> new IllegalStateException("Asset not found"));
        UploadSession session = uploadSessionRepository.findById(message.uploadSessionId())
                .orElseThrow(() -> new IllegalStateException("Session not found"));

        asset.setStatus(AssetStatus.SCANNING);
        assetRepository.save(asset);

        updateProductProcessingState(asset);

        byte[] quarantineBytes;
        try (InputStream input = storageProvider.getObject(
                storageProperties.bucketQuarantine(),
                asset.getQuarantineObjectKey()
        )) {
            quarantineBytes = input.readAllBytes();
        }

        String detectedMime = fileValidationService.detectMimeType(
                new ByteArrayInputStream(quarantineBytes),
                asset.getOriginalFileName()
        );
        asset.setDetectedContentType(detectedMime);

        ValidationResult mimeValidation = fileValidationService.validateMimeType(
                detectedMime,
                asset.getDeclaredContentType()
        );
        if (!mimeValidation.valid()) {
            rejectAsset(asset, session, mimeValidation.reason());
            return;
        }

        ScanResult scanResult = clamAvScanner.scan(new ByteArrayInputStream(quarantineBytes));
        asset.setScanStatus(scanResult.status().name());
        asset.setScanDetails(scanResult.details());

        if (!scanResult.isClean()) {
            rejectAsset(asset, session, "Malware scan failed: " + scanResult.details());
            asset.setStatus(AssetStatus.SCAN_FAILED);
            assetRepository.save(asset);
            return;
        }

        String checksum = sha256(quarantineBytes);
        asset.setSha256Checksum(checksum);
        asset.setStatus(AssetStatus.ENCRYPTING);
        assetRepository.save(asset);

        String associatedData = asset.getProductId() + ":" + asset.getProductVersionId() + ":" + asset.getId();
        EncryptedPayload encrypted = encryptionProvider.encryptStream(
                new ByteArrayInputStream(quarantineBytes),
                associatedData
        );

        String protectedKey = asset.getOrganisationId() + "/" + asset.getProductId() + "/"
                + asset.getProductVersionId() + "/" + asset.getId() + "/protected";

        storageProvider.putObject(
                storageProperties.bucketProtected(),
                protectedKey,
                new ByteArrayInputStream(encrypted.ciphertext()),
                encrypted.ciphertext().length,
                "application/octet-stream"
        );

        asset.setStorageBucket(storageProperties.bucketProtected());
        asset.setStorageObjectKey(protectedKey);
        asset.setEncrypted(true);
        asset.setWrappedDek(encrypted.wrappedKey().ciphertext());
        asset.setDekKeyVersion(encrypted.wrappedKey().keyVersion());
        asset.setEncryptionNonce(HexFormat.of().formatHex(encrypted.nonce()));
        asset.setStatus(AssetStatus.READY);
        assetRepository.save(asset);

        storageProvider.deleteObject(storageProperties.bucketQuarantine(), asset.getQuarantineObjectKey());

        session.setStatus(UploadSessionStatus.COMPLETED);
        session.setCompletedAt(Instant.now());
        uploadSessionRepository.save(session);

        ProductVersion version = versionRepository.findById(asset.getProductVersionId()).orElseThrow();
        version.setStatus(ProductVersionStatus.UNDER_REVIEW);
        versionRepository.save(version);

        Product product = productRepository.findById(asset.getProductId()).orElseThrow();
        product.setStatus(ProductStatus.UNDER_REVIEW);
        productRepository.save(product);

        log.info("Asset {} processed and encrypted successfully", asset.getId());
    }

    private void updateProductProcessingState(ProductAsset asset) {
        Product product = productRepository.findById(asset.getProductId()).orElseThrow();
        product.setStatus(ProductStatus.PROCESSING);
        productRepository.save(product);

        ProductVersion version = versionRepository.findById(asset.getProductVersionId()).orElseThrow();
        version.setStatus(ProductVersionStatus.PROCESSING);
        versionRepository.save(version);
    }

    private void rejectAsset(ProductAsset asset, UploadSession session, String reason) {
        asset.setStatus(AssetStatus.REJECTED);
        asset.setFailureReason(reason);
        assetRepository.save(asset);

        session.setStatus(UploadSessionStatus.FAILED);
        session.setFailureReason(reason);
        uploadSessionRepository.save(session);

        Product product = productRepository.findById(asset.getProductId()).orElseThrow();
        product.setStatus(ProductStatus.DRAFT);
        productRepository.save(product);

        try {
            storageProvider.deleteObject(storageProperties.bucketQuarantine(), asset.getQuarantineObjectKey());
        } catch (Exception ex) {
            log.warn("Failed to delete quarantine object for rejected asset {}", asset.getId(), ex);
        }
    }

    private void failAsset(UploadProcessingMessage message, String reason) {
        assetRepository.findById(message.assetId()).ifPresent(asset -> {
            asset.setStatus(AssetStatus.REJECTED);
            asset.setFailureReason(reason);
            assetRepository.save(asset);
        });
        uploadSessionRepository.findById(message.uploadSessionId()).ifPresent(session -> {
            session.setStatus(UploadSessionStatus.FAILED);
            session.setFailureReason(reason);
            uploadSessionRepository.save(session);
        });
    }

    private String sha256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(data));
    }
}
