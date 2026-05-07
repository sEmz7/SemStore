package order

import "github.com/google/uuid"

type Order struct {
	OrderID uuid.UUID
	UserID  uuid.UUID
	Price   float64
}
