package com.ciphermarket.api.identity.repository;

import com.ciphermarket.api.identity.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByKeycloakSub(String keycloakSub);

    Optional<UserProfile> findByEmail(String email);

    boolean existsByKeycloakSub(String keycloakSub);
}
