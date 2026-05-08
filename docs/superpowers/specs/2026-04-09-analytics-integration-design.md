# Design: Analytics Service Integration (Order Service Changes)

**Date:** 2026-04-09
**Branch:** feat-analytics-service

## Overview

Integrate order-service with the new analytics service via Kafka. Add user discount support: store discounts received from analytics service, apply them when submitting orders for payment, and notify analytics when orders complete.

## Scope

All changes are confined to `order-service` and `common-dtos`. No changes to other services.

---

## Part 1 — Kafka Topic: `users-discounts`

Add a new `@Bean` in `KafkaTopicConfig`:

```java
@Bean NewTopic usersDiscountsTopic() {
    return TopicBuilder.name("users-discounts").partitions(1).replicas(1).build();
}
```

Note: `order-completed` topic already exists in the config — no changes needed there.

---

## Part 2 — Liquibase Changeset `007`

New file: `007-create-users-discounts-table.yaml`

Table: `users_discounts`

| Column           | Type    | Constraints          |
|------------------|---------|----------------------|
| id               | uuid    | PK, not null         |
| user_id          | uuid    | not null             |
| discount_percent | int     | not null             |

Add include to `db.changelog-master.yaml`.

---

## Part 3 — Model, Repository, Consumer

**Model** `UserDiscount` — JPA entity mapped to `users_discounts`.

**Repository** `UserDiscountRepository extends JpaRepository<UserDiscount, UUID>`:
- `Optional<UserDiscount> findByUserId(UUID userId)`

**Consumer** — new `@KafkaListener(topics = "users-discounts")` method inside existing `KafkaConsumer` class. Accepts a DTO (from `common-dtos`) with `userId` and `discountPercent`. Logic: find existing record by `userId` and update, or create new one (upsert via save).

DTO in `common-dtos`: `UserDiscountEvent { UUID userId; int discountPercent; }`

---

## Part 4 — Send to `order-completed` from `completeOrder`

**New DTO** in `common-dtos`: `OrderCompletedEvent { UUID orderId; UUID userId; BigDecimal price; }`

**New producer method** in `KafkaProducer`:
```java
public void sendOrderCompleted(Order order) {
    OrderCompletedEvent event = new OrderCompletedEvent(order.getId(), order.getUserId(), order.getTotalPrice());
    kafkaTemplate.send(ORDER_COMPLETED_TOPIC, event);
}
```

**In `completeOrder`** — after `orderRepository.save(order)`, call `kafka.sendOrderCompleted(order)`. The existing outbox logic for address deletion is not touched.

---

## Part 5 — Apply Discount in `submitOrderForPayment`

After the existing loop that calculates `totalPrice`, add a new block (do not modify the existing loop):

```java
Optional<UserDiscount> discountOpt = userDiscountRepository.findByUserId(order.getUserId());
if (discountOpt.isPresent()) {
    int percent = discountOpt.get().getDiscountPercent();
    totalPrice = totalPrice
        .multiply(BigDecimal.valueOf(100 - percent))
        .divide(BigDecimal.valueOf(100));
}
order.setTotalPrice(totalPrice);
```

Remove the existing `order.setTotalPrice(totalPrice)` line (it moves into this block).

---

## Commit Plan

1. `feat: add users-discounts topic, changeset 007, UserDiscount model, repository and consumer`
2. `feat: add OrderCompletedEvent DTO and send to order-completed topic from completeOrder`
3. `feat: apply user discount to totalPrice in submitOrderForPayment`

Each commit is pushed to `feat-analytics-service`.
