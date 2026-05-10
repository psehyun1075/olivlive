CREATE TABLE live_products (
    id              VARCHAR(32) PRIMARY KEY,
    live_session_id TEXT        NOT NULL,
    product_id      VARCHAR(32) NOT NULL REFERENCES products (id),
    display_order   INTEGER     NOT NULL CHECK (display_order >= 1),
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT live_products_unique_mapping UNIQUE (live_session_id, product_id),
    CONSTRAINT live_products_unique_order   UNIQUE (live_session_id, display_order)
);

CREATE INDEX idx_live_products_session ON live_products (live_session_id, is_active);
CREATE INDEX idx_live_products_order   ON live_products (live_session_id, display_order);
