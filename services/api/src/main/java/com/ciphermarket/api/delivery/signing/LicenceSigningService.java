package com.ciphermarket.api.delivery.signing;

import com.ciphermarket.api.config.LicenceProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Component
public class LicenceSigningService {

    private static final Logger log = LoggerFactory.getLogger(LicenceSigningService.class);

    private final LicenceProperties properties;
    private final ObjectMapper objectMapper;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public LicenceSigningService(LicenceProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void initKeys() {
        try {
            if (properties.signingPrivateKeyBase64() != null && !properties.signingPrivateKeyBase64().isBlank()) {
                byte[] privateBytes = Base64.getDecoder().decode(properties.signingPrivateKeyBase64());
                byte[] publicBytes = Base64.getDecoder().decode(properties.signingPublicKeyBase64());
                KeyFactory factory = KeyFactory.getInstance("Ed25519");
                privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
                publicKey = factory.generatePublic(new X509EncodedKeySpec(publicBytes));
            } else {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("Ed25519");
                KeyPair pair = generator.generateKeyPair();
                privateKey = pair.getPrivate();
                publicKey = pair.getPublic();
                log.warn("Using ephemeral Ed25519 licence signing key — configure ciphermarket.licence for production");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize licence signing keys", e);
        }
    }

    public SignedLicencePayload sign(LicenceClaims claims) {
        try {
            String payload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(claims));
            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(privateKey);
            signature.update(payload.getBytes(StandardCharsets.UTF_8));
            String sig = Base64.getUrlEncoder().withoutPadding().encodeToString(signature.sign());
            return new SignedLicencePayload(payload, sig);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign licence", e);
        }
    }

    public boolean verify(String payload, String signatureBase64) {
        try {
            Signature signature = Signature.getInstance("Ed25519");
            signature.initVerify(publicKey);
            signature.update(payload.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getUrlDecoder().decode(signatureBase64));
        } catch (Exception e) {
            return false;
        }
    }

    public String hashFingerprint(String value) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public String signRaw(byte[] data) {
        try {
            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(privateKey);
            signature.update(data);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign data", e);
        }
    }

    public record LicenceClaims(
            UUID licenceId,
            UUID entitlementId,
            UUID buyerUserId,
            UUID productId,
            UUID productVersionId,
            long issuedAtEpochSeconds,
            long expiresAtEpochSeconds
    ) {
    }

    public record SignedLicencePayload(String payload, String signature) {
    }
}
