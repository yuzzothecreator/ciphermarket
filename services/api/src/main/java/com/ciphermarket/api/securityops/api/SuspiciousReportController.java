package com.ciphermarket.api.securityops.api;

import com.ciphermarket.api.security.AuthenticatedUser;
import com.ciphermarket.api.securityops.dto.CreateSuspiciousReportRequest;
import com.ciphermarket.api.securityops.dto.SuspiciousReportResponse;
import com.ciphermarket.api.securityops.service.SuspiciousActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Buyer suspicious activity reports")
public class SuspiciousReportController {

    private final SuspiciousActivityService suspiciousActivityService;

    public SuspiciousReportController(SuspiciousActivityService suspiciousActivityService) {
        this.suspiciousActivityService = suspiciousActivityService;
    }

    @PostMapping("/suspicious")
    @Operation(summary = "Report suspicious activity for security review")
    public SuspiciousReportResponse report(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateSuspiciousReportRequest body
    ) {
        return suspiciousActivityService.submit(user, body);
    }
}
