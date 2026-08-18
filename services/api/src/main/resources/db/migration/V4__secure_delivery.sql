-- CipherMarket Phase 4: Secure delivery schema

ALTER TABLE entitlements
    ADD COLUMN product_version_id UUID REFERENCES product_versions (id);

-- ---------------------------------------------------------------------------
-- Registered devices (buyer device binding)
-- ---------------------------------------------------------------------------
CREATE TABLE registered_devices (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_user_id       UUID NOT NULL REFERENCES user_profiles (id),
    fingerprint_hash    VARCHAR(64) NOT NULL,
    label               VARCHAR(128) NOT NULL,
    status              VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    registered_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked_at          TIMESTAMPTZ,
    last_seen_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_devices_fingerprint UNIQUE (buyer_user_id, fingerprint_hash),
    CONSTRAINT chk_devices_status CHECK (status IN ('ACTIVE', 'REVOKED'))
);

CREATE INDEX idx_devices_buyer ON registered_devices (buyer_user_id, status);

-- ---------------------------------------------------------------------------
-- Licences (Ed25519-signed delivery credentials)
-- ---------------------------------------------------------------------------
CREATE TABLE licences (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entitlement_id      UUID NOT NULL REFERENCES entitlements (id),
    buyer_user_id       UUID NOT NULL REFERENCES user_profiles (id),
    product_id          UUID NOT NULL REFERENCES products (id),
    product_version_id  UUID NOT NULL REFERENCES product_versions (id),
    status              VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    token_payload       TEXT NOT NULL,
    token_signature     TEXT NOT NULL,
    issued_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at          TIMESTAMPTZ NOT NULL,
    revoked_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_licences_entitlement UNIQUE (entitlement_id),
    CONSTRAINT chk_licences_status CHECK (status IN ('ACTIVE', 'REVOKED', 'EXPIRED'))
);

CREATE INDEX idx_licences_buyer ON licences (buyer_user_id, status);

-- ---------------------------------------------------------------------------
-- Access grants (short-lived download tokens)
-- ---------------------------------------------------------------------------
CREATE TABLE access_grants (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    licence_id          UUID NOT NULL REFERENCES licences (id),
    buyer_user_id       UUID NOT NULL REFERENCES user_profiles (id),
    product_asset_id    UUID NOT NULL REFERENCES product_assets (id),
    device_id           UUID REFERENCES registered_devices (id),
    token_hash          VARCHAR(64) NOT NULL,
    status              VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    max_uses            INT NOT NULL DEFAULT 1,
    use_count           INT NOT NULL DEFAULT 0,
    expires_at          TIMESTAMPTZ NOT NULL,
    revoked_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_access_grants_token_hash UNIQUE (token_hash),
    CONSTRAINT chk_access_grants_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'REVOKED', 'EXHAUSTED')),
    CONSTRAINT chk_access_grants_uses CHECK (max_uses >= 1 AND use_count >= 0)
);

CREATE INDEX idx_access_grants_licence ON access_grants (licence_id);
CREATE INDEX idx_access_grants_expires ON access_grants (expires_at) WHERE status = 'ACTIVE';

-- ---------------------------------------------------------------------------
-- Download events (audit trail)
-- ---------------------------------------------------------------------------
CREATE TABLE download_events (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    access_grant_id     UUID NOT NULL REFERENCES access_grants (id),
    buyer_user_id       UUID NOT NULL REFERENCES user_profiles (id),
    product_id          UUID NOT NULL REFERENCES products (id),
    product_asset_id    UUID NOT NULL REFERENCES product_assets (id),
    device_id           UUID REFERENCES registered_devices (id),
    outcome             VARCHAR(32) NOT NULL,
    client_ip           VARCHAR(45),
    user_agent          TEXT,
    bytes_delivered     BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_download_outcome CHECK (outcome IN ('SUCCESS', 'DENIED', 'FAILED'))
);

CREATE INDEX idx_download_events_buyer ON download_events (buyer_user_id, created_at DESC);
CREATE INDEX idx_download_events_grant ON download_events (access_grant_id);
