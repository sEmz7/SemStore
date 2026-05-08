package discount

import (
	"context"

	"github.com/google/uuid"
)

type DiscountRepository interface {
	GetDiscountByUserID(ctx context.Context, userID uuid.UUID) (float64, error)
}

type DiscountUsecase interface {
	GetDiscount(ctx context.Context, userID uuid.UUID) (float64, error)
}
