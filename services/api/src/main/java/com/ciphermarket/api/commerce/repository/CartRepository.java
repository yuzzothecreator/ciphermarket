package com.ciphermarket.api.commerce.repository;

import com.ciphermarket.api.commerce.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByBuyerUserId(UUID buyerUserId);
}
