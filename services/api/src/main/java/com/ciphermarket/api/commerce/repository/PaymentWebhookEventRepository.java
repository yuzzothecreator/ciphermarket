package com.ciphermarket.api.commerce.repository;

import com.ciphermarket.api.commerce.domain.PaymentWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentWebhookEventRepository extends JpaRepository<PaymentWebhookEvent, UUID> {

    Optional<PaymentWebhookEvent> findByProviderAndIdempotencyKey(String provider, String idempotencyKey);
}
