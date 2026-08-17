package com.ciphermarket.api.commerce.repository;

import com.ciphermarket.api.commerce.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByProviderAndExternalReference(String provider, String externalReference);
}
