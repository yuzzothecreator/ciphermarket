package com.ciphermarket.api.commerce.api;

import com.ciphermarket.api.commerce.dto.SalesAnalyticsResponse;
import com.ciphermarket.api.commerce.service.SalesAnalyticsService;
import com.ciphermarket.api.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organisations/{organisationId}/analytics")
@Tag(name = "Sales analytics", description = "Creator organisation sales insights")
public class SalesAnalyticsController {

    private final SalesAnalyticsService salesAnalyticsService;

    public SalesAnalyticsController(SalesAnalyticsService salesAnalyticsService) {
        this.salesAnalyticsService = salesAnalyticsService;
    }

    @GetMapping("/sales")
    @Operation(summary = "Paid sales summary for the organisation")
    public SalesAnalyticsResponse sales(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID organisationId
    ) {
        return salesAnalyticsService.getSales(user, organisationId);
    }
}
