package queue

import (
	"analytics-service/internal/model/order"
	"context"

	"github.com/IBM/sarama"
	"github.com/google/uuid"
)

type OrderUsecase interface {
	AddOrder(ctx context.Context, o order.Order) error
}

type DiscountUsecase interface {
	GetDiscount(ctx context.Context, userID uuid.UUID) (float64, error)
}

type SyncProducer interface {
	SendMessage(msg *sarama.ProducerMessage) (partition int32, offset int64, err error)
	Close() error
}
