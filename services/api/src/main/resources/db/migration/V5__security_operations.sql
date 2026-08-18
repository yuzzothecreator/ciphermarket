-- CipherMarket Phase 5: Security operations

-- ---------------------------------------------------------------------------
-- Security events (operational risk signals)
-- ---------------------------------------------------------------------------
CREATE TABLE security_events (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id     UUID REFERENCES organisations (id),
    actor_user_id       UUID REFERENCES user_profiles (id),
    event_type          VARCHAR(64) NOT NULL,
    severity            VARCHAR(16) NOT NULL DEFAULT 'INFO',
    status              VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    resource_type       VARCHAR(128),
    resource_id         UUID,
    summary             TEXT NOT NULL,
    details             JSONB,
    correlation_id      VARCHAR(64),
    acknowledged_by     UUID REFERENCES user_profiles (id),
    acknowledged_at     TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_security_events_severity CHECK (severity IN ('INFO', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_security_events_status CHECK (status IN ('OPEN', 'ACKNOWLEDGED', 'CLOSED'))
);

CREATE INDEX idx_security_events_created ON security_events (created_at DESC);
CREATE INDEX idx_security_events_status ON security_events (status, severity);
CREATE INDEX idx_security_events_org ON security_events (organisation_id, created_at DESC);

-- ---------------------------------------------------------------------------
-- Audit batches (sealed hash-chain checkpoints)
-- ---------------------------------------------------------------------------
CREATE TABLE audit_batches (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_event_id      UUID NOT NULL REFERENCES audit_events (id),
    last_event_id       UUID NOT NULL REFERENCES audit_events (id),
    event_count         INT NOT NULL,
    root_hash           VARCHAR(128) NOT NULL,
    previous_batch_hash VARCHAR(128),
    sealed_by_user_id   UUID REFERENCES user_profiles (id),
    sealed_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_audit_batches_count CHECK (event_count >= 1)
);

CREATE INDEX idx_audit_batches_sealed ON audit_batches (sealed_at DESC);

-- ---------------------------------------------------------------------------
-- Maker-checker approval requests
-- ---------------------------------------------------------------------------
CREATE TABLE approval_requests (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    action_type         VARCHAR(64) NOT NULL,
    resource_type       VARCHAR(128) NOT NULL,
    resource_id         UUID NOT NULL,
    organisation_id     UUID REFERENCES organisations (id),
    payload             JSONB,
    reason              TEXT NOT NULL,
    status              VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    requested_by        UUID NOT NULL REFERENCES user_profiles (id),
    decided_by          UUID REFERENCES user_profiles (id),
    decision_reason     TEXT,
    requested_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    decided_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_approval_action CHECK (action_type IN (
        'PRODUCT_SUSPEND', 'ENTITLEMENT_REVOKE', 'LICENCE_REVOKE'
    )),
    CONSTRAINT chk_approval_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'))
);

CREATE INDEX idx_approvals_status ON approval_requests (status, requested_at DESC);
CREATE INDEX idx_approvals_requester ON approval_requests (requested_by, requested_at DESC);
