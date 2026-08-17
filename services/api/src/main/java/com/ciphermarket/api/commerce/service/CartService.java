package com.ciphermarket.api.commerce.service;

import com.ciphermarket.api.commerce.domain.Cart;
import com.ciphermarket.api.commerce.domain.CartItem;
import com.ciphermarket.api.commerce.dto.AddToCartRequest;
import com.ciphermarket.api.commerce.dto.CartItemResponse;
import com.ciphermarket.api.commerce.dto.CartResponse;
import com.ciphermarket.api.commerce.dto.UpdateCartItemRequest;
import com.ciphermarket.api.commerce.repository.CartItemRepository;
import com.ciphermarket.api.commerce.repository.CartRepository;
import com.ciphermarket.api.common.enums.ProductStatus;
import com.ciphermarket.api.common.exception.ResourceNotFoundException;
import com.ciphermarket.api.identity.domain.UserProfile;
import com.ciphermarket.api.identity.service.UserProfileService;
import com.ciphermarket.api.product.domain.Product;
import com.ciphermarket.api.product.repository.ProductRepository;
import com.ciphermarket.api.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserProfileService userProfileService;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserProfileService userProfileService
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userProfileService = userProfileService;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(AuthenticatedUser user) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        return cartRepository.findByBuyerUserId(profile.getId())
                .map(this::toResponse)
                .orElseGet(() -> emptyCart(profile.getId()));
    }

    @Transactional
    public CartResponse addItem(AuthenticatedUser user, AddToCartRequest request) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        Product product = requirePublishedProduct(request.productId());

        Cart cart = cartRepository.findByBuyerUserId(profile.getId())
                .orElseGet(() -> cartRepository.save(new Cart(profile.getId())));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .map(existing -> {
                    existing.setQuantity(Math.min(99, existing.getQuantity() + request.quantity()));
                    return existing;
                })
                .orElseGet(() -> new CartItem(cart.getId(), product.getId(), request.quantity()));

        cartItemRepository.save(item);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(AuthenticatedUser user, UUID itemId, UpdateCartItemRequest request) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        Cart cart = requireCart(profile.getId());
        CartItem item = cartItemRepository.findById(itemId)
                .filter(i -> i.getCartId().equals(cart.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(AuthenticatedUser user, UUID itemId) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        Cart cart = requireCart(profile.getId());
        CartItem item = cartItemRepository.findById(itemId)
                .filter(i -> i.getCartId().equals(cart.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        cartItemRepository.delete(item);
        return toResponse(cart);
    }

    @Transactional
    public void clearCart(UUID buyerUserId) {
        cartRepository.findByBuyerUserId(buyerUserId).ifPresent(cart -> cartItemRepository.deleteByCartId(cart.getId()));
    }

    @Transactional(readOnly = true)
    public List<CartItem> requireNonEmptyCartItems(UUID buyerUserId) {
        Cart cart = requireCart(buyerUserId);
        List<CartItem> items = cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId());
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
        return items;
    }

    private Cart requireCart(UUID buyerUserId) {
        return cartRepository.findByBuyerUserId(buyerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private Product requirePublishedProduct(UUID productId) {
        return productRepository.findByIdAndStatus(productId, ProductStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Product not available for purchase"));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartIdOrderByCreatedAtAsc(cart.getId());
        List<CartItemResponse> responses = new ArrayList<>();
        long subtotal = 0;
        String currency = "USD";

        for (CartItem item : items) {
            Product product = productRepository.findByIdAndStatus(item.getProductId(), ProductStatus.PUBLISHED)
                    .orElse(null);
            if (product == null) {
                cartItemRepository.delete(item);
                continue;
            }
            responses.add(CartItemResponse.from(item, product));
            subtotal += product.getPriceCents() * item.getQuantity();
            currency = product.getCurrency();
        }

        return new CartResponse(cart.getId(), responses, subtotal, currency, responses.size());
    }

    private CartResponse emptyCart(UUID buyerUserId) {
        return new CartResponse(null, List.of(), 0, "USD", 0);
    }
}
