package com.ciphermarket.api.commerce.payment;

import com.ciphermarket.api.commerce.dto.MockPaymentWebhookPayload;
import com.ciphermarket.api.config.PaymentProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Component
public class PaymentWebhookSigner {

    private final PaymentProperties paymentProperties;
    private final ObjectMapper objectMapper;

    public PaymentWebhookSigner(PaymentProperties paymentProperties, ObjectMapper objectMapper) {
        this.paymentProperties = paymentProperties;
        this.objectMapper = objectMapper;
    }

    public String serializePayload(MockPaymentWebhookPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize webhook payload", e);
        }
    }

    public String hashPayload(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    paymentProperties.webhookSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Unable to sign webhook payload", e);
        }
    }

    public boolean verify(String payload, String signature) {
        if (signature == null || signature.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(sign(payload).getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
    }

    public MockPaymentWebhookPayload buildSucceededPayload(UUID paymentId, String externalReference, long amountCents, String currency) {
        return new MockPaymentWebhookPayload(
                "payment.succeeded",
                paymentId,
                externalReference,
                amountCents,
                currency,
                Instant.now().getEpochSecond()
        );
    }
}
