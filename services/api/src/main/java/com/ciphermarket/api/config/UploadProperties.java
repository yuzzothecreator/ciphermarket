package com.ciphermarket.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "ciphermarket.upload")
public record UploadProperties(
        long maxFileSizeBytes,
        int sessionTtlMinutes,
        List<String> allowedExtensions
) {
}
