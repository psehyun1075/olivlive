CREATE TABLE orders (
    id              VARCHAR(32)   PRIMARY KEY,
    buyer_id        VARCHAR(100)  NOT NULL,
    live_session_id TEXT,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PAID',
    total_amount    INTEGER       NOT NULL CHECK (total_amount > 0),
    payment_status  VARCHAR(20)   NOT NULL DEFAULT 'MOCK_PAID',
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_buyer_id   ON orders (buyer_id);
CREATE INDEX idx_orders_created_at ON orders (created_at DESC);
