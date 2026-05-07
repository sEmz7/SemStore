# Analytics Service Integration — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Добавить в order-service поддержку скидок пользователей и уведомления аналитики о завершённых заказах через Kafka.

**Architecture:** order-service получает скидки пользователей из топика `users-discounts` и сохраняет их в БД; при выставлении заказа на оплату применяет скидку к итоговой цене; при завершении заказа отправляет событие в топик `order-completed` для analytics-service.

**Tech Stack:** Java 21, Spring Boot, Spring Kafka, Spring Data JPA, Liquibase, Lombok, Mockito (тесты)

---

## File Map

### common-dtos
- **Create** `src/main/java/ru/semstore/common/dto/UserDiscountEvent.java` — DTO события скидки пользователя
- **Create** `src/main/java/ru/semstore/common/dto/OrderCompletedEvent.java` — DTO завершённого заказа

### order-service
- **Modify** `src/main/java/ru/semstore/orderservice/kafka/config/KafkaTopicConfig.java` — добавить топик `users-discounts`
- **Create** `src/main/resources/db/changelog/changeset/007-create-users-discounts-table.yaml` — таблица скидок
- **Modify** `src/main/resources/db/changelog/db.changelog-master.yaml` — подключить changeset 007
- **Create** `src/main/java/ru/semstore/orderservice/model/UserDiscount.java` — JPA-entity
- **Create** `src/main/java/ru/semstore/orderservice/repository/UserDiscountRepository.java` — репозиторий
- **Modify** `src/main/java/ru/semstore/orderservice/kafka/consumer/KafkaConsumer.java` — добавить listener для `users-discounts`
- **Modify** `src/main/java/ru/semstore/orderservice/kafka/producer/KafkaProducer.java` — добавить метод `sendOrderCompleted`
- **Modify** `src/main/java/ru/semstore/orderservice/service/impl/OrderServiceImpl.java` — inject `UserDiscountRepository`, вызов `sendOrderCompleted`, применение скидки
- **Modify** `src/test/java/ru/semstore/orderservice/order/OrderServiceTest.java` — моки + новые тесты

---

## Task 1: Kafka топик + Liquibase + Model + Repository

**Files:**
- Modify: `src/main/java/ru/semstore/orderservice/kafka/config/KafkaTopicConfig.java`
- Create: `src/main/resources/db/changelog/changeset/007-create-users-discounts-table.yaml`
- Modify: `src/main/resources/db/changelog/db.changelog-master.yaml`
- Create: `src/main/java/ru/semstore/orderservice/model/UserDiscount.java`
- Create: `src/main/java/ru/semstore/orderservice/repository/UserDiscountRepository.java`

- [ ] **Step 1: Добавить топик `users-discounts` в `KafkaTopicConfig`**

В файле `KafkaTopicConfig.java` добавить новый бин после `orderCompleteTopic`:

```java
@Bean NewTopic usersDiscountsTopic() {
    return TopicBuilder.name("users-discounts")
            .partitions(1)
            .replicas(1)
            .build();
}
```

- [ ] **Step 2: Создать changeset 007**

Создать файл `007-create-users-discounts-table.yaml`:

```yaml
databaseChangeLog:
  - changeSet:
      id: 007-create-users-discounts-table
      author: semyon
      preConditions:
        - onFail: MARK_RAN
        - not:
            tableExists:
              tableName: users_discounts
      changes:
        - createTable:
            tableName: users_discounts
            columns:
              - column:
                  name: id
                  type: uuid
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: user_id
                  type: uuid
                  constraints:
                    nullable: false
              - column:
                  name: discount_percent
                  type: int
                  constraints:
                    nullable: false
```

- [ ] **Step 3: Добавить include в `db.changelog-master.yaml`**

Дописать в конец файла:

```yaml
  - include:
      file: db/changelog/changeset/007-create-users-discounts-table.yaml
```

- [ ] **Step 4: Создать JPA-entity `UserDiscount`**

Создать файл `src/main/java/ru/semstore/orderservice/model/UserDiscount.java`:

```java
package ru.semstore.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users_discounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "discount_percent", nullable = false)
    private int discountPercent;
}
```

- [ ] **Step 5: Создать интерфейс репозитория `UserDiscountRepository`**

Создать файл `src/main/java/ru/semstore/orderservice/repository/UserDiscountRepository.java`:

