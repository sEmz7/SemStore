-- +goose Up
-- +goose StatementBegin
CREATE TABLE IF NOT EXISTS analytics.user_spend
(
    userID UUID,
    total_spent AggregateFunction(sum, Float64)
)
ENGINE = AggregatingMergeTree()
ORDER BY userID;
-- +goose StatementEnd

-- +goose StatementBegin
CREATE MATERIALIZED VIEW IF NOT EXISTS analytics.user_spend_mv
TO analytics.user_spend
AS
SELECT
    userID,
    sumState(price) AS total_spent
FROM analytics.orders
GROUP BY userID;
-- +goose StatementEnd


-- +goose Down
-- +goose StatementBegin
DROP VIEW IF EXISTS analytics.user_spend_mv;
DROP TABLE IF EXISTS analytics.user_spend;
-- +goose StatementEnd