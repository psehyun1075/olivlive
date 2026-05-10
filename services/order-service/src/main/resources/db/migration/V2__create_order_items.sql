CREATE TABLE order_items (
    id           VARCHAR(32)   PRIMARY KEY,
    order_id     VARCHAR(32)   NOT NULL REFERENCES orders(id),
    product_id   TEXT          NOT NULL,
    product_name VARCHAR(200)  NOT NULL,
    unit_price   INTEGER       NOT NULL CHECK (unit_price > 0),
    quantity     INTEGER       NOT NULL CHECK (quantity >= 1),
    subtotal     INTEGER       NOT NULL CHECK (subtotal > 0)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
