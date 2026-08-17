-- CipherMarket Phase 2: Product management schema

-- ---------------------------------------------------------------------------
-- Products (tenant-scoped)
-- ---------------------------------------------------------------------------
CREATE TABLE products (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id     UUID NOT NULL REFERENCES organisations (id),
    category_id         UUID REFERENCES categories (id),
    name                VARCHAR(255) NOT NULL,
    slug                VARCHAR(128) NOT NULL,
    short_description   TEXT,
    full_description    TEXT,
    product_type        VARCHAR(64) NOT NULL,
    status              VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    cover_image_url     TEXT,
    price_cents         BIGINT NOT NULL DEFAULT 0,
    currency            VARCHAR(3) NOT NULL DEFAULT 'USD',
    licence_type        VARCHAR(64),
    usage_terms         TEXT,
    refund_policy       TEXT,
    current_version_id  UUID,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_products_org_slug UNIQUE (organisation_id, slug),
    CONSTRAINT chk_products_type CHECK (product_type IN ('PDF', 'SOURCE_CODE', 'DESIGN', 'GENERAL')),
    CONSTRAINT chk_products_status CHECK (status IN (
        'DRAFT', 'UPLOADING', 'SCANNING', 'PROCESSING',
        'UNDER_REVIEW', 'PUBLISHED', 'SUSPENDED', 'ARCHIVED'
    )),
    CONSTRAINT chk_products_price CHECK (price_cents >= 0)
);

CREATE INDEX idx_products_org ON products (organisation_id, status);
CREATE INDEX idx_products_category ON products (category_id) WHERE category_id IS NOT NULL;
CREATE INDEX idx_products_slug ON products (slug);

-- ---------------------------------------------------------------------------
-- Product versions
-- ---------------------------------------------------------------------------
CREATE TABLE product_versions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id     UUID NOT NULL REFERENCES organisations (id),
    product_id          UUID NOT NULL REFERENCES products (id),
    version_label       VARCHAR(64) NOT NULL,
    changelog           TEXT,
    status              VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    published_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_product_versions_label UNIQUE (product_id, version_label),
    CONSTRAINT chk_product_versions_status CHECK (status IN (
        'DRAFT', 'UPLOADING', 'SCANNING', 'PROCESSING',
        'UNDER_REVIEW', 'PUBLISHED', 'SUSPENDED', 'REVOKED', 'ARCHIVED'
    ))
);

CREATE INDEX idx_product_versions_product ON product_versions (product_id, created_at DESC);
CREATE INDEX idx_product_versions_org ON product_versions (organisation_id);

ALTER TABLE products
    ADD CONSTRAINT fk_products_current_version
    FOREIGN KEY (current_version_id) REFERENCES product_versions (id);

-- ---------------------------------------------------------------------------
-- Product assets (encrypted file metadata)
-- ---------------------------------------------------------------------------
CREATE TABLE product_assets (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id     UUID NOT NULL REFERENCES organisations (id),
    product_id          UUID NOT NULL REFERENCES products (id),
    product_version_id  UUID NOT NULL REFERENCES product_versions (id),
    original_file_name  VARCHAR(512) NOT NULL,
    sanitized_file_name VARCHAR(512) NOT NULL,
    declared_content_type VARCHAR(128) NOT NULL,
    detected_content_type VARCHAR(128),
    file_size_bytes     BIGINT,
    sha256_checksum     VARCHAR(64),
    storage_bucket      VARCHAR(128),
    storage_object_key  VARCHAR(1024),
    quarantine_object_key VARCHAR(1024),
    encrypted           BOOLEAN NOT NULL DEFAULT FALSE,
    wrapped_dek         TEXT,
    dek_key_version     VARCHAR(32),
    encryption_nonce    VARCHAR(256),
    status              VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    scan_status         VARCHAR(32),
    scan_details        TEXT,
    failure_reason      TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_product_assets_status CHECK (status IN (
        'PENDING', 'UPLOADING', 'QUARANTINED', 'SCANNING', 'SCAN_FAILED',
        'ENCRYPTING', 'READY', 'REJECTED', 'REVOKED'
    )),
    CONSTRAINT chk_product_assets_scan CHECK (scan_status IS NULL OR scan_status IN (
        'CLEAN', 'INFECTED', 'ERROR', 'SKIPPED'
    ))
);

CREATE INDEX idx_product_assets_version ON product_assets (product_version_id);
CREATE INDEX idx_product_assets_org ON product_assets (organisation_id, status);

-- ---------------------------------------------------------------------------
-- Upload sessions (short-lived, single-purpose)
-- ---------------------------------------------------------------------------
CREATE TABLE upload_sessions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id     UUID NOT NULL REFERENCES organisations (id),
    product_id          UUID NOT NULL REFERENCES products (id),
    product_version_id  UUID NOT NULL REFERENCES product_versions (id),
    asset_id            UUID REFERENCES product_assets (id),
    initiated_by_user_id UUID NOT NULL REFERENCES user_profiles (id),
    declared_content_type VARCHAR(128) NOT NULL,
    declared_file_name  VARCHAR(512) NOT NULL,
    max_size_bytes      BIGINT NOT NULL,
    quarantine_object_key VARCHAR(1024) NOT NULL,
    status              VARCHAR(32) NOT NULL DEFAULT 'INITIATED',
    expires_at          TIMESTAMPTZ NOT NULL,
    completed_at        TIMESTAMPTZ,
    failure_reason      TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_upload_sessions_quarantine_key UNIQUE (quarantine_object_key),
    CONSTRAINT chk_upload_sessions_status CHECK (status IN (
        'INITIATED', 'UPLOADING', 'UPLOADED', 'PROCESSING', 'COMPLETED', 'FAILED', 'EXPIRED'
    ))
);

CREATE INDEX idx_upload_sessions_org ON upload_sessions (organisation_id, status);
CREATE INDEX idx_upload_sessions_expires ON upload_sessions (expires_at) WHERE status IN ('INITIATED', 'UPLOADING');