```java
package ru.semstore.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.semstore.orderservice.model.UserDiscount;

import java.util.Optional;
import java.util.UUID;

public interface UserDiscountRepository extends JpaRepository<UserDiscount, UUID> {

    Optional<UserDiscount> findByUserId(UUID userId);
}
```

- [ ] **Step 6: Commit**

```bash
cd /Users/semzz/Desktop/study/SemStore/backend/order-service
git add src/main/java/ru/semstore/orderservice/kafka/config/KafkaTopicConfig.java \
        src/main/resources/db/changelog/changeset/007-create-users-discounts-table.yaml \
        src/main/resources/db/changelog/db.changelog-master.yaml \
        src/main/java/ru/semstore/orderservice/model/UserDiscount.java \
        src/main/java/ru/semstore/orderservice/repository/UserDiscountRepository.java
git commit -m "feat: add users-discounts topic, changeset 007, UserDiscount model and repository"
git push origin feat-analytics-service
```

---

## Task 2: DTOs в common-dtos + Consumer

**Files:**
- Create: `backend/common-dtos/src/main/java/ru/semstore/common/dto/UserDiscountEvent.java`
- Create: `backend/common-dtos/src/main/java/ru/semstore/common/dto/OrderCompletedEvent.java`
- Modify: `src/main/java/ru/semstore/orderservice/kafka/consumer/KafkaConsumer.java`

- [ ] **Step 1: Создать `UserDiscountEvent` в common-dtos**

Создать файл `backend/common-dtos/src/main/java/ru/semstore/common/dto/UserDiscountEvent.java`:

```java
package ru.semstore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDiscountEvent {
    private UUID userId;
    private int discountPercent;
}
```

- [ ] **Step 2: Создать `OrderCompletedEvent` в common-dtos**

Создать файл `backend/common-dtos/src/main/java/ru/semstore/common/dto/OrderCompletedEvent.java`:

```java
package ru.semstore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCompletedEvent {
    private UUID orderId;
    private UUID userId;
    private BigDecimal price;
}
```

- [ ] **Step 3: Добавить listener в `KafkaConsumer`**

В `KafkaConsumer.java` добавить инъекцию `UserDiscountRepository` и новый метод-слушатель. Полный файл после изменений:

```java
package ru.semstore.orderservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.semstore.common.dto.OrderCheckedEvent;
import ru.semstore.common.dto.UserDiscountEvent;
import ru.semstore.orderservice.model.Order;
import ru.semstore.orderservice.model.OrderStatus;
import ru.semstore.orderservice.model.UserDiscount;
import ru.semstore.orderservice.repository.OrderRepository;
import ru.semstore.orderservice.repository.UserDiscountRepository;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final OrderRepository orderRepository;
    private final UserDiscountRepository userDiscountRepository;

    @KafkaListener(topics = "order-checked")
    public void orderCheckedListen(OrderCheckedEvent checkedOrder) {
        log.debug("Received checked order, orderId={}", checkedOrder.getOrderId());
        Optional<Order> orderOptional = orderRepository.findById(checkedOrder.getOrderId());
        if (orderOptional.isEmpty()) {
            log.warn("Order not found with id={}", checkedOrder.getOrderId());
            return;
        }
        Order order = orderOptional.get();
        order.setStatus(checkedOrder.getValid() ? OrderStatus.CREATED : OrderStatus.CANCELED);
        log.debug("Order status changed to={}, orderId{}", order.getStatus(), order.getId());
        orderRepository.save(order);
    }

    @KafkaListener(topics = "users-discounts")
    public void userDiscountListen(UserDiscountEvent event) {
        log.debug("Received user discount event, userId={}", event.getUserId());
        UserDiscount discount = userDiscountRepository.findByUserId(event.getUserId())
                .orElseGet(UserDiscount::new);
        discount.setUserId(event.getUserId());
        discount.setDiscountPercent(event.getDiscountPercent());
        userDiscountRepository.save(discount);
        log.debug("User discount saved, userId={}, percent={}", event.getUserId(), event.getDiscountPercent());
    }
}
```

- [ ] **Step 4: Commit**

```bash
cd /Users/semzz/Desktop/study/SemStore/backend
git add common-dtos/src/main/java/ru/semstore/common/dto/UserDiscountEvent.java \
        common-dtos/src/main/java/ru/semstore/common/dto/OrderCompletedEvent.java \
        order-service/src/main/java/ru/semstore/orderservice/kafka/consumer/KafkaConsumer.java
git commit -m "feat: add UserDiscountEvent, OrderCompletedEvent DTOs and users-discounts consumer"
git push origin feat-analytics-service
```

