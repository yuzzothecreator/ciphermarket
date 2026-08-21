package com.ciphermarket.api.commerce.service;

import com.ciphermarket.api.common.enums.RefundRequestStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RefundPolicyTest {

    @Test
    void openStatusesBlockDuplicateRequests() {
        assertThat(RefundPolicy.isOpen(RefundRequestStatus.REQUESTED)).isTrue();
        assertThat(RefundPolicy.isOpen(RefundRequestStatus.UNDER_REVIEW)).isTrue();
        assertThat(RefundPolicy.isOpen(RefundRequestStatus.COMPLETED)).isFalse();
        assertThat(RefundPolicy.isOpen(RefundRequestStatus.REJECTED)).isFalse();
    }

    @Test
    void approvalAndRejectGates() {
        assertThat(RefundPolicy.canSubmitForApproval(RefundRequestStatus.REQUESTED)).isTrue();
        assertThat(RefundPolicy.canSubmitForApproval(RefundRequestStatus.UNDER_REVIEW)).isFalse();
        assertThat(RefundPolicy.canReject(RefundRequestStatus.REQUESTED)).isTrue();
        assertThat(RefundPolicy.canReject(RefundRequestStatus.COMPLETED)).isFalse();
        assertThat(RefundPolicy.canExecute(RefundRequestStatus.UNDER_REVIEW)).isTrue();
        assertThat(RefundPolicy.canExecute(RefundRequestStatus.REQUESTED)).isFalse();
    }
}
