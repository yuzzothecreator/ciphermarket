package com.ciphermarket.api.disclosure.service;

import com.ciphermarket.api.audit.service.AuditService;
import com.ciphermarket.api.common.enums.DisclosureDocumentStatus;
import com.ciphermarket.api.common.enums.DisclosureRequestStatus;
import com.ciphermarket.api.common.enums.SecurityEventSeverity;
import com.ciphermarket.api.common.exception.AccessDeniedException;
import com.ciphermarket.api.common.exception.ResourceNotFoundException;
import com.ciphermarket.api.config.StorageProperties;
import com.ciphermarket.api.disclosure.domain.DisclosureAccessEvent;
import com.ciphermarket.api.disclosure.domain.DisclosureDocument;
import com.ciphermarket.api.disclosure.domain.DisclosureRequest;
import com.ciphermarket.api.disclosure.dto.CreateDisclosureRequestBody;
import com.ciphermarket.api.disclosure.dto.DisclosureDocumentResponse;
import com.ciphermarket.api.disclosure.dto.DisclosureRequestResponse;
import com.ciphermarket.api.disclosure.repository.DisclosureAccessEventRepository;
import com.ciphermarket.api.disclosure.repository.DisclosureDocumentRepository;
import com.ciphermarket.api.disclosure.repository.DisclosureRequestRepository;
import com.ciphermarket.api.encryption.EncryptedPayload;
import com.ciphermarket.api.encryption.EncryptionProvider;
import com.ciphermarket.api.encryption.WrappedKey;
import com.ciphermarket.api.identity.domain.UserProfile;
import com.ciphermarket.api.identity.repository.UserProfileRepository;
import com.ciphermarket.api.identity.service.UserProfileService;
import com.ciphermarket.api.organisation.domain.OrganisationMembership;
import com.ciphermarket.api.organisation.service.OrganisationService;
import com.ciphermarket.api.security.AuthenticatedUser;
import com.ciphermarket.api.securityops.service.SecurityEventService;
import com.ciphermarket.api.storage.ObjectStorageProvider;
import com.ciphermarket.api.upload.scan.ClamAvScanner;
import com.ciphermarket.api.upload.scan.ScanResult;
import com.ciphermarket.api.upload.validation.FileValidationService;
import com.ciphermarket.api.upload.validation.ValidationResult;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DisclosureService {

    private final DisclosureDocumentRepository documentRepository;
    private final DisclosureRequestRepository requestRepository;
    private final DisclosureAccessEventRepository accessEventRepository;
    private final OrganisationService organisationService;
    private final UserProfileService userProfileService;
    private final UserProfileRepository userProfileRepository;
    private final ObjectStorageProvider storageProvider;
    private final StorageProperties storageProperties;
    private final FileValidationService fileValidationService;
    private final ClamAvScanner clamAvScanner;
    private final EncryptionProvider encryptionProvider;
    private final AuditService auditService;
    private final SecurityEventService securityEventService;
    private final DisclosureNotificationService notificationService;

    public DisclosureService(
            DisclosureDocumentRepository documentRepository,
            DisclosureRequestRepository requestRepository,
            DisclosureAccessEventRepository accessEventRepository,
            OrganisationService organisationService,
            UserProfileService userProfileService,
            UserProfileRepository userProfileRepository,
            ObjectStorageProvider storageProvider,
            StorageProperties storageProperties,
            FileValidationService fileValidationService,
            ClamAvScanner clamAvScanner,
            EncryptionProvider encryptionProvider,
            AuditService auditService,
            SecurityEventService securityEventService,
            DisclosureNotificationService notificationService
    ) {
        this.documentRepository = documentRepository;
        this.requestRepository = requestRepository;
        this.accessEventRepository = accessEventRepository;
        this.organisationService = organisationService;
        this.userProfileService = userProfileService;
        this.userProfileRepository = userProfileRepository;
        this.storageProvider = storageProvider;
        this.storageProperties = storageProperties;
        this.fileValidationService = fileValidationService;
        this.clamAvScanner = clamAvScanner;
        this.encryptionProvider = encryptionProvider;
        this.auditService = auditService;
        this.securityEventService = securityEventService;
        this.notificationService = notificationService;
    }

    @Transactional
    public DisclosureDocumentResponse uploadDocument(
            UUID organisationId,
            AuthenticatedUser user,
            String title,
            String description,
            MultipartFile file
    ) {
        requireProductManager(user, organisationId);
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        ValidationResult nameValidation = fileValidationService.validateFileName(file.getOriginalFilename());
        if (!nameValidation.valid()) {
            throw new IllegalArgumentException(nameValidation.reason());
        }
        ValidationResult sizeValidation = fileValidationService.validateSize(file.getSize());
        if (!sizeValidation.valid()) {
            throw new IllegalArgumentException(sizeValidation.reason());
        }

        String sanitized = fileValidationService.sanitizeFileName(file.getOriginalFilename());
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        DisclosureDocument document = new DisclosureDocument(
                organisationId,
                profile.getId(),
                title.trim(),
                description,
                file.getOriginalFilename(),
                sanitized,
                contentType
        );
        document = documentRepository.save(document);

        String quarantineKey = organisationId + "/disclosures/" + document.getId() + "/quarantine";
        try {
            byte[] bytes = file.getBytes();
            storageProvider.putObject(
                    storageProperties.bucketQuarantine(),
                    quarantineKey,
                    new ByteArrayInputStream(bytes),
                    bytes.length,
                    contentType
            );
            document.setQuarantineObjectKey(quarantineKey);
            document.setFileSizeBytes((long) bytes.length);
            documentRepository.save(document);
            processDocument(document, bytes);
        } catch (IllegalArgumentException ex) {
            failDocument(document, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            failDocument(document, ex.getMessage());
            throw new IllegalStateException("Disclosure upload failed: " + ex.getMessage(), ex);
        }

        auditService.record(
                organisationId,
                profile.getId(),
                user.keycloakSub(),
                "DISCLOSURE_DOCUMENT_UPLOADED",
                "DisclosureDocument",
                document.getId(),
                null,
                Map.of("title", document.getTitle(), "sha256", document.getSha256Checksum())
        );

        return DisclosureDocumentResponse.from(document);
    }

    @Transactional(readOnly = true)
    public List<DisclosureDocumentResponse> listDocuments(UUID organisationId, AuthenticatedUser user) {
        organisationService.requireActiveMembership(user, organisationId);
        return documentRepository.findByOrganisationIdOrderByCreatedAtDesc(organisationId).stream()
                .map(DisclosureDocumentResponse::from)
                .toList();
    }

    @Transactional
    public DisclosureRequestResponse createRequest(
            UUID organisationId,
            UUID documentId,
            AuthenticatedUser user,
            CreateDisclosureRequestBody body
    ) {
        requireProductManager(user, organisationId);
        UserProfile creator = userProfileService.requireProfileEntity(user.keycloakSub());

        DisclosureDocument document = documentRepository.findByIdAndOrganisationId(documentId, organisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Disclosure document not found"));
        if (document.getStatus() != DisclosureDocumentStatus.READY) {
            throw new IllegalArgumentException("Document must be READY before disclosure");
        }

        UserProfile recipient = userProfileRepository.findByEmail(body.recipientEmail().trim().toLowerCase())
                .or(() -> userProfileRepository.findByEmail(body.recipientEmail().trim()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Recipient must have a CipherMarket account. Invite them to register first."
                ));

        if (recipient.getId().equals(creator.getId())) {
            throw new IllegalArgumentException("Cannot disclose a document to yourself");
        }

        if (requestRepository.existsByDocumentIdAndRecipientUserIdAndStatus(
                documentId, recipient.getId(), DisclosureRequestStatus.PENDING
        )) {
            throw new IllegalArgumentException("A pending disclosure already exists for this recipient");
        }

        Instant expiresAt = body.expiresAt();
        if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Expiration must be in the future");
        }

        DisclosureRequest request = new DisclosureRequest(
                organisationId,
                documentId,
                creator.getId(),
                recipient.getId(),
                recipient.getEmail(),
                body.confidentialityTerms().trim(),
                expiresAt
        );
        request = requestRepository.save(request);

        recordAccessEvent(
                organisationId,
                request.getId(),
                documentId,
                creator.getId(),
                "REQUEST_CREATED",
                Map.of("recipientEmail", recipient.getEmail())
        );

        auditService.record(
                organisationId,
                creator.getId(),
                user.keycloakSub(),
                "DISCLOSURE_REQUEST_CREATED",
                "DisclosureRequest",
                request.getId(),
                null,
                Map.of("documentId", documentId.toString(), "recipientUserId", recipient.getId().toString())
        );
        securityEventService.record(
                organisationId,
                creator.getId(),
                "DISCLOSURE_REQUEST_CREATED",
                SecurityEventSeverity.LOW,
                "DisclosureRequest",
                request.getId(),
                "Confidential disclosure request created",
                Map.of("documentId", documentId)
        );

        notificationService.sendDisclosureInvite(recipient.getEmail(), document.getTitle(), organisationId.toString());

        return DisclosureRequestResponse.from(
                request,
                document.getTitle(),
                document.getSha256Checksum(),
                document.getDocumentVersion()
        );
    }

    @Transactional(readOnly = true)
    public List<DisclosureRequestResponse> listOrganisationRequests(UUID organisationId, AuthenticatedUser user) {
        organisationService.requireActiveMembership(user, organisationId);
        return requestRepository.findByOrganisationIdOrderByCreatedAtDesc(organisationId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DisclosureRequestResponse> listInbox(AuthenticatedUser user) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        return requestRepository.findByRecipientUserIdOrderByCreatedAtDesc(profile.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DisclosureRequestResponse accept(UUID requestId, AuthenticatedUser user) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        DisclosureRequest request = requestRepository.findByIdAndRecipientUserId(requestId, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Disclosure request not found"));

        expireIfNeeded(request);
        if (!DisclosureAccessPolicy.canAccept(request.getStatus(), request.getExpiresAt(), Instant.now())) {
            throw new IllegalArgumentException("Disclosure cannot be accepted in status " + request.getStatus());
        }

        request.accept();
        requestRepository.save(request);

        recordAccessEvent(
                request.getOrganisationId(),
                request.getId(),
                request.getDocumentId(),
                profile.getId(),
                "TERMS_ACCEPTED",
                Map.of("acceptedAt", request.getAcceptedAt().toString())
        );
        auditService.record(
                request.getOrganisationId(),
                profile.getId(),
                user.keycloakSub(),
                "DISCLOSURE_TERMS_ACCEPTED",
                "DisclosureRequest",
                request.getId(),
                null,
                Map.of("documentId", request.getDocumentId().toString())
        );

        return toResponse(request);
    }

    @Transactional
    public DisclosureRequestResponse reject(UUID requestId, AuthenticatedUser user, String note) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        DisclosureRequest request = requestRepository.findByIdAndRecipientUserId(requestId, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Disclosure request not found"));

        if (!DisclosureAccessPolicy.canReject(request.getStatus())) {
            throw new IllegalArgumentException("Disclosure cannot be rejected in status " + request.getStatus());
        }

        request.reject(note);
        requestRepository.save(request);

        recordAccessEvent(
                request.getOrganisationId(),
                request.getId(),
                request.getDocumentId(),
                profile.getId(),
                "TERMS_REJECTED",
                Map.of("note", note == null ? "" : note)
        );
        return toResponse(request);
    }

    @Transactional
    public DisclosureRequestResponse revokeRequest(UUID organisationId, UUID requestId, AuthenticatedUser user) {
        requireProductManager(user, organisationId);
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());

        DisclosureRequest request = requestRepository.findByIdAndOrganisationId(requestId, organisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Disclosure request not found"));
        if (!DisclosureAccessPolicy.canRevoke(request.getStatus())) {
            throw new IllegalArgumentException("Disclosure cannot be revoked in status " + request.getStatus());
        }

        request.revoke();
        requestRepository.save(request);

        recordAccessEvent(
                organisationId,
                request.getId(),
                request.getDocumentId(),
                profile.getId(),
                "REQUEST_REVOKED",
                Map.of()
        );
        auditService.record(
                organisationId,
                profile.getId(),
                user.keycloakSub(),
                "DISCLOSURE_REQUEST_REVOKED",
                "DisclosureRequest",
                request.getId(),
                null,
                Map.of()
        );
        securityEventService.record(
                organisationId,
                profile.getId(),
                "DISCLOSURE_REQUEST_REVOKED",
                SecurityEventSeverity.MEDIUM,
                "DisclosureRequest",
                request.getId(),
                "Confidential disclosure access revoked",
                Map.of("documentId", request.getDocumentId())
        );

        return toResponse(request);
    }

    @Transactional
    public ResponseEntity<Resource> download(UUID requestId, AuthenticatedUser user) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        DisclosureRequest request = requestRepository.findByIdAndRecipientUserId(requestId, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Disclosure request not found"));

        expireIfNeeded(request);
        DisclosureDocument document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new ResourceNotFoundException("Disclosure document not found"));

        if (!DisclosureAccessPolicy.canDownload(
                request.getStatus(),
                document.getStatus(),
                request.getExpiresAt(),
                Instant.now()
        )) {
            throw new AccessDeniedException("Disclosure download is not permitted");
        }

        byte[] plaintext = decryptDocument(document);

        recordAccessEvent(
                request.getOrganisationId(),
                request.getId(),
                document.getId(),
                profile.getId(),
                "ACCESS_GRANTED_DOWNLOAD",
                Map.of("bytes", plaintext.length)
        );
        auditService.record(
                request.getOrganisationId(),
                profile.getId(),
                user.keycloakSub(),
                "DISCLOSURE_DOWNLOADED",
                "DisclosureDocument",
                document.getId(),
                null,
                Map.of("requestId", request.getId().toString(), "sha256", document.getSha256Checksum())
        );

        MediaType mediaType = MediaType.parseMediaType(
                document.getDetectedContentType() != null
                        ? document.getDetectedContentType()
                        : document.getDeclaredContentType()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getSanitizedFileName() + "\"")
                .contentType(mediaType)
                .contentLength(plaintext.length)
                .body(new ByteArrayResource(plaintext));
    }

    private void processDocument(DisclosureDocument document, byte[] quarantineBytes) throws Exception {
        String detectedMime = fileValidationService.detectMimeType(
                new ByteArrayInputStream(quarantineBytes),
                document.getOriginalFileName()
        );
        document.setDetectedContentType(detectedMime);

        ValidationResult mimeValidation = fileValidationService.validateMimeType(
                detectedMime,
                document.getDeclaredContentType()
        );
        if (!mimeValidation.valid()) {
            throw new IllegalArgumentException(mimeValidation.reason());
        }

        ScanResult scanResult = clamAvScanner.scan(new ByteArrayInputStream(quarantineBytes));
        document.setScanStatus(scanResult.status().name());
        if (!scanResult.isClean()) {
            throw new IllegalArgumentException("Malware scan failed: " + scanResult.details());
        }

        String checksum = sha256(quarantineBytes);
        document.setSha256Checksum(checksum);

        String associatedData = document.getOrganisationId() + ":" + document.getId() + ":v"
                + document.getDocumentVersion();
        EncryptedPayload encrypted = encryptionProvider.encryptStream(
                new ByteArrayInputStream(quarantineBytes),
                associatedData
        );

        String protectedKey = document.getOrganisationId() + "/disclosures/" + document.getId() + "/protected";
        storageProvider.putObject(
                storageProperties.bucketProtected(),
                protectedKey,
                new ByteArrayInputStream(encrypted.ciphertext()),
                encrypted.ciphertext().length,
                "application/octet-stream"
        );

        document.setStorageBucket(storageProperties.bucketProtected());
        document.setStorageObjectKey(protectedKey);
        document.setEncrypted(true);
        document.setWrappedDek(encrypted.wrappedKey().ciphertext());
        document.setDekKeyVersion(encrypted.wrappedKey().keyVersion());
        document.setEncryptionNonce(HexFormat.of().formatHex(encrypted.nonce()));
        document.setStatus(DisclosureDocumentStatus.READY);
        documentRepository.save(document);

        if (document.getQuarantineObjectKey() != null) {
            storageProvider.deleteObject(storageProperties.bucketQuarantine(), document.getQuarantineObjectKey());
        }
    }

    private byte[] decryptDocument(DisclosureDocument document) {
        String associatedData = document.getOrganisationId() + ":" + document.getId() + ":v"
                + document.getDocumentVersion();
        byte[] nonce = HexFormat.of().parseHex(document.getEncryptionNonce());
        WrappedKey wrappedKey = new WrappedKey(document.getWrappedDek(), document.getDekKeyVersion());

        try (InputStream ciphertext = storageProvider.getObject(
                document.getStorageBucket(), document.getStorageObjectKey()
        );
             ByteArrayOutputStream plaintext = new ByteArrayOutputStream()) {
            encryptionProvider.decryptStream(ciphertext, nonce, wrappedKey, associatedData, plaintext);
            return plaintext.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt disclosure document", e);
        }
    }

    private void failDocument(DisclosureDocument document, String reason) {
        document.setStatus(DisclosureDocumentStatus.FAILED);
        document.setFailureReason(reason);
        documentRepository.save(document);
        if (document.getQuarantineObjectKey() != null) {
            try {
                storageProvider.deleteObject(storageProperties.bucketQuarantine(), document.getQuarantineObjectKey());
            } catch (Exception ignored) {
                // best-effort cleanup
            }
        }
    }

    private void expireIfNeeded(DisclosureRequest request) {
        if (request.getStatus() == DisclosureRequestStatus.PENDING
                && request.getExpiresAt() != null
                && Instant.now().isAfter(request.getExpiresAt())) {
            request.markExpired();
            requestRepository.save(request);
        }
    }

    private void requireProductManager(AuthenticatedUser user, UUID organisationId) {
        OrganisationMembership membership = organisationService.requireActiveMembership(user, organisationId);
        if (!membership.getRole().canManageProducts()) {
            throw new AccessDeniedException("Product manager role required for confidential disclosures");
        }
    }

    private DisclosureRequestResponse toResponse(DisclosureRequest request) {
        DisclosureDocument document = documentRepository.findById(request.getDocumentId()).orElse(null);
        return DisclosureRequestResponse.from(
                request,
                document != null ? document.getTitle() : null,
                document != null ? document.getSha256Checksum() : null,
                document != null ? document.getDocumentVersion() : null
        );
    }

    private void recordAccessEvent(
            UUID organisationId,
            UUID requestId,
            UUID documentId,
            UUID actorUserId,
            String eventType,
            Map<String, Object> details
    ) {
        accessEventRepository.save(new DisclosureAccessEvent(
                organisationId,
                requestId,
                documentId,
                actorUserId,
                eventType,
                details
        ));
    }

    private static String sha256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(data));
    }
}
