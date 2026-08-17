package com.ciphermarket.api.commerce.repository;

import com.ciphermarket.api.commerce.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByBuyerUserIdOrderByCreatedAtDesc(UUID buyerUserId);

    Optional<Order> findByIdAndBuyerUserId(UUID id, UUID buyerUserId);
}