---

## Task 3: Producer метод + отправка из `completeOrder` (TDD)

**Files:**
- Modify: `src/main/java/ru/semstore/orderservice/kafka/producer/KafkaProducer.java`
- Modify: `src/main/java/ru/semstore/orderservice/service/impl/OrderServiceImpl.java`
- Modify: `src/test/java/ru/semstore/orderservice/order/OrderServiceTest.java`

- [ ] **Step 1: Написать падающий тест для `completeOrder`**

В `OrderServiceTest.java` добавить моки и тест. В секцию `@Mock` добавить:

```java
@Mock
private OutboxRepository outboxRepository;

@Mock
private UserDiscountRepository userDiscountRepository;
```

В конец класса добавить тест:

```java
@Test
@DisplayName("Завершение заказа — отправка события в Kafka")
void completeOrder_ShouldSendOrderCompletedEvent() {
    order.setStatus(OrderStatus.DELIVERING);
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(orderRepository.findCountByAddressIdAndStatusNot(addressId, OrderStatus.COMPLETED)).thenReturn(1);
    when(orderMapper.toFullDto(order)).thenReturn(orderFullDto);

    orderService.completeOrder(orderId);

    verify(kafka, times(1)).sendOrderCompleted(order);
}
```

- [ ] **Step 2: Запустить тест — убедиться, что падает**

```bash
cd /Users/semzz/Desktop/study/SemStore/backend/order-service
./mvnw test -Dtest=OrderServiceTest#completeOrder_ShouldSendOrderCompletedEvent -pl . 2>&1 | tail -20
```

Ожидаемый результат: тест падает с ошибкой (метод `sendOrderCompleted` не существует).

- [ ] **Step 3: Добавить метод `sendOrderCompleted` в `KafkaProducer`**

В `KafkaProducer.java` добавить константу и метод:

```java
private final String ORDER_COMPLETED_TOPIC = "order-completed";

public void sendOrderCompleted(Order order) {
    OrderCompletedEvent event = new OrderCompletedEvent(
            order.getId(), order.getUserId(), order.getTotalPrice());
    kafkaTemplate.send(ORDER_COMPLETED_TOPIC, event);
    log.debug("Order completed event sent to kafka, orderId={}", order.getId());
}
```

Добавить импорт: `import ru.semstore.common.dto.OrderCompletedEvent;`

- [ ] **Step 4: Вызвать `sendOrderCompleted` в `completeOrder`**

В `OrderServiceImpl.java` в методе `completeOrder`, сразу после `orderRepository.save(order)` и **до** блока с outbox:

```java
order.setStatus(OrderStatus.COMPLETED);
orderRepository.save(order);
kafka.sendOrderCompleted(order);  // <-- добавить эту строку

int ordersCountWithSameAddress = orderRepository.findCountByAddressIdAndStatusNot(...
```

- [ ] **Step 5: Запустить тест — убедиться, что проходит**

```bash
./mvnw test -Dtest=OrderServiceTest -pl . 2>&1 | tail -20
```

Ожидаемый результат: `BUILD SUCCESS`, все тесты зелёные.

- [ ] **Step 6: Commit**

```bash
cd /Users/semzz/Desktop/study/SemStore/backend
git add order-service/src/main/java/ru/semstore/orderservice/kafka/producer/KafkaProducer.java \
        order-service/src/main/java/ru/semstore/orderservice/service/impl/OrderServiceImpl.java \
        order-service/src/test/java/ru/semstore/orderservice/order/OrderServiceTest.java
git commit -m "feat: add OrderCompletedEvent producer and send from completeOrder"
git push origin feat-analytics-service
```

---

## Task 4: Применение скидки в `submitOrderForPayment` (TDD)

**Files:**
- Modify: `src/main/java/ru/semstore/orderservice/service/impl/OrderServiceImpl.java`
- Modify: `src/test/java/ru/semstore/orderservice/order/OrderServiceTest.java`

- [ ] **Step 1: Написать падающие тесты**

В `OrderServiceTest.java` добавить два теста (если `@Mock UserDiscountRepository userDiscountRepository` ещё не добавлен из Task 3 — добавить):

