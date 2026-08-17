package com.ciphermarket.api.commerce.api;

import com.ciphermarket.api.commerce.service.PaymentWebhookService;
import com.ciphermarket.api.config.PaymentProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/payments")
@Tag(name = "Payment Webhooks", description = "Server-to-server payment confirmations")
public class PaymentWebhookController {

    private final PaymentWebhookService paymentWebhookService;
    private final PaymentProperties paymentProperties;

    public PaymentWebhookController(
            PaymentWebhookService paymentWebhookService,
            PaymentProperties paymentProperties
    ) {
        this.paymentWebhookService = paymentWebhookService;
        this.paymentProperties = paymentProperties;
    }

    @PostMapping("/mock")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Mock payment provider webhook")
    public void mockWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Webhook-Signature") String signature,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey
    ) {
        paymentWebhookService.processWebhook(paymentProperties.mockProviderName(), payload, signature, idempotencyKey);
    }
}
