package com.ciphermarket.api.delivery.signing;

import com.ciphermarket.api.config.LicenceProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LicenceSigningServiceTest {

    private LicenceSigningService signingService;

    @BeforeEach
    void setUp() {
        signingService = new LicenceSigningService(
                new LicenceProperties("", ""),
                new ObjectMapper()
        );
        signingService.initKeys();
    }

    @Test
    void signAndVerifyLicencePayload() {
        var claims = new LicenceSigningService.LicenceClaims(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                1,
                9999999999L
        );
        var signed = signingService.sign(claims);
        assertThat(signingService.verify(signed.payload(), signed.signature())).isTrue();
    }

    @Test
    void signRawBytes() {
        String signature = signingService.signRaw("manifest".getBytes());
        assertThat(signature).isNotBlank();
    }
}
