package com.ciphermarket.api.commerce.dto;

import com.ciphermarket.api.commerce.domain.Order;
import com.ciphermarket.api.common.enums.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        OrderStatus status,
        long subtotalCents,
        String currency,
        Instant paidAt,
        Instant createdAt,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(Order order, List<OrderItemResponse> items) {
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getSubtotalCents(),
                order.getCurrency(),
                order.getPaidAt(),
                order.getCreatedAt(),
                items
        );
    }
}
