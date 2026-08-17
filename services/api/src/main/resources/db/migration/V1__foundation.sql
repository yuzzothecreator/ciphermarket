-- CipherMarket Phase 1: Foundation schema
-- UUID primary keys, UTC timestamps, tenant isolation from the start

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ---------------------------------------------------------------------------
-- User profiles (linked to Keycloak subject)
-- ---------------------------------------------------------------------------
CREATE TABLE user_profiles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_sub    VARCHAR(255) NOT NULL,
    email           VARCHAR(320) NOT NULL,
    display_name    VARCHAR(255) NOT NULL,
    avatar_url      TEXT,
    locale          VARCHAR(10) NOT NULL DEFAULT 'en',
    timezone        VARCHAR(64) NOT NULL DEFAULT 'UTC',
    status          VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    mfa_enabled     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_user_profiles_keycloak_sub UNIQUE (keycloak_sub),
    CONSTRAINT uq_user_profiles_email UNIQUE (email),
    CONSTRAINT chk_user_profiles_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED'))
);

CREATE INDEX idx_user_profiles_email ON user_profiles (email);
CREATE INDEX idx_user_profiles_status ON user_profiles (status);

-- ---------------------------------------------------------------------------
-- Organisations (tenants)
-- ---------------------------------------------------------------------------
CREATE TABLE organisations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(128) NOT NULL,
    description     TEXT,
    status          VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    owner_user_id   UUID NOT NULL REFERENCES user_profiles (id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_organisations_slug UNIQUE (slug),
    CONSTRAINT chk_organisations_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'ARCHIVED'))
);

CREATE INDEX idx_organisations_owner ON organisations (owner_user_id);
CREATE INDEX idx_organisations_status ON organisations (status);

-- ---------------------------------------------------------------------------
-- Organisation memberships
-- ---------------------------------------------------------------------------
CREATE TABLE organisation_memberships (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id     UUID NOT NULL REFERENCES organisations (id),
    user_id             UUID NOT NULL REFERENCES user_profiles (id),
    role                VARCHAR(64) NOT NULL,
    status              VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    invited_by_user_id  UUID REFERENCES user_profiles (id),
    joined_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_org_membership UNIQUE (organisation_id, user_id),
    CONSTRAINT chk_org_membership_role CHECK (role IN (
        'OWNER', 'ADMINISTRATOR', 'PRODUCT_MANAGER',
        'FINANCE_OFFICER', 'SUPPORT_OFFICER', 'SECURITY_VIEWER'
    )),
    CONSTRAINT chk_org_membership_status CHECK (status IN ('ACTIVE', 'INVITED', 'SUSPENDED', 'REMOVED'))
);

CREATE INDEX idx_org_memberships_org ON organisation_memberships (organisation_id);
CREATE INDEX idx_org_memberships_user ON organisation_memberships (user_id);

-- ---------------------------------------------------------------------------
-- Categories (platform-wide, Phase 1 seed)
-- ---------------------------------------------------------------------------
CREATE TABLE categories (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(128) NOT NULL,
    slug            VARCHAR(128) NOT NULL,
    description     TEXT,
    parent_id       UUID REFERENCES categories (id),
    sort_order      INT NOT NULL DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_categories_slug UNIQUE (slug)
);

-- ---------------------------------------------------------------------------
-- Audit events (append-only)
-- ---------------------------------------------------------------------------
CREATE TABLE audit_events (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id     UUID REFERENCES organisations (id),
    actor_user_id       UUID REFERENCES user_profiles (id),
    actor_keycloak_sub  VARCHAR(255),
    action              VARCHAR(128) NOT NULL,
    resource_type       VARCHAR(128) NOT NULL,
    resource_id         UUID,
    before_summary      JSONB,
    after_summary       JSONB,
    reason              TEXT,
    approval_actor_id   UUID REFERENCES user_profiles (id),
    correlation_id      VARCHAR(64) NOT NULL,
    ip_address          INET,
    user_agent          TEXT,
    event_hash          VARCHAR(128),
    previous_hash       VARCHAR(128),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_events_org ON audit_events (organisation_id, created_at DESC);
CREATE INDEX idx_audit_events_actor ON audit_events (actor_user_id, created_at DESC);
CREATE INDEX idx_audit_events_correlation ON audit_events (correlation_id);
CREATE INDEX idx_audit_events_resource ON audit_events (resource_type, resource_id);

-- Prevent updates/deletes on audit_events (append-only enforcement)
CREATE OR REPLACE FUNCTION prevent_audit_mutation()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'audit_events is append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_events_no_update
    BEFORE UPDATE OR DELETE ON audit_events
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_mutation();

-- Seed default categories
INSERT INTO categories (name, slug, description, sort_order) VALUES
    ('Documents & E-books', 'documents', 'PDF documents, research papers, and e-books', 1),
    ('Source Code', 'source-code', 'ZIP archives and source-code projects', 2),
    ('Design Assets', 'design', 'Templates, graphics, and design files', 3),
    ('Business', 'business', 'Proposals, plans, and business documents', 4);
