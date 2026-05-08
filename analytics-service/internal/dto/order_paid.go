package dto

import "github.com/google/uuid"

type OrderPaidMessage struct {
	OrderID uuid.UUID `json:"orderId"`
	UserID  uuid.UUID `json:"userId"`
	Price   float64   `json:"price"`
}
