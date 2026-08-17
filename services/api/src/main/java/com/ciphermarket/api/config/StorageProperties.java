package com.ciphermarket.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "ciphermarket.storage")
public record StorageProperties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucketQuarantine,
        String bucketProtected
) {
}
