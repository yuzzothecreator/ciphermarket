package com.ciphermarket.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ciphermarket.licence")
public record LicenceProperties(
        String signingPrivateKeyBase64,
        String signingPublicKeyBase64
) {
}
