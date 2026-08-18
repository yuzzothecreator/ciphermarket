package com.ciphermarket.api.delivery.dto;

import java.util.UUID;

public record CreateAccessGrantRequest(
        UUID deviceId
) {
}
