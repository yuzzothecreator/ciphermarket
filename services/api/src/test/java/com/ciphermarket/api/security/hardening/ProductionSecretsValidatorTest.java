package com.ciphermarket.api.security.hardening;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionSecretsValidatorTest {

    @Test
    void acceptsStrongProductionValues() {
        MockEnvironment env = baseEnv()
                .withProperty("spring.datasource.password", "prod-db-secret")
                .withProperty("spring.data.redis.password", "prod-redis-secret")
                .withProperty("spring.rabbitmq.password", "prod-rmq-secret")
                .withProperty("ciphermarket.vault.token", "s.production-token")
                .withProperty("ciphermarket.payment.webhook-secret", "prod-webhook-secret")
                .withProperty("ciphermarket.storage.secret-key", "prod-storage-secret")
                .withProperty("ciphermarket.licence.signing-private-key-base64", "dGVzdA==")
                .withProperty("ciphermarket.licence.signing-public-key-base64", "dGVzdA==");

        assertThat(ProductionSecretsValidator.violations(env)).isEmpty();
    }

    @Test
    void rejectsLocalPlaceholders() {
        MockEnvironment env = baseEnv()
                .withProperty("spring.datasource.password", "change_me_in_local_env")
                .withProperty("ciphermarket.vault.token", "dev-only-token")
                .withProperty("ciphermarket.payment.webhook-secret", "local-dev-webhook-secret-change-me")
                .withProperty("ciphermarket.licence.signing-private-key-base64", "")
                .withProperty("ciphermarket.licence.signing-public-key-base64", "");

        List<String> issues = ProductionSecretsValidator.violations(env);
        assertThat(issues).isNotEmpty();
        assertThat(issues.stream().anyMatch(issue -> issue.contains("datasource.password"))).isTrue();
        assertThat(issues.stream().anyMatch(issue -> issue.contains("vault.token"))).isTrue();
        assertThat(issues.stream().anyMatch(issue -> issue.contains("webhook-secret"))).isTrue();
        assertThat(issues.stream().anyMatch(issue -> issue.contains("licence signing keys"))).isTrue();
    }

    private static MockEnvironment baseEnv() {
        return new MockEnvironment()
                .withProperty("spring.data.redis.password", "prod-redis-secret")
                .withProperty("spring.rabbitmq.password", "prod-rmq-secret")
                .withProperty("ciphermarket.storage.secret-key", "prod-storage-secret");
    }
}
