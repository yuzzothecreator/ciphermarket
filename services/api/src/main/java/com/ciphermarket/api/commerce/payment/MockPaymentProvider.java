package com.ciphermarket.api.commerce.payment;

import com.ciphermarket.api.commerce.domain.Payment;
import com.ciphermarket.api.commerce.dto.MockPaymentWebhookPayload;
import com.ciphermarket.api.commerce.repository.OrderRepository;
import com.ciphermarket.api.commerce.repository.PaymentRepository;
import com.ciphermarket.api.commerce.service.PaymentWebhookService;
import com.ciphermarket.api.common.exception.AccessDeniedException;
import com.ciphermarket.api.common.exception.ResourceNotFoundException;
import com.ciphermarket.api.config.PaymentProperties;
import com.ciphermarket.api.identity.service.UserProfileService;
import com.ciphermarket.api.security.AuthenticatedUser;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentProvider {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentWebhookService paymentWebhookService;
    private final PaymentWebhookSigner signer;
    private final PaymentProperties paymentProperties;
    private final UserProfileService userProfileService;

    public MockPaymentProvider(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            PaymentWebhookService paymentWebhookService,
            PaymentWebhookSigner signer,
            PaymentProperties paymentProperties,
            UserProfileService userProfileService
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentWebhookService = paymentWebhookService;
        this.signer = signer;
        this.paymentProperties = paymentProperties;
        this.userProfileService = userProfileService;
    }

    public void simulatePayment(AuthenticatedUser user, UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        var profile = userProfileService.requireProfileEntity(user.keycloakSub());
        var order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getBuyerUserId().equals(profile.getId())) {
            throw new AccessDeniedException("Payment does not belong to current user");
        }

        deliverSucceededWebhook(payment);
    }

    public void deliverSucceededWebhook(Payment payment) {
        MockPaymentWebhookPayload payload = signer.buildSucceededPayload(
                payment.getId(),
                payment.getExternalReference(),
                payment.getAmountCents(),
                payment.getCurrency()
        );
        String serialized = signer.serializePayload(payload);
        String signature = signer.sign(serialized);
        String idempotencyKey = payment.getExternalReference() + ":succeeded";

        paymentWebhookService.processWebhook(
                paymentProperties.mockProviderName(),
                serialized,
                signature,
                idempotencyKey
        );
    }
}
