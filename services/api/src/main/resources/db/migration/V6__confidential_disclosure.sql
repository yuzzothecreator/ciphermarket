-- CipherMarket Phase 7: Confidential disclosure

-- ---------------------------------------------------------------------------
-- Encrypted confidential documents (tenant-scoped)
-- ---------------------------------------------------------------------------
CREATE TABLE disclosure_documents (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id         UUID NOT NULL REFERENCES organisations (id),
    created_by_user_id      UUID NOT NULL REFERENCES user_profiles (id),
    title                   VARCHAR(255) NOT NULL,
    description             TEXT,
    original_file_name      VARCHAR(512) NOT NULL,
    sanitized_file_name     VARCHAR(512) NOT NULL,
    declared_content_type   VARCHAR(255) NOT NULL,
    detected_content_type   VARCHAR(255),
    file_size_bytes         BIGINT,
    sha256_checksum         VARCHAR(64),
    document_version        INT NOT NULL DEFAULT 1,
    storage_bucket          VARCHAR(255),
    storage_object_key      VARCHAR(1024),
    quarantine_object_key   VARCHAR(1024),
    encrypted               BOOLEAN NOT NULL DEFAULT FALSE,
    wrapped_dek             TEXT,
    dek_key_version         VARCHAR(64),
    encryption_nonce        VARCHAR(128),
    status                  VARCHAR(32) NOT NULL DEFAULT 'PROCESSING',
    scan_status             VARCHAR(32),
    failure_reason          TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version                 BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_disclosure_documents_status CHECK (status IN (
        'PROCESSING', 'READY', 'FAILED', 'REVOKED'
    ))
);

CREATE INDEX idx_disclosure_documents_org ON disclosure_documents (organisation_id, created_at DESC);
CREATE INDEX idx_disclosure_documents_hash ON disclosure_documents (sha256_checksum);

-- ---------------------------------------------------------------------------
-- Disclosure requests (creator → recipient)
-- ---------------------------------------------------------------------------
CREATE TABLE disclosure_requests (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id         UUID NOT NULL REFERENCES organisations (id),
    document_id             UUID NOT NULL REFERENCES disclosure_documents (id),
    created_by_user_id      UUID NOT NULL REFERENCES user_profiles (id),
    recipient_user_id       UUID NOT NULL REFERENCES user_profiles (id),
    recipient_email         VARCHAR(320) NOT NULL,
    confidentiality_terms   TEXT NOT NULL,
    status                  VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    expires_at              TIMESTAMPTZ,
    accepted_at             TIMESTAMPTZ,
    rejected_at             TIMESTAMPTZ,
    revoked_at              TIMESTAMPTZ,
    disclosed_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    decision_note           TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version                 BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_disclosure_requests_status CHECK (status IN (
        'PENDING', 'ACCEPTED', 'REJECTED', 'REVOKED', 'EXPIRED'
    ))
);

CREATE UNIQUE INDEX uq_disclosure_request_pending
    ON disclosure_requests (document_id, recipient_user_id)
    WHERE status = 'PENDING';

CREATE INDEX idx_disclosure_requests_org ON disclosure_requests (organisation_id, created_at DESC);
CREATE INDEX idx_disclosure_requests_recipient ON disclosure_requests (recipient_user_id, status, created_at DESC);
CREATE INDEX idx_disclosure_requests_document ON disclosure_requests (document_id, created_at DESC);

-- ---------------------------------------------------------------------------
-- Access events (append-oriented evidence trail)
-- ---------------------------------------------------------------------------
CREATE TABLE disclosure_access_events (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id         UUID NOT NULL REFERENCES organisations (id),
    request_id              UUID NOT NULL REFERENCES disclosure_requests (id),
    document_id             UUID NOT NULL REFERENCES disclosure_documents (id),
    actor_user_id           UUID NOT NULL REFERENCES user_profiles (id),
    event_type              VARCHAR(64) NOT NULL,
    details                 JSONB,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_disclosure_access_event_type CHECK (event_type IN (
        'REQUEST_CREATED',
        'TERMS_ACCEPTED',
        'TERMS_REJECTED',
        'ACCESS_GRANTED_DOWNLOAD',
        'REQUEST_REVOKED',
        'DOCUMENT_REVOKED'
    ))
);

CREATE INDEX idx_disclosure_access_events_request ON disclosure_access_events (request_id, created_at DESC);
CREATE INDEX idx_disclosure_access_events_document ON disclosure_access_events (document_id, created_at DESC);
