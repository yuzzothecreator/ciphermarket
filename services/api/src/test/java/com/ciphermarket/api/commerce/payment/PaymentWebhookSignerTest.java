package com.ciphermarket.api.commerce.payment;

import com.ciphermarket.api.commerce.dto.MockPaymentWebhookPayload;
import com.ciphermarket.api.config.PaymentProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentWebhookSignerTest {

    private PaymentWebhookSigner signer;

    @BeforeEach
    void setUp() {
        PaymentProperties properties = new PaymentProperties("test-secret", "MOCK", "http://localhost:3000");
        signer = new PaymentWebhookSigner(properties, new ObjectMapper());
    }

    @Test
    void signAndVerifyRoundTrip() {
        MockPaymentWebhookPayload payload = signer.buildSucceededPayload(
                UUID.randomUUID(),
                "mock_ref_1",
                999,
                "GBP"
        );
        String serialized = signer.serializePayload(payload);
        String signature = signer.sign(serialized);

        assertThat(signer.verify(serialized, signature)).isTrue();
        assertThat(signer.verify(serialized, "invalid")).isFalse();
    }
}
