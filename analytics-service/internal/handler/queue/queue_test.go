package queue

import (
	"context"
	"encoding/json"
	"errors"
	"testing"

	"analytics-service/config"
	"analytics-service/internal/dto"
	queue_mocks "analytics-service/internal/handler/queue/mocks"

	"github.com/IBM/sarama"
	"github.com/google/uuid"
	"go.uber.org/mock/gomock"
)

type mockSession struct {
	marked bool
}

func (m *mockSession) MarkMessage(msg *sarama.ConsumerMessage, metadata string) {
	m.marked = true
}

func (m *mockSession) Commit()                                  {}
func (m *mockSession) Context() context.Context                 { return context.Background() }
func (m *mockSession) Claims() map[string][]int32               { return nil }
func (m *mockSession) MemberID() string                         { return "" }
func (m *mockSession) GenerationID() int32                      { return 0 }
func (m *mockSession) ResetOffset(string, int32, int64, string) {}
func (m *mockSession) MarkOffset(string, int32, int64, string)  {}

type mockClaim struct {
	messages chan *sarama.ConsumerMessage
}

func (m *mockClaim) Topic() string              { return "" }
func (m *mockClaim) Partition() int32           { return 0 }
func (m *mockClaim) InitialOffset() int64       { return 0 }
func (m *mockClaim) HighWaterMarkOffset() int64 { return 0 }
func (m *mockClaim) Messages() <-chan *sarama.ConsumerMessage {
	return m.messages
}

func Test_sendDiscount(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	userID := uuid.New()

	tests := []struct {
		name    string
		err     error
		wantErr bool
	}{
		{"success", nil, false},
		{"producer error", errors.New("fail"), true},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {

			producer := queue_mocks.NewMockSyncProducer(ctrl)

			producer.EXPECT().
				SendMessage(gomock.Any()).
				DoAndReturn(func(msg *sarama.ProducerMessage) (int32, int64, error) {

					var decoded dto.DiscountMessage
					bytes, _ := msg.Value.Encode()
					_ = json.Unmarshal(bytes, &decoded)

					if decoded.UserID != userID {
						t.Errorf("wrong userID")
					}

					return 0, 0, tt.err
				})

			h := &Handler{
				cfg:      &config.KafkaConfig{TopicDiscount: "test"},
				producer: producer,
			}

			err := h.sendDiscount(userID, 10)

			if (err != nil) != tt.wantErr {
				t.Fatalf("unexpected error: %v", err)
			}
		})
	}
}

func Test_ConsumeClaim(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	tests := []struct {
		name         string
		orderErr     error
		discountErr  error
		expectMarked bool
	}{
		{"success", nil, nil, true},
		{"order error", errors.New("fail"), nil, false},
		{"discount error", nil, errors.New("fail"), false},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			ctrl := gomock.NewController(t)
			defer ctrl.Finish()

			orderUC := queue_mocks.NewMockOrderUsecase(ctrl)
			discountUC := queue_mocks.NewMockDiscountUsecase(ctrl)
			producer := queue_mocks.NewMockSyncProducer(ctrl)

			userID := uuid.New()

			orderUC.EXPECT().
				AddOrder(gomock.Any(), gomock.Any()).
				Return(tt.orderErr)

			if tt.orderErr == nil {
				discountUC.EXPECT().
					GetDiscount(gomock.Any(), gomock.Any()).
					Return(10.0, tt.discountErr)
			}

			if tt.orderErr == nil && tt.discountErr == nil {
				producer.EXPECT().
					SendMessage(gomock.Any()).
					Return(int32(0), int64(0), nil)
			}

			handler := &Handler{
				cfg:        &config.KafkaConfig{TopicDiscount: "test"},
				orderUC:    orderUC,
				discountUC: discountUC,
				producer:   producer,
			}

			consumer := &consumerGroupHandler{queue: handler}

			event := dto.OrderPaidMessage{
				OrderID: uuid.New(),
				UserID:  userID,
				Price:   100,
			}

			data, _ := json.Marshal(event)

			msgChan := make(chan *sarama.ConsumerMessage, 1)
			msgChan <- &sarama.ConsumerMessage{Value: data}
			close(msgChan)

			claim := &mockClaim{messages: msgChan}
			session := &mockSession{}

			_ = consumer.ConsumeClaim(session, claim)

			if session.marked != tt.expectMarked {
				t.Fatalf("mark mismatch")
			}
		})
	}
}
