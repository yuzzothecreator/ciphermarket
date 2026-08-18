package com.ciphermarket.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ciphermarket.rate-limit")
public record RateLimitProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("120") int requestsPerMinute,
        @DefaultValue("false") boolean trustForwardedFor
) {
}
