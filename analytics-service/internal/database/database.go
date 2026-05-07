package database

import (
	"context"
	"fmt"
	"log"
	"time"

	"analytics-service/config"

	"github.com/ClickHouse/clickhouse-go/v2"
)

type DB struct {
	Client clickhouse.Conn
	Cfg    *config.ClickhouseConfig
}

func NewDB(ctx context.Context, cfg *config.ClickhouseConfig) (*DB, error) {
	addr := fmt.Sprintf("%s:%s", cfg.Host, cfg.Port)

	var lastErr error
	var conn clickhouse.Conn

	for i := 0; i < cfg.Attempts; i++ {
		options := &clickhouse.Options{
			Addr: []string{addr},
			Auth: clickhouse.Auth{
				Database: cfg.DB,
				Username: cfg.User,
				Password: cfg.Pass,
			},
			Compression: &clickhouse.Compression{
				Method: clickhouse.CompressionLZ4,
			},
		}
		c, err := clickhouse.Open(options)
		if err == nil {
			pingCtx, cancel := context.WithTimeout(ctx, 5*time.Second)
			defer cancel()
			if pingErr := c.Ping(pingCtx); pingErr == nil {
				conn = c
				log.Println("connected to ClickHouse")
				return &DB{Client: conn, Cfg: cfg}, nil
			} else {
				lastErr = fmt.Errorf("clickhouse ping: %w", pingErr)
				_ = c.Close()
			}
		} else {
			lastErr = fmt.Errorf("clickhouse open: %w", err)
		}

		delay := time.Duration(i+1) * cfg.BaseDelay
		log.Printf("DB connect attempt %d/%d failed: %v — retry %s", i+1, cfg.Attempts, lastErr, delay)
		select {
		case <-time.After(delay):
		case <-ctx.Done():
			return nil, ctx.Err()
		}
	}
	return nil, fmt.Errorf("could not connect to ClickHouse: %w", lastErr)
}

func (db *DB) Close() error {
	if db.Client != nil {
		return db.Client.Close()
	}
	return nil
}
