package order

import (
	"context"

	"analytics-service/internal/model/order"
)

type OrderRepository interface {
	CreateOrder(ctx context.Context, o order.Order) error
}

type OrderUsecase interface {
	AddOrder(ctx context.Context, o order.Order) error
}
