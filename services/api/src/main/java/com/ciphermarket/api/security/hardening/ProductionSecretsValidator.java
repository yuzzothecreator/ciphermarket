package com.ciphermarket.api.security.hardening;

import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fails production startup when local placeholder secrets are still configured.
 */
public final class ProductionSecretsValidator {

    private static final List<String> FORBIDDEN_FRAGMENTS = List.of(
            "change_me_in_local_env",
            "local-dev-webhook-secret-change-me",
            "dev-only-token"
    );

    private ProductionSecretsValidator() {
    }

    public static List<String> violations(Environment environment) {
        List<String> issues = new ArrayList<>();
        rejectPlaceholder(issues, "spring.datasource.password", environment.getProperty("spring.datasource.password"));
        rejectPlaceholder(issues, "spring.data.redis.password", environment.getProperty("spring.data.redis.password"));
        rejectPlaceholder(issues, "spring.rabbitmq.password", environment.getProperty("spring.rabbitmq.password"));
        rejectPlaceholder(issues, "ciphermarket.vault.token", environment.getProperty("ciphermarket.vault.token"));
        rejectPlaceholder(
                issues,
                "ciphermarket.payment.webhook-secret",
                environment.getProperty("ciphermarket.payment.webhook-secret")
        );
        rejectPlaceholder(
                issues,
                "ciphermarket.storage.secret-key",
                environment.getProperty("ciphermarket.storage.secret-key")
        );

        String licencePrivate = environment.getProperty("ciphermarket.licence.signing-private-key-base64");
        String licencePublic = environment.getProperty("ciphermarket.licence.signing-public-key-base64");
        if (isBlank(licencePrivate) || isBlank(licencePublic)) {
            issues.add("licence signing keys must be provided in production (LICENCE_SIGNING_PRIVATE_KEY / LICENCE_SIGNING_PUBLIC_KEY)");
        }
        return List.copyOf(issues);
    }

    private static void rejectPlaceholder(List<String> issues, String name, String value) {
        if (isBlank(value)) {
            issues.add(name + " must not be blank in production");
            return;
        }
        String normalised = value.toLowerCase(Locale.ROOT);
        for (String fragment : FORBIDDEN_FRAGMENTS) {
            if (normalised.contains(fragment)) {
                issues.add(name + " still uses a local development placeholder");
                return;
            }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
