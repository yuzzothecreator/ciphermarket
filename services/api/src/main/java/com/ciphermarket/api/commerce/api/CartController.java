package com.ciphermarket.api.commerce.api;

import com.ciphermarket.api.commerce.dto.AddToCartRequest;
import com.ciphermarket.api.commerce.dto.CartResponse;
import com.ciphermarket.api.commerce.dto.UpdateCartItemRequest;
import com.ciphermarket.api.commerce.service.CartService;
import com.ciphermarket.api.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "Buyer shopping cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Get current cart")
    public CartResponse getCart(@AuthenticationPrincipal AuthenticatedUser user) {
        return cartService.getCart(user);
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public CartResponse addItem(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody AddToCartRequest request
    ) {
        return cartService.addItem(user, request);
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public CartResponse updateItem(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return cartService.updateItem(user, itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public CartResponse removeItem(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable UUID itemId
    ) {
        return cartService.removeItem(user, itemId);
    }
}
