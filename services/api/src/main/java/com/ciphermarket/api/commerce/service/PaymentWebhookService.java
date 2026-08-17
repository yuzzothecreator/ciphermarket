package com.ciphermarket.api.commerce.service;

import com.ciphermarket.api.audit.service.AuditService;
import com.ciphermarket.api.commerce.domain.Entitlement;
import com.ciphermarket.api.commerce.domain.Order;
import com.ciphermarket.api.commerce.domain.OrderItem;
import com.ciphermarket.api.commerce.domain.Payment;
import com.ciphermarket.api.commerce.domain.PaymentWebhookEvent;
import com.ciphermarket.api.commerce.dto.MockPaymentWebhookPayload;
import com.ciphermarket.api.commerce.payment.PaymentWebhookSigner;
import com.ciphermarket.api.commerce.repository.EntitlementRepository;
import com.ciphermarket.api.commerce.repository.OrderItemRepository;
import com.ciphermarket.api.commerce.repository.OrderRepository;
import com.ciphermarket.api.commerce.repository.PaymentRepository;
import com.ciphermarket.api.commerce.repository.PaymentWebhookEventRepository;
import com.ciphermarket.api.common.enums.PaymentStatus;
import com.ciphermarket.api.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentWebhookService {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookService.class);

    private final PaymentWebhookEventRepository webhookEventRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EntitlementRepository entitlementRepository;
    private final EntitlementService entitlementService;
    private final OrderNotificationService notificationService;
    private final PaymentWebhookSigner signer;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public PaymentWebhookService(
            PaymentWebhookEventRepository webhookEventRepository,
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            EntitlementRepository entitlementRepository,
            EntitlementService entitlementService,
            OrderNotificationService notificationService,
            PaymentWebhookSigner signer,
            AuditService auditService,
            ObjectMapper objectMapper
    ) {
        this.webhookEventRepository = webhookEventRepository;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.entitlementRepository = entitlementRepository;
        this.entitlementService = entitlementService;
        this.notificationService = notificationService;
        this.signer = signer;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void processWebhook(String provider, String payload, String signature, String idempotencyKey) {
        if (!signer.verify(payload, signature)) {
            throw new IllegalArgumentException("Invalid webhook signature");
        }

        MockPaymentWebhookPayload event = parsePayload(payload);
        if (webhookEventRepository.findByProviderAndIdempotencyKey(provider, idempotencyKey).isPresent()) {
            log.info("Ignoring duplicate webhook idempotencyKey={}", idempotencyKey);
            return;
        }

        Payment payment = paymentRepository.findById(event.paymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        PaymentWebhookEvent webhookEvent = PaymentWebhookEvent.received(
                payment.getId(),
                provider,
                idempotencyKey,
                event.eventType(),
                signer.hashPayload(payload),
                signature
        );
        webhookEventRepository.save(webhookEvent);

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            webhookEvent.markProcessed();
            return;
        }

        if (!"payment.succeeded".equals(event.eventType())) {
            payment.markFailed("Unsupported event: " + event.eventType());
            paymentRepository.save(payment);
            webhookEvent.markProcessed();
            return;
        }

        if (event.amountCents() != payment.getAmountCents()) {
            payment.markFailed("Amount mismatch");
            paymentRepository.save(payment);
            webhookEvent.markFailed("Amount mismatch");
            return;
        }

        payment.markSucceeded();
        paymentRepository.save(payment);

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.markPaid();
        orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrderIdOrderByCreatedAtAsc(order.getId());
        for (OrderItem item : items) {
            if (entitlementRepository.findByOrderItemId(item.getId()).isEmpty()) {
                Entitlement entitlement = entitlementService.grant(order.getBuyerUserId(), item);
                auditService.record(
                        item.getOrganisationId(),
                        order.getBuyerUserId(),
                        null,
                        "ENTITLEMENT_GRANTED",
                        "Entitlement",
                        entitlement.getId(),
                        null,
                        Map.of("productId", item.getProductId(), "orderId", order.getId())
                );
            }
        }

        auditService.record(
                null,
                order.getBuyerUserId(),
                null,
                "PAYMENT_CONFIRMED",
                "Payment",
                payment.getId(),
                Map.of("status", PaymentStatus.PENDING.name()),
                Map.of("status", PaymentStatus.SUCCEEDED.name(), "orderId", order.getId())
        );

        webhookEvent.markProcessed();
        webhookEventRepository.save(webhookEvent);

        try {
            notificationService.sendOrderReceipt(order, items);
        } catch (Exception e) {
            log.warn("Failed to send order receipt for order {}: {}", order.getId(), e.getMessage());
        }
    }

    private MockPaymentWebhookPayload parsePayload(String payload) {
        try {
            return objectMapper.readValue(payload, MockPaymentWebhookPayload.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid webhook payload");
        }
    }
}
