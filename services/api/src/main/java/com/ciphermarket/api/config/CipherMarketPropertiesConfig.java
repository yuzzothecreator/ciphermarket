package com.ciphermarket.api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        StorageProperties.class,
        VaultProperties.class,
        UploadProperties.class,
        ClamAvProperties.class,
        MessagingProperties.class,
        PaymentProperties.class,
        DeliveryProperties.class,
        LicenceProperties.class
})
public class CipherMarketPropertiesConfig {
}
