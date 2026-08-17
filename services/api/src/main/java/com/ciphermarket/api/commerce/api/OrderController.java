package com.ciphermarket.api.commerce.api;

import com.ciphermarket.api.commerce.dto.EntitlementResponse;
import com.ciphermarket.api.commerce.dto.OrderResponse;
import com.ciphermarket.api.commerce.service.OrderService;
import com.ciphermarket.api.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Orders", description = "Buyer orders and entitlements")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/api/v1/orders")
    @Operation(summary = "List buyer orders")
    public List<OrderResponse> listOrders(@AuthenticationPrincipal AuthenticatedUser user) {
        return orderService.listMyOrders(user);
    }

    @GetMapping("/api/v1/orders/{orderId}")
    @Operation(summary = "Get order details")
    public OrderResponse getOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID orderId
    ) {
        return orderService.getOrder(user, orderId);
    }

    @GetMapping("/api/v1/entitlements")
    @Operation(summary = "List buyer entitlements")
    public List<EntitlementResponse> listEntitlements(@AuthenticationPrincipal AuthenticatedUser user) {
        return orderService.listMyEntitlements(user);
    }
}