```java
@Test
@DisplayName("submitOrderForPayment — скидка применяется к totalPrice")
void submitOrderForPayment_ShouldApplyDiscount() {
    OrderItem item = new OrderItem(UUID.randomUUID(), order, "link", "size", "cfg",
            new BigDecimal("100.00"));
    order.setItems(List.of(item));
    order.setStatus(OrderStatus.IN_CHECK);

    UserDiscount discount = new UserDiscount();
    discount.setUserId(userId);
    discount.setDiscountPercent(10);

    when(orderRepository.findOrderByIdWithItems(orderId)).thenReturn(Optional.of(order));
    when(userDiscountRepository.findByUserId(userId)).thenReturn(Optional.of(discount));
    when(orderRepository.save(order)).thenReturn(order);
    when(orderMapper.toFullDto(order)).thenReturn(orderFullDto);

    orderService.submitOrderForPayment(orderId);

    assertEquals(new BigDecimal("90.00"), order.getTotalPrice());
}

@Test
@DisplayName("submitOrderForPayment — без скидки, totalPrice не меняется")
void submitOrderForPayment_ShouldNotApplyDiscount_WhenNoDiscount() {
    OrderItem item = new OrderItem(UUID.randomUUID(), order, "link", "size", "cfg",
            new BigDecimal("100.00"));
    order.setItems(List.of(item));
    order.setStatus(OrderStatus.IN_CHECK);

    when(orderRepository.findOrderByIdWithItems(orderId)).thenReturn(Optional.of(order));
    when(userDiscountRepository.findByUserId(userId)).thenReturn(Optional.empty());
    when(orderRepository.save(order)).thenReturn(order);
    when(orderMapper.toFullDto(order)).thenReturn(orderFullDto);

    orderService.submitOrderForPayment(orderId);

    assertEquals(new BigDecimal("100.00"), order.getTotalPrice());
}
```

- [ ] **Step 2: Запустить тесты — убедиться, что падают**

```bash
cd /Users/semzz/Desktop/study/SemStore/backend/order-service
./mvnw test -Dtest="OrderServiceTest#submitOrderForPayment*" -pl . 2>&1 | tail -20
```

Ожидаемый результат: тесты падают (поле `userDiscountRepository` null / метод не реализован).

- [ ] **Step 3: Добавить `UserDiscountRepository` в `OrderServiceImpl`**

В `OrderServiceImpl.java` добавить поле (все поля `final` — `@RequiredArgsConstructor` создаст конструктор автоматически):

```java
private final UserDiscountRepository userDiscountRepository;
```

Добавить импорты:
```java
import ru.semstore.orderservice.model.UserDiscount;
import ru.semstore.orderservice.repository.UserDiscountRepository;
import java.util.Optional;
```

- [ ] **Step 4: Применить скидку в `submitOrderForPayment`**

В методе `submitOrderForPayment`, после окончания цикла `for(OrderItem item: order.getItems())` и перед `order.setStatus(...)`, добавить блок:

```java
// было:
// order.setStatus(OrderStatus.AWAITING_PAYMENT);
// order.setTotalPrice(totalPrice);

// стало:
Optional<UserDiscount> discountOpt = userDiscountRepository.findByUserId(order.getUserId());
if (discountOpt.isPresent()) {
    int percent = discountOpt.get().getDiscountPercent();
    totalPrice = totalPrice
            .multiply(BigDecimal.valueOf(100 - percent))
            .divide(BigDecimal.valueOf(100));
}

order.setStatus(OrderStatus.AWAITING_PAYMENT);
order.setTotalPrice(totalPrice);
```

Строки `order.setStatus(...)` и `order.setTotalPrice(...)` остаются на своих местах — меняется только то, что между ними вставляется блок со скидкой.

- [ ] **Step 5: Запустить все тесты**

```bash
cd /Users/semzz/Desktop/study/SemStore/backend/order-service
./mvnw test -pl . 2>&1 | tail -30
```

Ожидаемый результат: `BUILD SUCCESS`, все тесты зелёные.

- [ ] **Step 6: Commit**

```bash
cd /Users/semzz/Desktop/study/SemStore/backend
git add order-service/src/main/java/ru/semstore/orderservice/service/impl/OrderServiceImpl.java \
        order-service/src/test/java/ru/semstore/orderservice/order/OrderServiceTest.java
git commit -m "feat: apply user discount to totalPrice in submitOrderForPayment"
git push origin feat-analytics-service
```
