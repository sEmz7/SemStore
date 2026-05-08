//go:build integration

package integration_test

import (
	"context"
	"database/sql"
	"fmt"
	"time"

	_ "github.com/ClickHouse/clickhouse-go/v2"
	"github.com/pressly/goose/v3"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/wait"
)

func StartClickhouse(ctx context.Context, migrationsPath string) (testcontainers.Container, string, string, error) {
	req := testcontainers.ContainerRequest{
		Image:        "clickhouse/clickhouse-server:23.8",
		ExposedPorts: []string{"9000/tcp"},
		Env: map[string]string{
			"CLICKHOUSE_DB":       "analytics",
			"CLICKHOUSE_USER":     "test",
			"CLICKHOUSE_PASSWORD": "test",
		},
		WaitingFor: wait.ForListeningPort("9000/tcp").WithStartupTimeout(60 * time.Second),
	}

	container, err := testcontainers.GenericContainer(ctx,
		testcontainers.GenericContainerRequest{
			ContainerRequest: req,
			Started:          true,
		})
	if err != nil {
		return nil, "", "", err
	}

	host, err := container.Host(ctx)
	if err != nil {
		return nil, "", "", err
	}

	port, err := container.MappedPort(ctx, "9000")
	if err != nil {
		return nil, "", "", err
	}

	dsn := fmt.Sprintf("clickhouse://test:test@%s:%s/default", host, port.Port())

	sqlDB, err := sql.Open("clickhouse", dsn)
	if err != nil {
		return nil, "", "", err
	}
	defer sqlDB.Close()

	for i := 0; i < 20; i++ {
		if err := sqlDB.Ping(); err == nil {
			break
		}
		time.Sleep(time.Second)
	}

	if err := goose.SetDialect("clickhouse"); err != nil {
		return nil, "", "", err
	}

	if err := goose.Up(sqlDB, migrationsPath); err != nil {
		return nil, "", "", err
	}

	return container, host, port.Port(), nil
}
