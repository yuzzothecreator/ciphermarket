package com.ciphermarket.api.securityops.service;

import com.ciphermarket.api.common.exception.AccessDeniedException;

import java.util.UUID;

public final class MakerCheckerPolicy {

    private MakerCheckerPolicy() {
    }

    public static void assertDistinctActors(UUID makerId, UUID checkerId) {
        if (makerId.equals(checkerId)) {
            throw new AccessDeniedException("Maker cannot check their own request");
        }
    }
}
