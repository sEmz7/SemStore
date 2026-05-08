//go:build integration

package integration_test

import (
	"context"
	"fmt"
	"testing"
	"time"

	"analytics-service/internal/database"
	"analytics-service/internal/model/order"
	orderRepo "analytics-service/internal/repository/order"

	"github.com/ClickHouse/clickhouse-go/v2"
	"github.com/google/uuid"
	"github.com/stretchr/testify/suite"
	"github.com/testcontainers/testcontainers-go"
)

type RepositorySuite struct {
	suite.Suite

	ctx context.Context

	container testcontainers.Container
	db        *database.DB
	repo      *orderRepo.Repository
}

func createTestDB(ctx context.Context, host, port string) (*database.DB, error) {
	conn, err := clickhouse.Open(&clickhouse.Options{
		Addr: []string{fmt.Sprintf("%s:%s", host, port)},
		Auth: clickhouse.Auth{
			Database: "analytics",
			Username: "test",
			Password: "test",
		},
		DialTimeout:  5 * time.Second,
		MaxOpenConns: 5,
	})
	if err != nil {
		return nil, err
	}

	if err := conn.Ping(ctx); err != nil {
		return nil, err
	}

	return &database.DB{Client: conn}, nil
}

func TestRepositorySuite(t *testing.T) {
	suite.Run(t, new(RepositorySuite))
}

func (s *RepositorySuite) SetupSuite() {
	s.ctx = context.Background()

	container, host, port, err := StartClickhouse(s.ctx, "../../migrations")
	s.Require().NoError(err)

	s.container = container

	db, err := createTestDB(s.ctx, host, port)
	s.Require().NoError(err)

	s.db = db
	s.repo = orderRepo.NewRepository(db)
}

func (s *RepositorySuite) TearDownSuite() {
	s.db.Client.Close()
	s.container.Terminate(s.ctx)
}

func (s *RepositorySuite) SetupTest() {
	err := s.db.Client.Exec(s.ctx, "TRUNCATE TABLE analytics.orders")
	s.Require().NoError(err)

	err = s.db.Client.Exec(s.ctx, "TRUNCATE TABLE analytics.user_spend")
	s.Require().NoError(err)
}

func (s *RepositorySuite) TestCreateOrder() {
	user := uuid.New()

	err := s.repo.CreateOrder(s.ctx, order.Order{
		OrderID: uuid.New(),
		UserID:  user,
		Price:   100,
	})

	s.Require().NoError(err)

	var count uint64
	err = s.db.Client.
		QueryRow(s.ctx, "SELECT COUNT(*) FROM analytics.orders").
		Scan(&count)

	s.Require().NoError(err)
	s.Equal(uint64(1), count)
}

func (s *RepositorySuite) TestGetDiscount() {
	user1 := uuid.New()
	user2 := uuid.New()

	err := s.repo.CreateOrder(s.ctx, order.Order{
		OrderID: uuid.New(),
		UserID:  user1,
		Price:   100,
	})
	s.Require().NoError(err)

	err = s.repo.CreateOrder(s.ctx, order.Order{
		OrderID: uuid.New(),
		UserID:  user2,
		Price:   50,
	})
	s.Require().NoError(err)

	// time.Sleep(time.Second)

	discount1, err := s.repo.GetDiscountByUserID(s.ctx, user1)
	s.Require().NoError(err)

	discount2, err := s.repo.GetDiscountByUserID(s.ctx, user2)
	s.Require().NoError(err)

	s.Greater(discount1, float64(0))
	s.Equal(float64(0), discount2)
}
