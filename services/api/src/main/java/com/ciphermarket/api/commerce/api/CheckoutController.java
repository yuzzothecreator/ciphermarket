package com.ciphermarket.api.commerce.api;

import com.ciphermarket.api.commerce.dto.CheckoutResponse;
import com.ciphermarket.api.commerce.service.CheckoutService;
import com.ciphermarket.api.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
@Tag(name = "Checkout", description = "Order checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping
    @Operation(summary = "Create order from cart and initiate payment")
    public CheckoutResponse checkout(@AuthenticationPrincipal AuthenticatedUser user) {
        return checkoutService.checkout(user);
    }
}
