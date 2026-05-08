include .env
export $(shell sed 's/=.*//' .env)

GOOSE_DRIVER = clickhouse
GOOSE_DBSTRING = clickhouse://$(CLICKHOUSE_USER):$(CLICKHOUSE_PASSWORD)@localhost:$(CLICKHOUSE_TCP_PORT)/$(CLICKHOUSE_DB)

MIGRATIONS_DIR = $(MIGRATIONS_PATH)

.PHONY: migrate-up migrate-down migrate-status

migrate-up:
	@echo "Applying migrations..."
	GOOSE_DRIVER=$(GOOSE_DRIVER) GOOSE_DBSTRING="$(GOOSE_DBSTRING)" goose -dir $(MIGRATIONS_DIR) up

migrate-down:
	@echo "Rollback migration..."
	GOOSE_DRIVER=$(GOOSE_DRIVER) GOOSE_DBSTRING="$(GOOSE_DBSTRING)" goose -dir $(MIGRATIONS_DIR) down

migrate-status:
	@echo "Migration status:"
	GOOSE_DRIVER=$(GOOSE_DRIVER) GOOSE_DBSTRING="$(GOOSE_DBSTRING)" goose -dir $(MIGRATIONS_DIR) status