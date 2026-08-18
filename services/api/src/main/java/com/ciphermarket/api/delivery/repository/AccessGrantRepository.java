package com.ciphermarket.api.delivery.repository;

import com.ciphermarket.api.delivery.domain.AccessGrant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccessGrantRepository extends JpaRepository<AccessGrant, UUID> {

    Optional<AccessGrant> findByTokenHash(String tokenHash);
}
