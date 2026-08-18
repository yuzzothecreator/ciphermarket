package com.ciphermarket.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ciphermarket.delivery")
public record DeliveryProperties(
        int grantTtlMinutes,
        int maxUsesPerGrant,
        int licenceValidityDays,
        boolean requireDeviceRegistration
) {
}
