CREATE TABLE products (
    id             VARCHAR(32)  PRIMARY KEY,
    name           VARCHAR(200) NOT NULL,
    description    TEXT,
    price          INTEGER      NOT NULL CHECK (price > 0),
    stock_quantity INTEGER      NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    status         VARCHAR(20)  NOT NULL DEFAULT 'active',
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_status     ON products (status);
CREATE INDEX idx_products_created_at ON products (created_at DESC);
