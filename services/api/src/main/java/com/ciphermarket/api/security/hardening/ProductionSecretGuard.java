package com.ciphermarket.api.security.hardening;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("prod")
public class ProductionSecretGuard implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductionSecretGuard.class);

    private final Environment environment;

    public ProductionSecretGuard(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> issues = ProductionSecretsValidator.violations(environment);
        if (!issues.isEmpty()) {
            issues.forEach(issue -> log.error("Production secret check failed: {}", issue));
            throw new IllegalStateException(
                    "Refusing to start in prod with insecure or placeholder secrets: " + String.join("; ", issues)
            );
        }
    }
}
