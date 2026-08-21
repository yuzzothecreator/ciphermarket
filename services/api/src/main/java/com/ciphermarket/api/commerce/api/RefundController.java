package com.ciphermarket.api.commerce.api;

import com.ciphermarket.api.commerce.dto.CreateRefundRequestBody;
import com.ciphermarket.api.commerce.dto.RefundRequestResponse;
import com.ciphermarket.api.commerce.service.RefundService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Refunds", description = "Buyer refund requests")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping("/orders/{orderId}/refund-requests")
    @Operation(summary = "Request a refund for a paid order")
    public RefundRequestResponse request(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID orderId,
            @Valid @RequestBody CreateRefundRequestBody body
    ) {
        return refundService.requestRefund(user, orderId, body);
    }

    @GetMapping("/refund-requests")
    @Operation(summary = "List my refund requests")
    public List<RefundRequestResponse> listMine(@AuthenticationPrincipal AuthenticatedUser user) {
        return refundService.listMine(user);
    }

    @PostMapping("/refund-requests/{refundId}/cancel")
    @Operation(summary = "Cancel a pending refund request")
    public RefundRequestResponse cancel(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID refundId
    ) {
        return refundService.cancel(user, refundId);
    }
}
