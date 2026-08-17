package com.ciphermarket.api.commerce.service;

import com.ciphermarket.api.commerce.domain.Order;
import com.ciphermarket.api.commerce.dto.EntitlementResponse;
import com.ciphermarket.api.commerce.dto.OrderItemResponse;
import com.ciphermarket.api.commerce.dto.OrderResponse;
import com.ciphermarket.api.commerce.repository.EntitlementRepository;
import com.ciphermarket.api.commerce.repository.OrderItemRepository;
import com.ciphermarket.api.commerce.repository.OrderRepository;
import com.ciphermarket.api.common.exception.ResourceNotFoundException;
import com.ciphermarket.api.identity.domain.UserProfile;
import com.ciphermarket.api.identity.service.UserProfileService;
import com.ciphermarket.api.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EntitlementRepository entitlementRepository;
    private final UserProfileService userProfileService;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            EntitlementRepository entitlementRepository,
            UserProfileService userProfileService
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.entitlementRepository = entitlementRepository;
        this.userProfileService = userProfileService;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listMyOrders(AuthenticatedUser user) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        return orderRepository.findByBuyerUserIdOrderByCreatedAtDesc(profile.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(AuthenticatedUser user, UUID orderId) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        Order order = orderRepository.findByIdAndBuyerUserId(orderId, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<EntitlementResponse> listMyEntitlements(AuthenticatedUser user) {
        UserProfile profile = userProfileService.requireProfileEntity(user.keycloakSub());
        return entitlementRepository.findByBuyerUserIdOrderByGrantedAtDesc(profile.getId()).stream()
                .map(EntitlementResponse::from)
                .toList();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = orderItemRepository.findByOrderIdOrderByCreatedAtAsc(order.getId()).stream()
                .map(OrderItemResponse::from)
                .toList();
        return OrderResponse.from(order, items);
    }
}
