package com.ciphermarket.api.common.tenant;

import java.util.Optional;
import java.util.UUID;

public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_ORG = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setOrganisationId(UUID organisationId) {
        CURRENT_ORG.set(organisationId);
    }

    public static Optional<UUID> getOrganisationId() {
        return Optional.ofNullable(CURRENT_ORG.get());
    }

    public static void clear() {
        CURRENT_ORG.remove();
    }
}
