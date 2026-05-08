package order

import (
	"context"
	"fmt"
	"time"

	"analytics-service/internal/database"
	"analytics-service/internal/model/order"

	"github.com/google/uuid"
)

type Repository struct {
	db *database.DB
}

func NewRepository(db *database.DB) *Repository {
	return &Repository{db: db}
}

func (r *Repository) CreateOrder(ctx context.Context, o order.Order) error {
	q := `INSERT INTO analytics.orders (orderID, userID, price, ts) VALUES (?, ?, ?, ?)`
	err := r.db.Client.Exec(ctx, q, o.OrderID, o.UserID, o.Price, time.Now())
	if err != nil {
		return fmt.Errorf("insert order: %w", err)
	}
	return nil
}

func (r *Repository) GetDiscountByUserID(ctx context.Context, userID uuid.UUID) (float64, error) {
	var lessCount uint64
	var totalCount uint64

	query := `
	WITH
	(
		SELECT sumMerge(total_spent)
		FROM analytics.user_spend
		WHERE userID = ?
	) AS target_user_spend

	SELECT
		countIf(user_spend_value < target_user_spend) AS users_with_less_spend,
		count() AS total_users
	FROM
	(
		SELECT
			userID,
			sumMerge(total_spent) AS user_spend_value
		FROM analytics.user_spend
		GROUP BY userID
	)
	`

	err := r.db.Client.QueryRow(ctx, query, userID).
		Scan(&lessCount, &totalCount)

	if err != nil {
		return 0, fmt.Errorf("discount query: %w", err)
	}

	if totalCount == 0 {
		return 0, nil
	}

	discount := float64(lessCount) / float64(totalCount) * 15

	if discount > 15 {
		discount = 15
	}

	return discount, nil
}
