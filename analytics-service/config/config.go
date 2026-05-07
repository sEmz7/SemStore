package config

import (
	"log"
	"os"
	"strconv"
	"time"

	"github.com/joho/godotenv"
)

type Config struct {
	Clickhouse ClickhouseConfig
	Server     ServerConfig
	Kafka      KafkaConfig
}

type ClickhouseConfig struct {
	Host string
	Port string
	DB   string
	User string
	Pass string

	Attempts  int
	BaseDelay time.Duration
}

type ServerConfig struct {
	Port string
}

type KafkaConfig struct {
	Host          string
	Port          string
	TopicPaid     string
	TopicDiscount string
	GroupID       string
}

func LoadConfig(envPath string) *Config {
	if envPath != "" {
		if err := godotenv.Load(envPath); err != nil {
			log.Fatalf("Could not load env file %s: %v", envPath, err)
		}
	}

	attempts, _ := strconv.Atoi(getEnv("DB_ATTEMPTS", "5"))
	base, _ := strconv.Atoi(getEnv("DB_BASE_DELAY", "2"))

	return &Config{
		Clickhouse: ClickhouseConfig{
			Host:      getEnv("CLICKHOUSE_HOST", "localhost"),
			Port:      getEnv("CLICKHOUSE_TCP_PORT", "9000"),
			DB:        getEnv("CLICKHOUSE_DB", "analytics"),
			User:      getEnv("CLICKHOUSE_USER", "default"),
			Pass:      getEnv("CLICKHOUSE_PASSWORD", ""),
			Attempts:  attempts,
			BaseDelay: time.Second * time.Duration(base),
		},
		Server: ServerConfig{
			Port: getEnv("APP_PORT", "8080"),
		},
		Kafka: KafkaConfig{
			Host:          getEnv("KAFKA_HOST", "kafka"),
			Port:          getEnv("KAFKA_PORT", "9092"),
			TopicPaid:     getEnv("KAFKA_TOPIC_ORDER_COMPLETED", "orders.paid"),
			TopicDiscount: getEnv("KAFKA_TOPIC_DISCOUNT", "users.discount"),
			GroupID:       getEnv("KAFKA_GROUP_ID", "analytics-service"),
		},
	}
}

func getEnv(key, def string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return def
}
