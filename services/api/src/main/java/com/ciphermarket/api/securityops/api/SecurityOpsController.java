package com.ciphermarket.api.securityops.api;

import com.ciphermarket.api.common.enums.ApprovalStatus;
import com.ciphermarket.api.common.enums.SecurityEventStatus;
import com.ciphermarket.api.identity.service.UserProfileService;
import com.ciphermarket.api.security.AuthenticatedUser;
import com.ciphermarket.api.securityops.dto.ApprovalRequestResponse;
import com.ciphermarket.api.securityops.dto.AuditBatchResponse;
import com.ciphermarket.api.securityops.dto.AuditEventResponse;
import com.ciphermarket.api.securityops.dto.AuditVerifyResponse;
import com.ciphermarket.api.securityops.dto.CreateApprovalRequest;
import com.ciphermarket.api.securityops.dto.DecideApprovalRequest;
import com.ciphermarket.api.securityops.dto.SecurityEventResponse;
import com.ciphermarket.api.securityops.service.ApprovalService;
import com.ciphermarket.api.securityops.service.AuditInvestigationService;
import com.ciphermarket.api.securityops.service.SecurityEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Security operations", description = "Audit investigation, security events, and maker-checker")
public class SecurityOpsController {

    private final AuditInvestigationService auditInvestigationService;
    private final SecurityEventService securityEventService;
    private final ApprovalService approvalService;
    private final UserProfileService userProfileService;

    public SecurityOpsController(
            AuditInvestigationService auditInvestigationService,
            SecurityEventService securityEventService,
            ApprovalService approvalService,
            UserProfileService userProfileService
    ) {
        this.auditInvestigationService = auditInvestigationService;
        this.securityEventService = securityEventService;
        this.approvalService = approvalService;
        this.userProfileService = userProfileService;
    }

    @GetMapping("/api/v1/audit/events")
    @Operation(summary = "List recent audit events")
    public List<AuditEventResponse> listAuditEvents() {
        return auditInvestigationService.listRecentEvents();
    }

    @GetMapping("/api/v1/audit/verify")
    @Operation(summary = "Verify the append-only audit hash chain")
    public AuditVerifyResponse verify() {
        return auditInvestigationService.verifyChain();
    }

    @GetMapping("/api/v1/audit/batches")
    @Operation(summary = "List sealed audit batches")
    public List<AuditBatchResponse> listBatches() {
        return auditInvestigationService.listBatches();
    }

    @PostMapping("/api/v1/admin/audit/batches")
    @Operation(summary = "Seal a tamper-evident audit batch")
    public AuditBatchResponse sealBatch(@AuthenticationPrincipal AuthenticatedUser user) {
        var profile = userProfileService.requireProfileEntity(user.keycloakSub());
        return auditInvestigationService.sealBatch(profile.getId());
    }

    @GetMapping("/api/v1/audit/security-events")
    @Operation(summary = "List recent security events")
    public List<SecurityEventResponse> listSecurityEvents(
            @RequestParam(required = false) SecurityEventStatus status
    ) {
        return securityEventService.listRecent(status);
    }

    @PostMapping("/api/v1/admin/security-events/{eventId}/acknowledge")
    @Operation(summary = "Acknowledge a security event")
    public SecurityEventResponse acknowledge(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID eventId
    ) {
        var profile = userProfileService.requireProfileEntity(user.keycloakSub());
        return securityEventService.acknowledge(eventId, profile.getId());
    }

    @GetMapping("/api/v1/audit/approvals")
    @Operation(summary = "List maker-checker approval requests")
    public List<ApprovalRequestResponse> listApprovals(
            @RequestParam(required = false) ApprovalStatus status
    ) {
        return approvalService.list(status);
    }

    @PostMapping("/api/v1/admin/approvals")
    @Operation(summary = "Create a maker-checker approval request")
    public ApprovalRequestResponse createApproval(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateApprovalRequest request
    ) {
        return approvalService.create(user, request);
    }

    @PostMapping("/api/v1/admin/approvals/{approvalId}/decide")
    @Operation(summary = "Approve or reject as checker (must not be the maker)")
    public ApprovalRequestResponse decide(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID approvalId,
            @Valid @RequestBody DecideApprovalRequest request
    ) {
        return approvalService.decide(user, approvalId, request);
    }
}
