package queue

import (
	"context"
	"encoding/json"
	"log"

	"analytics-service/config"
	"analytics-service/internal/dto"
	"analytics-service/internal/model/order"

	"github.com/google/uuid"

	"github.com/IBM/sarama"
)

type Handler struct {
	cfg *config.KafkaConfig

	orderUC    OrderUsecase
	discountUC DiscountUsecase

	producer SyncProducer
}

func NewHandler(
	cfg *config.KafkaConfig,
	orderUC OrderUsecase,
	discountUC DiscountUsecase,
) (*Handler, error) {

	brokers := []string{cfg.Host + ":" + cfg.Port}

	saramaCfg := sarama.NewConfig()
	saramaCfg.Producer.Return.Successes = true

	producer, err := sarama.NewSyncProducer(brokers, saramaCfg)
	if err != nil {
		return nil, err
	}

	return &Handler{
		cfg:        cfg,
		orderUC:    orderUC,
		discountUC: discountUC,
		producer:   producer,
	}, nil
}

func (h *Handler) Close() {
	if h.producer != nil {
		if err := h.producer.Close(); err != nil {
			log.Printf("kafka producer close error: %v", err)
		}
	}
}

func (h *Handler) StartConsumer(ctx context.Context) error {

	brokers := []string{h.cfg.Host + ":" + h.cfg.Port}

	saramaCfg := sarama.NewConfig()

	group, err := sarama.NewConsumerGroup(
		brokers,
		h.cfg.GroupID,
		saramaCfg,
	)
	if err != nil {
		return err
	}

	defer group.Close()

	handler := &consumerGroupHandler{
		queue: h,
	}

	for {
		if ctx.Err() != nil {
			return nil
		}

		err := group.Consume(
			ctx,
			[]string{h.cfg.TopicPaid},
			handler,
		)

		if err != nil {
			log.Println("consumer error:", err)
		}
	}
}

type consumerGroupHandler struct {
	queue *Handler
}

func (consumerGroupHandler) Setup(sarama.ConsumerGroupSession) error   { return nil }
func (consumerGroupHandler) Cleanup(sarama.ConsumerGroupSession) error { return nil }

func (h *consumerGroupHandler) ConsumeClaim(
	session sarama.ConsumerGroupSession,
	claim sarama.ConsumerGroupClaim,
) error {

	for msg := range claim.Messages() {

		var event dto.OrderPaidMessage

		if err := json.Unmarshal(msg.Value, &event); err != nil {
			log.Println("json error:", err)
			continue
		}

		log.Println("user id, order_id, price: ", event.UserID, event.OrderID, event.Price)

		ctx := context.Background()

		o := order.Order{
			OrderID: event.OrderID,
			UserID:  event.UserID,
			Price:   event.Price,
		}

		if err := h.queue.orderUC.AddOrder(ctx, o); err != nil {
			log.Println("insert error:", err)
			continue
		}

		discount, err := h.queue.discountUC.GetDiscount(ctx, event.UserID)
		if err != nil {
			log.Println("discount error:", err)
			continue
		}

		if err := h.queue.sendDiscount(event.UserID, discount); err != nil {
			log.Println("kafka produce error:", err)
		}

		log.Println("sended to discount topic: ", event.UserID, discount)

		session.MarkMessage(msg, "")
	}

	return nil
}

func (h *Handler) sendDiscount(userID uuid.UUID, discount float64) error {

	msg := dto.DiscountMessage{
		UserID:   userID,
		Discount: discount,
	}

	data, err := json.Marshal(msg)
	if err != nil {
		return err
	}

	kmsg := &sarama.ProducerMessage{
		Topic: h.cfg.TopicDiscount,
		Value: sarama.ByteEncoder(data),
	}

	_, _, err = h.producer.SendMessage(kmsg)

	return err
}
