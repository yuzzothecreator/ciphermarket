package com.ciphermarket.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ciphermarket.messaging")
public record MessagingProperties(
        String uploadQueue,
        String uploadDlq
) {
}
