-- +goose Up
-- +goose StatementBegin
CREATE DATABASE IF NOT EXISTS analytics;
-- +goose StatementEnd

-- +goose StatementBegin
CREATE TABLE IF NOT EXISTS analytics.orders
(
    orderID UUID,
    userID  UUID,
    price   Float64,
    ts      DateTime64(3) DEFAULT now()
)
ENGINE = MergeTree()
ORDER BY (userID, ts);
-- +goose StatementEnd


-- +goose Down
-- +goose StatementBegin
DROP TABLE IF EXISTS analytics.orders;
DROP DATABASE IF EXISTS analytics;
-- +goose StatementEnd