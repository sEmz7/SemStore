package order

import (
	"analytics-service/internal/model/order"
	"context"
)

type usecase struct {
	repo OrderRepository
}

func NewOrderUsecase(r OrderRepository) OrderUsecase {
	return &usecase{repo: r}
}

func (u *usecase) AddOrder(ctx context.Context, o order.Order) error {
	return u.repo.CreateOrder(ctx, o)
}
