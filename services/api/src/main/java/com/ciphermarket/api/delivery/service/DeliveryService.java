package com.ciphermarket.api.delivery.service;

import com.ciphermarket.api.common.enums.DownloadOutcome;
import com.ciphermarket.api.common.enums.ProductType;
import com.ciphermarket.api.common.exception.ResourceNotFoundException;
import com.ciphermarket.api.delivery.domain.AccessGrant;
import com.ciphermarket.api.delivery.domain.DownloadEvent;
import com.ciphermarket.api.delivery.repository.DownloadEventRepository;
import com.ciphermarket.api.delivery.repository.LicenceRepository;
import com.ciphermarket.api.delivery.transform.PdfWatermarkService;
import com.ciphermarket.api.delivery.transform.SourceManifestService;
import com.ciphermarket.api.encryption.EncryptionProvider;
import com.ciphermarket.api.encryption.WrappedKey;
import com.ciphermarket.api.identity.repository.UserProfileRepository;
import com.ciphermarket.api.product.domain.Product;
import com.ciphermarket.api.product.domain.ProductAsset;
import com.ciphermarket.api.product.repository.ProductAssetRepository;
import com.ciphermarket.api.product.repository.ProductRepository;
import com.ciphermarket.api.securityops.service.SecurityEventService;
import com.ciphermarket.api.storage.ObjectStorageProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HexFormat;

@Service
public class DeliveryService {

    private final AccessGrantService accessGrantService;
    private final ProductAssetRepository assetRepository;
    private final ProductRepository productRepository;
    private final LicenceRepository licenceRepository;
    private final DownloadEventRepository downloadEventRepository;
    private final UserProfileRepository userProfileRepository;
    private final ObjectStorageProvider storageProvider;
    private final EncryptionProvider encryptionProvider;
    private final PdfWatermarkService pdfWatermarkService;
    private final SourceManifestService sourceManifestService;
    private final SecurityEventService securityEventService;

    public DeliveryService(
            AccessGrantService accessGrantService,
            ProductAssetRepository assetRepository,
            ProductRepository productRepository,
            LicenceRepository licenceRepository,
            DownloadEventRepository downloadEventRepository,
            UserProfileRepository userProfileRepository,
            ObjectStorageProvider storageProvider,
            EncryptionProvider encryptionProvider,
            PdfWatermarkService pdfWatermarkService,
            SourceManifestService sourceManifestService,
            SecurityEventService securityEventService
    ) {
        this.accessGrantService = accessGrantService;
        this.assetRepository = assetRepository;
        this.productRepository = productRepository;
        this.licenceRepository = licenceRepository;
        this.downloadEventRepository = downloadEventRepository;
        this.userProfileRepository = userProfileRepository;
        this.storageProvider = storageProvider;
        this.encryptionProvider = encryptionProvider;
        this.pdfWatermarkService = pdfWatermarkService;
        this.sourceManifestService = sourceManifestService;
        this.securityEventService = securityEventService;
    }

    @Transactional
    public ResponseEntity<Resource> download(String accessToken, HttpServletRequest request) {
        AccessGrant grant = accessGrantService.requireUsableGrant(accessToken);
        ProductAsset asset = assetRepository.findById(grant.getProductAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        Product product = productRepository.findById(asset.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        String clientIp = request.getRemoteAddr();
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);

        try {
            byte[] plaintext = decryptAsset(asset);
            byte[] delivered = transform(product, asset, grant, plaintext);
            accessGrantService.recordUse(grant);

            downloadEventRepository.save(DownloadEvent.record(
                    grant.getId(),
                    grant.getBuyerUserId(),
                    product.getId(),
                    asset.getId(),
                    grant.getDeviceId(),
                    DownloadOutcome.SUCCESS,
                    clientIp,
                    userAgent,
                    (long) delivered.length
            ));
            securityEventService.record(
                    product.getOrganisationId(),
                    grant.getBuyerUserId(),
                    "DOWNLOAD_SUCCESS",
                    com.ciphermarket.api.common.enums.SecurityEventSeverity.INFO,
                    "ProductAsset",
                    asset.getId(),
                    "Secure download completed",
                    java.util.Map.of("productId", product.getId())
            );

            String fileName = asset.getSanitizedFileName();
            MediaType mediaType = MediaType.parseMediaType(
                    asset.getDetectedContentType() != null ? asset.getDetectedContentType() : asset.getDeclaredContentType()
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(mediaType)
                    .contentLength(delivered.length)
                    .body(new ByteArrayResource(delivered));
        } catch (Exception e) {
            downloadEventRepository.save(DownloadEvent.record(
                    grant.getId(),
                    grant.getBuyerUserId(),
                    product.getId(),
                    asset.getId(),
                    grant.getDeviceId(),
                    DownloadOutcome.FAILED,
                    clientIp,
                    userAgent,
                    null
            ));
            throw e;
        }
    }

    private byte[] decryptAsset(ProductAsset asset) {
        String associatedData = asset.getProductId() + ":" + asset.getProductVersionId() + ":" + asset.getId();
        byte[] nonce = HexFormat.of().parseHex(asset.getEncryptionNonce());
        WrappedKey wrappedKey = new WrappedKey(asset.getWrappedDek(), asset.getDekKeyVersion());

        try (InputStream ciphertext = storageProvider.getObject(asset.getStorageBucket(), asset.getStorageObjectKey());
             ByteArrayOutputStream plaintext = new ByteArrayOutputStream()) {
            encryptionProvider.decryptStream(ciphertext, nonce, wrappedKey, associatedData, plaintext);
            return plaintext.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt asset", e);
        }
    }

    private byte[] transform(Product product, ProductAsset asset, AccessGrant grant, byte[] plaintext) {
        String buyerLabel = userProfileRepository.findById(grant.getBuyerUserId())
                .map(p -> p.getEmail() + " / " + grant.getBuyerUserId())
                .orElse(grant.getBuyerUserId().toString());

        if (product.getProductType() == ProductType.PDF) {
            return pdfWatermarkService.watermark(new java.io.ByteArrayInputStream(plaintext),
                    "Licensed to " + buyerLabel);
        }
        if (product.getProductType() == ProductType.SOURCE_CODE) {
            return sourceManifestService.attachManifest(
                    plaintext,
                    asset.getOriginalFileName(),
                    asset.getSha256Checksum(),
                    buyerLabel
            );
        }
        return plaintext;
    }
}
