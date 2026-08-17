package com.ciphermarket.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ciphermarket.vault")
public record VaultProperties(
        String address,
        String token,
        String transitKey
) {
}
