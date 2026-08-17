-- CipherMarket Phase 3: Commerce schema

-- ---------------------------------------------------------------------------
-- Shopping carts (one active cart per buyer)
-- ---------------------------------------------------------------------------
CREATE TABLE carts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_user_id   UUID NOT NULL REFERENCES user_profiles (id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_carts_buyer UNIQUE (buyer_user_id)
);

CREATE INDEX idx_carts_buyer ON carts (buyer_user_id);

CREATE TABLE cart_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id         UUID NOT NULL REFERENCES carts (id) ON DELETE CASCADE,
    product_id      UUID NOT NULL REFERENCES products (id),
    quantity        INT NOT NULL DEFAULT 1,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version         BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_cart_items_product UNIQUE (cart_id, product_id),
    CONSTRAINT chk_cart_items_quantity CHECK (quantity >= 1 AND quantity <= 99)
);

CREATE INDEX idx_cart_items_cart ON cart_items (cart_id);

-- ---------------------------------------------------------------------------
-- Orders
-- ---------------------------------------------------------------------------
CREATE TABLE orders (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_user_id       UUID NOT NULL REFERENCES user_profiles (id),
    status              VARCHAR(32) NOT NULL DEFAULT 'PENDING_PAYMENT',
    subtotal_cents      BIGINT NOT NULL DEFAULT 0,
    currency            VARCHAR(3) NOT NULL DEFAULT 'USD',
    paid_at             TIMESTAMPTZ,
    cancelled_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_orders_status CHECK (status IN (
        'PENDING_PAYMENT', 'PAID', 'FAILED', 'CANCELLED', 'REFUNDED'
    )),
    CONSTRAINT chk_orders_subtotal CHECK (subtotal_cents >= 0)
);

CREATE INDEX idx_orders_buyer ON orders (buyer_user_id, created_at DESC);
CREATE INDEX idx_orders_status ON orders (status);

CREATE TABLE order_items (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id            UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id          UUID NOT NULL REFERENCES products (id),
    organisation_id     UUID NOT NULL REFERENCES organisations (id),
    product_name        VARCHAR(255) NOT NULL,
    product_slug        VARCHAR(128) NOT NULL,
    unit_price_cents    BIGINT NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    quantity            INT NOT NULL DEFAULT 1,
    line_total_cents    BIGINT NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_order_items_quantity CHECK (quantity >= 1),
    CONSTRAINT chk_order_items_prices CHECK (unit_price_cents >= 0 AND line_total_cents >= 0)
);

CREATE INDEX idx_order_items_order ON order_items (order_id);
CREATE INDEX idx_order_items_product ON order_items (product_id);

-- ---------------------------------------------------------------------------
-- Payments (mock provider in Phase 3)
-- ---------------------------------------------------------------------------
CREATE TABLE payments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id            UUID NOT NULL REFERENCES orders (id),
    provider            VARCHAR(32) NOT NULL DEFAULT 'MOCK',
    external_reference  VARCHAR(255) NOT NULL,
    status              VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    amount_cents        BIGINT NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    failure_reason      TEXT,
    completed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_payments_external_ref UNIQUE (provider, external_reference),
    CONSTRAINT chk_payments_status CHECK (status IN (
        'PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'CANCELLED'
    )),
    CONSTRAINT chk_payments_amount CHECK (amount_cents >= 0)
);

CREATE INDEX idx_payments_order ON payments (order_id);
CREATE INDEX idx_payments_status ON payments (status);

-- ---------------------------------------------------------------------------
-- Payment webhook events (idempotent processing)
-- ---------------------------------------------------------------------------
CREATE TABLE payment_webhook_events (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id          UUID REFERENCES payments (id),
    provider            VARCHAR(32) NOT NULL,
    idempotency_key     VARCHAR(255) NOT NULL,
    event_type          VARCHAR(64) NOT NULL,
    payload_hash        VARCHAR(64) NOT NULL,
    signature           VARCHAR(512),
    processed           BOOLEAN NOT NULL DEFAULT FALSE,
    processing_error    TEXT,
    received_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at        TIMESTAMPTZ,
    CONSTRAINT uq_webhook_idempotency UNIQUE (provider, idempotency_key)
);

CREATE INDEX idx_webhook_payment ON payment_webhook_events (payment_id);

-- ---------------------------------------------------------------------------
-- Entitlements (granted only after verified webhook)
-- ---------------------------------------------------------------------------
CREATE TABLE entitlements (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_user_id       UUID NOT NULL REFERENCES user_profiles (id),
    product_id          UUID NOT NULL REFERENCES products (id),
    order_id            UUID NOT NULL REFERENCES orders (id),
    order_item_id       UUID NOT NULL REFERENCES order_items (id),
    status              VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    granted_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked_at          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_entitlements_order_item UNIQUE (order_item_id),
    CONSTRAINT chk_entitlements_status CHECK (status IN ('ACTIVE', 'REVOKED', 'EXPIRED'))
);

CREATE INDEX idx_entitlements_buyer ON entitlements (buyer_user_id, status);
CREATE INDEX idx_entitlements_product ON entitlements (product_id);
CREATE INDEX idx_entitlements_order ON entitlements (order_id);

-- Published products catalogue index
CREATE INDEX idx_products_published ON products (status, updated_at DESC) WHERE status = 'PUBLISHED';
