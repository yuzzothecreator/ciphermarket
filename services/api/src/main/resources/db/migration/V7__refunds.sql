-- CipherMarket Phase 8: Refunds

-- ---------------------------------------------------------------------------
-- Refund requests (buyer-initiated, dual-control approval for execution)
-- ---------------------------------------------------------------------------
CREATE TABLE refund_requests (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id                UUID NOT NULL REFERENCES orders (id),
    payment_id              UUID NOT NULL REFERENCES payments (id),
    buyer_user_id           UUID NOT NULL REFERENCES user_profiles (id),
    organisation_id         UUID REFERENCES organisations (id),
    amount_cents            BIGINT NOT NULL,
    currency                VARCHAR(3) NOT NULL,
    reason                  TEXT NOT NULL,
    status                  VARCHAR(32) NOT NULL DEFAULT 'REQUESTED',
    rejection_reason        TEXT,
    approval_request_id     UUID REFERENCES approval_requests (id),
    provider_refund_ref     VARCHAR(255),
    requested_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    decided_at              TIMESTAMPTZ,
    completed_at            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version                 BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_refund_requests_status CHECK (status IN (
        'REQUESTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'CANCELLED', 'COMPLETED'
    )),
    CONSTRAINT chk_refund_requests_amount CHECK (amount_cents > 0)
);

CREATE UNIQUE INDEX uq_refund_requests_open_order
    ON refund_requests (order_id)
    WHERE status IN ('REQUESTED', 'UNDER_REVIEW', 'APPROVED');

CREATE INDEX idx_refund_requests_buyer ON refund_requests (buyer_user_id, requested_at DESC);
CREATE INDEX idx_refund_requests_status ON refund_requests (status, requested_at DESC);

-- Allow REFUNDED payment status
ALTER TABLE payments DROP CONSTRAINT chk_payments_status;
ALTER TABLE payments ADD CONSTRAINT chk_payments_status CHECK (status IN (
    'PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'CANCELLED', 'REFUNDED'
));

-- Maker-checker action for refund execution
ALTER TABLE approval_requests DROP CONSTRAINT chk_approval_action;
ALTER TABLE approval_requests ADD CONSTRAINT chk_approval_action CHECK (action_type IN (
    'PRODUCT_SUSPEND', 'ENTITLEMENT_REVOKE', 'LICENCE_REVOKE', 'REFUND_APPROVE'
));
