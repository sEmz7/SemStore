package dto

import "github.com/google/uuid"

type DiscountMessage struct {
	UserID   uuid.UUID `json:"userId"`
	Discount float64   `json:"discountPercent"`
}
