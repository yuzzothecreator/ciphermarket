package com.ciphermarket.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ciphermarket.clamav")
public record ClamAvProperties(
        String host,
        int port,
        boolean enabled
) {
}
