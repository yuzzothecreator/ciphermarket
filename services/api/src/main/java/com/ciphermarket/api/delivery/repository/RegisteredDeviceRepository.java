package com.ciphermarket.api.delivery.repository;

import com.ciphermarket.api.delivery.domain.RegisteredDevice;
import com.ciphermarket.api.common.enums.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegisteredDeviceRepository extends JpaRepository<RegisteredDevice, UUID> {

    List<RegisteredDevice> findByBuyerUserIdAndStatusOrderByRegisteredAtDesc(UUID buyerUserId, DeviceStatus status);

    Optional<RegisteredDevice> findByIdAndBuyerUserId(UUID id, UUID buyerUserId);

    Optional<RegisteredDevice> findByBuyerUserIdAndFingerprintHash(UUID buyerUserId, String fingerprintHash);
}
