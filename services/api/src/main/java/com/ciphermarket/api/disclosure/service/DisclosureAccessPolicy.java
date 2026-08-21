package com.ciphermarket.api.disclosure.service;

import com.ciphermarket.api.common.enums.DisclosureDocumentStatus;
import com.ciphermarket.api.common.enums.DisclosureRequestStatus;

import java.time.Instant;

/**
 * Pure policy for confidential disclosure acceptance and access.
 * Hashing and encryption prove existence and delivery history — not a legal NDA.
 */
public final class DisclosureAccessPolicy {

    private DisclosureAccessPolicy() {
    }

    public static boolean canAccept(DisclosureRequestStatus status, Instant expiresAt, Instant now) {
        if (status != DisclosureRequestStatus.PENDING) {
            return false;
        }
        return expiresAt == null || !now.isAfter(expiresAt);
    }

    public static boolean canReject(DisclosureRequestStatus status) {
        return status == DisclosureRequestStatus.PENDING;
    }

    public static boolean canDownload(
            DisclosureRequestStatus requestStatus,
            DisclosureDocumentStatus documentStatus,
            Instant expiresAt,
            Instant now
    ) {
        if (requestStatus != DisclosureRequestStatus.ACCEPTED) {
            return false;
        }
        if (documentStatus != DisclosureDocumentStatus.READY) {
            return false;
        }
        return expiresAt == null || !now.isAfter(expiresAt);
    }

    public static boolean canRevoke(DisclosureRequestStatus status) {
        return status == DisclosureRequestStatus.PENDING || status == DisclosureRequestStatus.ACCEPTED;
    }
}
