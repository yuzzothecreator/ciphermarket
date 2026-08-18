package com.ciphermarket.api.securityops.service;

import com.ciphermarket.api.audit.service.AuditService;
import com.ciphermarket.api.commerce.domain.Entitlement;
import com.ciphermarket.api.commerce.repository.EntitlementRepository;
import com.ciphermarket.api.common.enums.ApprovalActionType;
import com.ciphermarket.api.common.enums.ApprovalStatus;
import com.ciphermarket.api.common.enums.ProductStatus;
import com.ciphermarket.api.common.enums.SecurityEventSeverity;
import com.ciphermarket.api.common.exception.ResourceNotFoundException;
import com.ciphermarket.api.delivery.repository.LicenceRepository;
import com.ciphermarket.api.identity.domain.UserProfile;
import com.ciphermarket.api.identity.service.UserProfileService;
import com.ciphermarket.api.product.domain.Product;
import com.ciphermarket.api.product.repository.ProductRepository;
import com.ciphermarket.api.security.AuthenticatedUser;
import com.ciphermarket.api.securityops.domain.ApprovalRequest;
import com.ciphermarket.api.securityops.dto.ApprovalRequestResponse;
import com.ciphermarket.api.securityops.dto.CreateApprovalRequest;
import com.ciphermarket.api.securityops.dto.DecideApprovalRequest;
import com.ciphermarket.api.securityops.repository.ApprovalRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ApprovalService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final UserProfileService userProfileService;
    private final ProductRepository productRepository;
    private final EntitlementRepository entitlementRepository;
    private final LicenceRepository licenceRepository;
    private final AuditService auditService;
    private final SecurityEventService securityEventService;

    public ApprovalService(
            ApprovalRequestRepository approvalRequestRepository,
            UserProfileService userProfileService,
            ProductRepository productRepository,
            EntitlementRepository entitlementRepository,
            LicenceRepository licenceRepository,
            AuditService auditService,
            SecurityEventService securityEventService
    ) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.userProfileService = userProfileService;
        this.productRepository = productRepository;
        this.entitlementRepository = entitlementRepository;
        this.licenceRepository = licenceRepository;
        this.auditService = auditService;
        this.securityEventService = securityEventService;
    }

    @Transactional
    public ApprovalRequestResponse create(AuthenticatedUser user, CreateApprovalRequest request) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        String resourceType = resourceTypeFor(request.actionType());
        UUID organisationId = resolveOrganisation(request.actionType(), request.resourceId());

        ApprovalRequest approval = approvalRequestRepository.save(new ApprovalRequest(
                request.actionType(),
                resourceType,
                request.resourceId(),
                organisationId,
                request.payload(),
                request.reason(),
                profile.getId()
        ));

        auditService.record(
                organisationId,
                profile.getId(),
                user.keycloakSub(),
                "APPROVAL_REQUESTED",
                "ApprovalRequest",
                approval.getId(),
                null,
                Map.of("actionType", request.actionType().name(), "resourceId", request.resourceId())
        );
        securityEventService.record(
                organisationId,
                profile.getId(),
                "APPROVAL_REQUESTED",
                SecurityEventSeverity.MEDIUM,
                "ApprovalRequest",
                approval.getId(),
                "Maker requested " + request.actionType().name(),
                Map.of("resourceId", request.resourceId())
        );

        return ApprovalRequestResponse.from(approval);
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequestResponse> list(ApprovalStatus status) {
        List<ApprovalRequest> requests = status == null
                ? approvalRequestRepository.findTop100ByOrderByRequestedAtDesc()
                : approvalRequestRepository.findByStatusOrderByRequestedAtDesc(status);
        return requests.stream().map(ApprovalRequestResponse::from).toList();
    }

    @Transactional
    public ApprovalRequestResponse decide(AuthenticatedUser user, UUID approvalId, DecideApprovalRequest request) {
        if (request.decision() != ApprovalStatus.APPROVED && request.decision() != ApprovalStatus.REJECTED) {
            throw new IllegalArgumentException("Decision must be APPROVED or REJECTED");
        }

        UserProfile checker = userProfileService.requireProfileEntity(user.keycloakSub());
        ApprovalRequest approval = approvalRequestRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval request not found"));

        if (approval.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalArgumentException("Approval request is not pending");
        }
        MakerCheckerPolicy.assertDistinctActors(approval.getRequestedBy(), checker.getId());

        approval.decide(request.decision(), checker.getId(), request.decisionReason());
        approvalRequestRepository.save(approval);

        if (request.decision() == ApprovalStatus.APPROVED) {
            execute(approval);
        }

        auditService.record(
                approval.getOrganisationId(),
                checker.getId(),
                user.keycloakSub(),
                "APPROVAL_DECIDED",
                "ApprovalRequest",
                approval.getId(),
                Map.of("status", ApprovalStatus.PENDING.name()),
                Map.of("status", request.decision().name())
        );
        securityEventService.record(
                approval.getOrganisationId(),
                checker.getId(),
                "APPROVAL_DECIDED",
                SecurityEventSeverity.HIGH,
                "ApprovalRequest",
                approval.getId(),
                "Checker " + request.decision().name() + " " + approval.getActionType().name(),
                Map.of("decision", request.decision().name())
        );

        return ApprovalRequestResponse.from(approval);
    }

    private void execute(ApprovalRequest approval) {
        switch (approval.getActionType()) {
            case PRODUCT_SUSPEND -> {
                Product product = productRepository.findById(approval.getResourceId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
                product.setStatus(ProductStatus.SUSPENDED);
                productRepository.save(product);
            }
            case ENTITLEMENT_REVOKE -> {
                Entitlement entitlement = entitlementRepository.findById(approval.getResourceId())
                        .orElseThrow(() -> new ResourceNotFoundException("Entitlement not found"));
                entitlement.revoke();
                entitlementRepository.save(entitlement);
                licenceRepository.findByEntitlementId(entitlement.getId()).ifPresent(licence -> {
                    licence.revoke();
                    licenceRepository.save(licence);
                });
            }
            case LICENCE_REVOKE -> licenceRepository.findById(approval.getResourceId()).ifPresent(licence -> {
                licence.revoke();
                licenceRepository.save(licence);
            });
        }
    }

    private String resourceTypeFor(ApprovalActionType actionType) {
        return switch (actionType) {
            case PRODUCT_SUSPEND -> "Product";
            case ENTITLEMENT_REVOKE -> "Entitlement";
            case LICENCE_REVOKE -> "Licence";
        };
    }

    private UUID resolveOrganisation(ApprovalActionType actionType, UUID resourceId) {
        return switch (actionType) {
            case PRODUCT_SUSPEND -> productRepository.findById(resourceId).map(Product::getOrganisationId).orElse(null);
            case ENTITLEMENT_REVOKE -> null;
            case LICENCE_REVOKE -> licenceRepository.findById(resourceId).map(l -> {
                return productRepository.findById(l.getProductId()).map(Product::getOrganisationId).orElse(null);
            }).orElse(null);
        };
    }
}
