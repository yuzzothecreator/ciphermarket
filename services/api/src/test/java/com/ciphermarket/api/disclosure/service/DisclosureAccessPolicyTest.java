package com.ciphermarket.api.disclosure.service;

import com.ciphermarket.api.common.enums.DisclosureDocumentStatus;
import com.ciphermarket.api.common.enums.DisclosureRequestStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DisclosureAccessPolicyTest {

    @Test
    void acceptsPendingUnexpiredRequest() {
        Instant now = Instant.parse("2026-08-21T10:00:00Z");
        assertThat(DisclosureAccessPolicy.canAccept(
                DisclosureRequestStatus.PENDING,
                Instant.parse("2026-08-22T10:00:00Z"),
                now
        )).isTrue();
    }

    @Test
    void rejectsExpiredOrNonPendingAccept() {
        Instant now = Instant.parse("2026-08-21T10:00:00Z");
        assertThat(DisclosureAccessPolicy.canAccept(
                DisclosureRequestStatus.PENDING,
                Instant.parse("2026-08-20T10:00:00Z"),
                now
        )).isFalse();
        assertThat(DisclosureAccessPolicy.canAccept(
                DisclosureRequestStatus.ACCEPTED,
                null,
                now
        )).isFalse();
    }

    @Test
    void downloadRequiresAcceptedReadyDocument() {
        Instant now = Instant.parse("2026-08-21T10:00:00Z");
        assertThat(DisclosureAccessPolicy.canDownload(
                DisclosureRequestStatus.ACCEPTED,
                DisclosureDocumentStatus.READY,
                null,
                now
        )).isTrue();
        assertThat(DisclosureAccessPolicy.canDownload(
                DisclosureRequestStatus.PENDING,
                DisclosureDocumentStatus.READY,
                null,
                now
        )).isFalse();
        assertThat(DisclosureAccessPolicy.canDownload(
                DisclosureRequestStatus.ACCEPTED,
                DisclosureDocumentStatus.REVOKED,
                null,
                now
        )).isFalse();
    }

    @Test
    void revokeAllowedForPendingAndAccepted() {
        assertThat(DisclosureAccessPolicy.canRevoke(DisclosureRequestStatus.PENDING)).isTrue();
        assertThat(DisclosureAccessPolicy.canRevoke(DisclosureRequestStatus.ACCEPTED)).isTrue();
        assertThat(DisclosureAccessPolicy.canRevoke(DisclosureRequestStatus.REJECTED)).isFalse();
    }
}
