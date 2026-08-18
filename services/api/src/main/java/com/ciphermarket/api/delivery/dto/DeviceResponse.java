package com.ciphermarket.api.delivery.dto;

import com.ciphermarket.api.delivery.domain.RegisteredDevice;

import java.time.Instant;
import java.util.UUID;

public record DeviceResponse(
        UUID id,
        String label,
        String status,
        Instant registeredAt,
        Instant lastSeenAt
) {
    public static DeviceResponse from(RegisteredDevice device) {
        return new DeviceResponse(
                device.getId(),
                device.getLabel(),
                device.getStatus().name(),
                device.getRegisteredAt(),
                device.getLastSeenAt()
        );
    }
}
