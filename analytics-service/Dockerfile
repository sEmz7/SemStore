FROM golang:1.25 AS builder

WORKDIR /src
COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o /app ./cmd/service

FROM gcr.io/distroless/static:nonroot
COPY --from=builder /app /app
EXPOSE 8080
USER nonroot:nonroot
ENTRYPOINT ["/app"]