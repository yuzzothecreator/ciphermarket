package com.ciphermarket.api.commerce.service;

import com.ciphermarket.api.audit.service.AuditService;
import com.ciphermarket.api.commerce.domain.Order;
import com.ciphermarket.api.commerce.domain.OrderItem;
import com.ciphermarket.api.commerce.domain.Payment;
import com.ciphermarket.api.commerce.domain.RefundRequest;
import com.ciphermarket.api.commerce.dto.CreateRefundRequestBody;
import com.ciphermarket.api.commerce.dto.RefundRequestResponse;
import com.ciphermarket.api.commerce.dto.RejectRefundRequestBody;
import com.ciphermarket.api.commerce.repository.EntitlementRepository;
import com.ciphermarket.api.commerce.repository.OrderItemRepository;
import com.ciphermarket.api.commerce.repository.OrderRepository;
import com.ciphermarket.api.commerce.repository.PaymentRepository;
import com.ciphermarket.api.commerce.repository.RefundRequestRepository;
import com.ciphermarket.api.common.enums.ApprovalActionType;
import com.ciphermarket.api.common.enums.OrderStatus;
import com.ciphermarket.api.common.enums.PaymentStatus;
import com.ciphermarket.api.common.enums.RefundRequestStatus;
import com.ciphermarket.api.common.enums.SecurityEventSeverity;
import com.ciphermarket.api.common.exception.AccessDeniedException;
import com.ciphermarket.api.common.exception.ResourceNotFoundException;
import com.ciphermarket.api.delivery.repository.LicenceRepository;
import com.ciphermarket.api.identity.domain.UserProfile;
import com.ciphermarket.api.identity.service.UserProfileService;
import com.ciphermarket.api.security.AuthenticatedUser;
import com.ciphermarket.api.securityops.dto.CreateApprovalRequest;
import com.ciphermarket.api.securityops.service.ApprovalService;
import com.ciphermarket.api.securityops.service.SecurityEventService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RefundService {

    private final RefundRequestRepository refundRequestRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final EntitlementRepository entitlementRepository;
    private final LicenceRepository licenceRepository;
    private final UserProfileService userProfileService;
    private final AuditService auditService;
    private final SecurityEventService securityEventService;
    private final OrderNotificationService notificationService;
    private final ApprovalService approvalService;

    public RefundService(
            RefundRequestRepository refundRequestRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            PaymentRepository paymentRepository,
            EntitlementRepository entitlementRepository,
            LicenceRepository licenceRepository,
            UserProfileService userProfileService,
            AuditService auditService,
            SecurityEventService securityEventService,
            OrderNotificationService notificationService,
            @Lazy ApprovalService approvalService
    ) {
        this.refundRequestRepository = refundRequestRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.entitlementRepository = entitlementRepository;
        this.licenceRepository = licenceRepository;
        this.userProfileService = userProfileService;
        this.auditService = auditService;
        this.securityEventService = securityEventService;
        this.notificationService = notificationService;
        this.approvalService = approvalService;
    }

    @Transactional
    public RefundRequestResponse requestRefund(AuthenticatedUser user, UUID orderId, CreateRefundRequestBody body) {
        UserProfile buyer = userProfileService.requireProfileEntity(user.keycloakSub());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getBuyerUserId().equals(buyer.getId())) {
            throw new AccessDeniedException("Order does not belong to current user");
        }
        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalArgumentException("Only paid orders can be refunded");
        }
        if (refundRequestRepository.existsByOrderIdAndStatusIn(
                orderId,
                EnumSet.of(
                        RefundRequestStatus.REQUESTED,
                        RefundRequestStatus.UNDER_REVIEW,
                        RefundRequestStatus.APPROVED
                )
        )) {
            throw new IllegalArgumentException("An open refund request already exists for this order");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));
        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
            throw new IllegalArgumentException("Payment must have succeeded before refund");
        }

        List<OrderItem> items = orderItemRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        UUID organisationId = items.isEmpty() ? null : items.getFirst().getOrganisationId();

        RefundRequest refund = refundRequestRepository.save(new RefundRequest(
                orderId,
                payment.getId(),
                buyer.getId(),
                organisationId,
                order.getSubtotalCents(),
                order.getCurrency(),
                body.reason().trim()
        ));

        auditService.record(
                organisationId,
                buyer.getId(),
                user.keycloakSub(),
                "REFUND_REQUESTED",
                "RefundRequest",
                refund.getId(),
                null,
                Map.of("orderId", orderId.toString(), "amountCents", refund.getAmountCents())
        );
        securityEventService.record(
                organisationId,
                buyer.getId(),
                "REFUND_REQUESTED",
                SecurityEventSeverity.MEDIUM,
                "RefundRequest",
                refund.getId(),
                "Buyer requested refund for order",
                Map.of("orderId", orderId)
        );

        return RefundRequestResponse.from(refund);
    }

    @Transactional(readOnly = true)
    public List<RefundRequestResponse> listMine(AuthenticatedUser user) {
        UserProfile buyer = userProfileService.requireProfileEntity(user.keycloakSub());
        return refundRequestRepository.findByBuyerUserIdOrderByRequestedAtDesc(buyer.getId()).stream()
                .map(RefundRequestResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RefundRequestResponse> listAll(RefundRequestStatus status) {
        List<RefundRequest> requests = status == null
                ? refundRequestRepository.findTop100ByOrderByRequestedAtDesc()
                : refundRequestRepository.findByStatusOrderByRequestedAtDesc(status);
        return requests.stream().map(RefundRequestResponse::from).toList();
    }

    @Transactional
    public RefundRequestResponse cancel(AuthenticatedUser user, UUID refundId) {
        UserProfile buyer = userProfileService.requireProfileEntity(user.keycloakSub());
        RefundRequest refund = refundRequestRepository.findByIdAndBuyerUserId(refundId, buyer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Refund request not found"));
        if (!RefundPolicy.canCancel(refund.getStatus())) {
            throw new IllegalArgumentException("Refund cannot be cancelled in status " + refund.getStatus());
        }
        refund.markCancelled();
        return RefundRequestResponse.from(refundRequestRepository.save(refund));
    }

    @Transactional
    public RefundRequestResponse reject(AuthenticatedUser user, UUID refundId, RejectRefundRequestBody body) {
        requireMarketplaceAdmin(user);
        UserProfile admin = userProfileService.requireProfileEntity(user.keycloakSub());
        RefundRequest refund = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund request not found"));
        if (!RefundPolicy.canReject(refund.getStatus())) {
            throw new IllegalArgumentException("Refund cannot be rejected in status " + refund.getStatus());
        }
        refund.markRejected(body.rejectionReason().trim());
        refundRequestRepository.save(refund);

        auditService.record(
                refund.getOrganisationId(),
                admin.getId(),
                user.keycloakSub(),
                "REFUND_REJECTED",
                "RefundRequest",
                refund.getId(),
                null,
                Map.of("reason", body.rejectionReason())
        );
        notificationService.sendRefundDecision(refund, false);
        return RefundRequestResponse.from(refund);
    }

    @Transactional
    public RefundRequestResponse submitForApproval(AuthenticatedUser user, UUID refundId) {
        requireMarketplaceAdmin(user);
        UserProfile admin = userProfileService.requireProfileEntity(user.keycloakSub());
        RefundRequest refund = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund request not found"));
        if (!RefundPolicy.canSubmitForApproval(refund.getStatus())) {
            throw new IllegalArgumentException("Refund cannot enter review in status " + refund.getStatus());
        }

        var approval = approvalService.create(user, new CreateApprovalRequest(
                ApprovalActionType.REFUND_APPROVE,
                refund.getId(),
                "Refund approval for order " + refund.getOrderId() + ": " + refund.getReason(),
                Map.of(
                        "orderId", refund.getOrderId().toString(),
                        "amountCents", refund.getAmountCents(),
                        "currency", refund.getCurrency()
                )
        ));

        refund.markUnderReview(approval.id());
        refundRequestRepository.save(refund);

        auditService.record(
                refund.getOrganisationId(),
                admin.getId(),
                user.keycloakSub(),
                "REFUND_SUBMITTED_FOR_APPROVAL",
                "RefundRequest",
                refund.getId(),
                null,
                Map.of("approvalRequestId", approval.id())
        );

        return RefundRequestResponse.from(refund);
    }

    /**
     * Called by maker-checker after REFUND_APPROVE is approved by a distinct checker.
     */
    @Transactional
    public void executeApprovedRefund(UUID refundRequestId) {
        RefundRequest refund = refundRequestRepository.findById(refundRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund request not found"));
        if (!RefundPolicy.canExecute(refund.getStatus())) {
            throw new IllegalStateException("Refund cannot be executed in status " + refund.getStatus());
        }

        refund.markApproved();
        refundRequestRepository.save(refund);

        Order order = orderRepository.findById(refund.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        Payment payment = paymentRepository.findById(refund.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        String providerRef = "mock-refund-" + refund.getId();
        entitlementRepository.findByOrderId(order.getId()).forEach(entitlement -> {
            entitlement.revoke();
            entitlementRepository.save(entitlement);
            licenceRepository.findByEntitlementId(entitlement.getId()).ifPresent(licence -> {
                licence.revoke();
                licenceRepository.save(licence);
            });
        });

        order.markRefunded();
        orderRepository.save(order);
        payment.markRefunded();
        paymentRepository.save(payment);

        refund.markCompleted(providerRef);
        refundRequestRepository.save(refund);

        auditService.record(
                refund.getOrganisationId(),
                null,
                null,
                "REFUND_COMPLETED",
                "RefundRequest",
                refund.getId(),
                null,
                Map.of("orderId", order.getId().toString(), "providerRefundRef", providerRef)
        );
        securityEventService.record(
                refund.getOrganisationId(),
                null,
                "REFUND_COMPLETED",
                SecurityEventSeverity.HIGH,
                "RefundRequest",
                refund.getId(),
                "Refund completed; entitlements revoked",
                Map.of("orderId", order.getId(), "amountCents", refund.getAmountCents())
        );
        notificationService.sendRefundDecision(refund, true);
    }

    private void requireMarketplaceAdmin(AuthenticatedUser user) {
        if (!user.hasRole("marketplace_admin")) {
            throw new AccessDeniedException("Marketplace admin role required");
        }
    }
}
