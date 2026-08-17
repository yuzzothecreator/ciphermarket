package com.ciphermarket.api.upload.service;

import com.ciphermarket.api.audit.service.AuditService;
import com.ciphermarket.api.common.enums.AssetStatus;
import com.ciphermarket.api.common.enums.ProductStatus;
import com.ciphermarket.api.common.enums.ProductVersionStatus;
import com.ciphermarket.api.common.enums.UploadSessionStatus;
import com.ciphermarket.api.common.exception.ResourceNotFoundException;
import com.ciphermarket.api.config.StorageProperties;
import com.ciphermarket.api.config.UploadProperties;
import com.ciphermarket.api.encryption.EncryptedPayload;
import com.ciphermarket.api.encryption.EncryptionProvider;
import com.ciphermarket.api.product.domain.Product;
import com.ciphermarket.api.product.domain.ProductAsset;
import com.ciphermarket.api.product.domain.ProductVersion;
import com.ciphermarket.api.product.domain.UploadSession;
import com.ciphermarket.api.product.dto.CreateUploadSessionRequest;
import com.ciphermarket.api.product.dto.UploadSessionResponse;
import com.ciphermarket.api.product.repository.ProductAssetRepository;
import com.ciphermarket.api.product.repository.ProductRepository;
import com.ciphermarket.api.product.repository.ProductVersionRepository;
import com.ciphermarket.api.product.repository.UploadSessionRepository;
import com.ciphermarket.api.product.service.ProductService;
import com.ciphermarket.api.organisation.service.OrganisationService;
import com.ciphermarket.api.identity.domain.UserProfile;
import com.ciphermarket.api.identity.service.UserProfileService;
import com.ciphermarket.api.security.AuthenticatedUser;
import com.ciphermarket.api.storage.ObjectStorageProvider;
import com.ciphermarket.api.upload.messaging.RabbitMqConfig;
import com.ciphermarket.api.upload.messaging.UploadProcessingMessage;
import com.ciphermarket.api.upload.validation.FileValidationService;
import com.ciphermarket.api.upload.validation.ValidationResult;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class UploadService {

    private final UploadSessionRepository uploadSessionRepository;
    private final ProductAssetRepository assetRepository;
    private final ProductRepository productRepository;
    private final ProductVersionRepository versionRepository;
    private final ProductService productService;
    private final OrganisationService organisationService;
    private final UserProfileService userProfileService;
    private final FileValidationService fileValidationService;
    private final ObjectStorageProvider storageProvider;
    private final StorageProperties storageProperties;
    private final UploadProperties uploadProperties;
    private final RabbitTemplate rabbitTemplate;
    private final AuditService auditService;

    public UploadService(
            UploadSessionRepository uploadSessionRepository,
            ProductAssetRepository assetRepository,
            ProductRepository productRepository,
            ProductVersionRepository versionRepository,
            ProductService productService,
            OrganisationService organisationService,
            UserProfileService userProfileService,
            FileValidationService fileValidationService,
            ObjectStorageProvider storageProvider,
            StorageProperties storageProperties,
            UploadProperties uploadProperties,
            RabbitTemplate rabbitTemplate,
            AuditService auditService
    ) {
        this.uploadSessionRepository = uploadSessionRepository;
        this.assetRepository = assetRepository;
        this.productRepository = productRepository;
        this.versionRepository = versionRepository;
        this.productService = productService;
        this.organisationService = organisationService;
        this.userProfileService = userProfileService;
        this.fileValidationService = fileValidationService;
        this.storageProvider = storageProvider;
        this.storageProperties = storageProperties;
        this.uploadProperties = uploadProperties;
        this.rabbitTemplate = rabbitTemplate;
        this.auditService = auditService;
    }

    @Transactional
    public UploadSessionResponse createSession(
            UUID organisationId,
            UUID productId,
            AuthenticatedUser user,
            CreateUploadSessionRequest request
    ) {
        organisationService.requireOrganisationRole(user, organisationId,
                com.ciphermarket.api.common.enums.OrganisationRole.PRODUCT_MANAGER);
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        Product product = productService.requireProduct(organisationId, productId);
        ProductVersion version = productService.requireVersion(organisationId, request.productVersionId());

        if (!version.getProductId().equals(productId)) {
            throw new IllegalArgumentException("Version does not belong to product");
        }

        ValidationResult nameValidation = fileValidationService.validateFileName(request.fileName());
        if (!nameValidation.valid()) {
            throw new IllegalArgumentException(nameValidation.reason());
        }

        ProductAsset asset = new ProductAsset(
                organisationId,
                productId,
                version.getId(),
                request.fileName(),
                nameValidation.sanitizedFileName(),
                request.contentType()
        );
        asset.setStatus(AssetStatus.UPLOADING);
        asset = assetRepository.save(asset);

        String quarantineKey = buildQuarantineKey(organisationId, productId, version.getId(), asset.getId());
        asset.setQuarantineObjectKey(quarantineKey);
        assetRepository.save(asset);

        Instant expiresAt = Instant.now().plusSeconds(uploadProperties.sessionTtlMinutes() * 60L);
        UploadSession session = new UploadSession(
                organisationId,
                productId,
                version.getId(),
                asset.getId(),
                profile.getId(),
                request.contentType(),
                request.fileName(),
                uploadProperties.maxFileSizeBytes(),
                quarantineKey,
                expiresAt
        );
        session = uploadSessionRepository.save(session);

        product.setStatus(ProductStatus.UPLOADING);
        version.setStatus(ProductVersionStatus.UPLOADING);
        productRepository.save(product);
        versionRepository.save(version);

        return UploadSessionResponse.from(session);
    }

    @Transactional
    public UploadSessionResponse uploadFile(
            UUID organisationId,
            UUID sessionId,
            AuthenticatedUser user,
            MultipartFile file
    ) {
        organisationService.requireActiveMembership(user, organisationId);
        UploadSession session = uploadSessionRepository.findByIdAndOrganisationId(sessionId, organisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Upload session not found"));

        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setStatus(UploadSessionStatus.EXPIRED);
            uploadSessionRepository.save(session);
            throw new IllegalStateException("Upload session expired");
        }

        ValidationResult sizeValidation = fileValidationService.validateSize(file.getSize());
        if (!sizeValidation.valid()) {
            throw new IllegalArgumentException(sizeValidation.reason());
        }

        ProductAsset asset = assetRepository.findById(session.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        try {
            storageProvider.putObject(
                    storageProperties.bucketQuarantine(),
                    session.getQuarantineObjectKey(),
                    file.getInputStream(),
                    file.getSize(),
                    session.getDeclaredContentType()
            );
        } catch (Exception ex) {
            session.setStatus(UploadSessionStatus.FAILED);
            session.setFailureReason("Storage upload failed: " + ex.getMessage());
            uploadSessionRepository.save(session);
            asset.setStatus(AssetStatus.REJECTED);
            asset.setFailureReason(session.getFailureReason());
            assetRepository.save(asset);
            throw new IllegalStateException("Failed to upload to quarantine storage", ex);
        }

        session.setStatus(UploadSessionStatus.UPLOADED);
        uploadSessionRepository.save(session);

        asset.setStatus(AssetStatus.QUARANTINED);
        asset.setFileSizeBytes(file.getSize());
        assetRepository.save(asset);

        String correlationId = MDC.get("correlationId");
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.UPLOAD_EXCHANGE,
                "processing",
                new UploadProcessingMessage(session.getId(), asset.getId(), organisationId, correlationId)
        );

        session.setStatus(UploadSessionStatus.PROCESSING);
        uploadSessionRepository.save(session);

        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        auditService.record(
                organisationId,
                profile.getId(),
                user.keycloakSub(),
                "UPLOAD_RECEIVED",
                "product_asset",
                asset.getId(),
                null,
                Map.of("sessionId", session.getId().toString())
        );

        return UploadSessionResponse.from(session);
    }

    @Transactional(readOnly = true)
    public UploadSessionResponse getSession(UUID organisationId, UUID sessionId, AuthenticatedUser user) {
        organisationService.requireActiveMembership(user, organisationId);
        UploadSession session = uploadSessionRepository.findByIdAndOrganisationId(sessionId, organisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Upload session not found"));
        return UploadSessionResponse.from(session);
    }

    private String buildQuarantineKey(UUID orgId, UUID productId, UUID versionId, UUID assetId) {
        return orgId + "/" + productId + "/" + versionId + "/" + assetId + "/quarantine";
    }
}
