package com.ciphermarket.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ciphermarket.payment")
public record PaymentProperties(
        String webhookSecret,
        String mockProviderName,
        String appBaseUrl
) {
}
