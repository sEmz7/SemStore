package discount

import (
	"context"

	"github.com/google/uuid"
)

type usecase struct {
	repo DiscountRepository
}

func NewDiscountUsecase(r DiscountRepository) DiscountUsecase {
	return &usecase{repo: r}
}

func (u *usecase) GetDiscount(ctx context.Context, userID uuid.UUID) (float64, error) {
	return u.repo.GetDiscountByUserID(ctx, userID)
}
