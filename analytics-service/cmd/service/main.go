package main

import (
	"context"
	"log"
	"os"
	"os/signal"
	"sync"
	"syscall"
	"time"

	"analytics-service/config"
	"analytics-service/internal/database"
	"analytics-service/internal/handler/queue"
	orderRepo "analytics-service/internal/repository/order"
	discountUC "analytics-service/internal/usecase/discount"
	orderUC "analytics-service/internal/usecase/order"
)

func main() {
	cfg := config.LoadConfig("")

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	dbCtx, dbCancel := context.WithTimeout(ctx, 30*time.Second)
	defer dbCancel()

	db, err := database.NewDB(dbCtx, &cfg.Clickhouse)
	if err != nil {
		log.Fatalf("db connect: %v", err)
	}
	defer func() {
		if err := db.Close(); err != nil {
			log.Printf("db close: %v", err)
		}
	}()

	repo := orderRepo.NewRepository(db)
	orderUsecase := orderUC.NewOrderUsecase(repo)
	discountUsecase := discountUC.NewDiscountUsecase(repo)

	queueHandler, err := queue.NewHandler(
		&cfg.Kafka,
		orderUsecase,
		discountUsecase,
	)
	if err != nil {
		log.Fatalf("kafka init: %v", err)
	}

	var wg sync.WaitGroup

	wg.Add(1)
	go func() {
		defer wg.Done()
		if err := queueHandler.StartConsumer(ctx); err != nil {
			log.Printf("consumer stopped: %v", err)
		}
	}()

	log.Println("service started")

	stop := make(chan os.Signal, 1)
	signal.Notify(stop, os.Interrupt, syscall.SIGTERM)
	<-stop

	log.Println("shutdown signal received")
	cancel()
	wg.Wait()

	log.Println("closing kafka")
	queueHandler.Close()

	log.Println("service stopped")
}
