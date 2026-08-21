package com.ciphermarket.api.commerce.service;

import com.ciphermarket.api.common.enums.RefundRequestStatus;

import java.util.EnumSet;
import java.util.Set;

public final class RefundPolicy {

    private static final Set<RefundRequestStatus> OPEN_STATUSES = EnumSet.of(
            RefundRequestStatus.REQUESTED,
            RefundRequestStatus.UNDER_REVIEW,
            RefundRequestStatus.APPROVED
    );

    private RefundPolicy() {
    }

    public static boolean isOpen(RefundRequestStatus status) {
        return OPEN_STATUSES.contains(status);
    }

    public static boolean canReject(RefundRequestStatus status) {
        return status == RefundRequestStatus.REQUESTED || status == RefundRequestStatus.UNDER_REVIEW;
    }

    public static boolean canSubmitForApproval(RefundRequestStatus status) {
        return status == RefundRequestStatus.REQUESTED;
    }

    public static boolean canCancel(RefundRequestStatus status) {
        return status == RefundRequestStatus.REQUESTED;
    }

    public static boolean canExecute(RefundRequestStatus status) {
        return status == RefundRequestStatus.UNDER_REVIEW || status == RefundRequestStatus.APPROVED;
    }
}
