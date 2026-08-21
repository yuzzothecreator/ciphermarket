package com.ciphermarket.api.commerce.api;

import com.ciphermarket.api.commerce.dto.RefundRequestResponse;
import com.ciphermarket.api.commerce.dto.RejectRefundRequestBody;
import com.ciphermarket.api.commerce.service.RefundService;
import com.ciphermarket.api.common.enums.RefundRequestStatus;
import com.ciphermarket.api.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/refunds")
@Tag(name = "Admin refunds", description = "Marketplace refund review")
public class AdminRefundController {

    private final RefundService refundService;

    public AdminRefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @GetMapping
    @Operation(summary = "List refund requests")
    public List<RefundRequestResponse> list(@RequestParam(required = false) RefundRequestStatus status) {
        return refundService.listAll(status);
    }

    @PostMapping("/{refundId}/reject")
    @Operation(summary = "Reject a refund request")
    public RefundRequestResponse reject(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID refundId,
            @Valid @RequestBody RejectRefundRequestBody body
    ) {
        return refundService.reject(user, refundId, body);
    }

    @PostMapping("/{refundId}/submit-for-approval")
    @Operation(summary = "Submit refund for maker-checker approval")
    public RefundRequestResponse submitForApproval(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID refundId
    ) {
        return refundService.submitForApproval(user, refundId);
    }
}
