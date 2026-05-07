# Дизайн: Панель модератора

**Дата:** 2026-04-04
**Ветка:** feat-add-admin-profile

## Цель

Создать панель модератора (ROLE_ADMIN), где администратор может:
1. Просматривать все заказы пользователей
2. Выставлять цены товарам в заказах со статусом `IN_CHECK`
3. Отправлять заказ на оплату пользователю (переводить в статус `AWAITING_PAYMENT`)

Все изменения — только в `frontend/`. Backend не трогаем.

---

## Backend API (только чтение, не меняем)

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/admin/orders?page&size&status` | Список всех заказов (пагинация, фильтр по статусу) |
| GET | `/admin/orders/{orderId}` | Детали заказа с товарами и totalPrice |
| PATCH | `/admin/orders/{orderId}/items/{itemId}` | Выставить цену товару: `{ price: number }` |
| PATCH | `/admin/orders/{orderId}/submit` | Отправить заказ на оплату |

Все admin endpoints проксируются через Vite: `/api/admin` → `http://localhost:8080/admin/...`

**Новый статус после submit:** `AWAITING_PAYMENT`

**Полный enum статусов:**
```
CREATED | IN_CHECK | AWAITING_PAYMENT | PAID | DELIVERING | COMPLETED | CANCELED | PENDING
```

---

## Data Layer

### Изменения в `api/types.ts`

```typescript
// Добавить role в UserDto
type UserDto = {
  id: string;
  email: string;
  role: "ROLE_ADMIN" | "ROLE_USER";
};

// Расширить OrderStatus новыми статусами
type OrderStatus =
  | "CREATED" | "IN_CHECK" | "PENDING" | "CANCELED"
  | "ORDERED" | "PAID"
  | "AWAITING_PAYMENT" | "DELIVERING" | "COMPLETED";

// Новые типы для admin
type OrderShortDto = {
  id: string;
  name: string;
  userId: string;
  addressId: string;
  status: OrderStatus;
  createdDate: string;
  trackingNumber?: string;
};

type OrderFullDto = {
  id: string;
  name: string;
  userId: string;
  addressId: string;
  status: OrderStatus;
  createdDate: string;
  trackingNumber?: string;
  items: OrderItemDto[];
  totalPrice?: number;
};

type OrderItemDto = {
  id: string;
  orderId: string;
  link: string;
  size: string;
  configuration: string;
  price?: number;
};
```

### Новый файл `api/adminOrders.ts`

Четвёртый axios инстанс `adminApi` с `baseURL: "/api/admin"`.

Функции:
- `listAdminOrders(page, size, status?)` → `PageResponse<OrderShortDto>`
- `getAdminOrder(orderId)` → `OrderFullDto`
- `setItemPrice(orderId, itemId, price)` → `OrderItemDto`
- `submitOrder(orderId)` → `OrderFullDto`

---

## Маршрутизация

### Новые маршруты в `App.tsx`

```
/admin                → AdminOrdersPage
/admin/orders/:id     → AdminOrderDetailsPage
```

Оба маршрута обёрнуты в `AdminRoute`.

### `AdminRoute` (новый компонент)

Аналог `ProtectedRoute`, но с двумя уровнями проверки:
1. Не авторизован → редирект на `/login`
2. Авторизован с ролью `ROLE_USER` → редирект на `/`

---

## Навигация (`Layout.tsx`)

Admin-пользователь видит **только** ссылку "Панель модератора" (`/admin`). Обычные разделы (Заказы, Адреса, Профиль) скрыты. Определяется через `user.role === "ROLE_ADMIN"`.

---

## Страницы

### `AdminOrdersPage` (`/admin`)

- Заголовок "Панель модератора"
- Фильтр по статусу (select) — по умолчанию `IN_CHECK`
- Таблица заказов (пагинация 10 на странице):
  - Колонки: Название, ID пользователя, Статус, Дата создания, Действие
  - Кнопка "Открыть" → `/admin/orders/:id`
- Хук: `useAdminOrders()`

### `AdminOrderDetailsPage` (`/admin/orders/:id`)

- Шапка: название заказа, статус (`StatusBadge`), totalPrice (если выставлена)
- Таблица товаров — каждая строка:
  - Ссылка, размер, конфигурация
  - Поле ввода цены (inline input, только если статус `IN_CHECK`)
  - Кнопка "Сохранить" рядом с полем
- Кнопка **"Выставить на оплату"**:
  - Видна только при статусе `IN_CHECK`
  - Активна только когда у всех товаров `price != null && price > 0`
  - При клике → `ConfirmDialog` → вызов `submitOrder()`
  - После успеха → заказ перезагружается, статус `AWAITING_PAYMENT`
- Хук: `useAdminOrderDetails()`

---

## Новые хуки

### `useAdminOrders()`
Состояние: список заказов, текущая страница, фильтр по статусу, isLoading.
Методы: загрузка списка, смена страницы, смена фильтра.

### `useAdminOrderDetails(orderId)`
Состояние: заказ (`OrderFullDto`), isLoading, isSaving (флаг для кнопки submit).
Методы:
- `saveItemPrice(itemId, price)` → вызывает `setItemPrice`, обновляет цену в локальном состоянии
- `handleSubmit()` → вызывает `submitOrder`, перезагружает заказ

---

## Обработка ошибок

| Ситуация | Поведение |
|----------|-----------|
| Цена <= 0 | Валидация на клиенте, кнопка "Сохранить" неактивна |
| Ошибка при сохранении цены | Toast с текстом ошибки, поле остаётся редактируемым |
| Submit — не все цены выставлены | Toast с ошибкой от бэкенда |
| Submit — успех | Перезагрузка заказа, статус AWAITING_PAYMENT, поля цены и кнопка скрываются |
| Обычный пользователь заходит на `/admin` | Редирект на `/` |

---

## Переиспользуемые компоненты (без изменений)

- `Layout` — основная обёртка (изменяем навигацию внутри)
- `StatusBadge` — добавляем цвета для новых статусов
- `ConfirmDialog` — диалог подтверждения submit
- `Toast` / `useAppToast` — уведомления
- `FormField` — поля ввода цены

---

## Изменения на стороне пользователя

**Не трогаем пользовательские страницы**, но:

1. `api/types.ts` — расширяем `UserDto` и `OrderStatus`
2. `StatusBadge.tsx` — добавляем цвета для `AWAITING_PAYMENT`, `DELIVERING`, `COMPLETED`
3. `locales/ru.ts` и `locales/en.ts` — добавляем переводы новых статусов и текст баннера для `AWAITING_PAYMENT`
4. `OrderDetailsPage.tsx` — добавляем баннер для статуса `AWAITING_PAYMENT` (заказ ожидает оплаты, редактирование заблокировано — уже работает через `isEditable`)

---

## Файловая структура (новые файлы)

```
frontend/src/
├── api/
│   └── adminOrders.ts          # Admin API функции + adminApi axios instance
├── components/
│   └── AdminRoute.tsx           # Защищённый маршрут для ROLE_ADMIN
├── hooks/
│   ├── useAdminOrders.ts        # Список заказов для модератора
│   └── useAdminOrderDetails.ts  # Детали заказа + выставление цен
└── pages/
    ├── AdminOrdersPage.tsx       # Список всех заказов
    └── AdminOrderDetailsPage.tsx # Детали заказа с ценами
```

---

## Что НЕ входит в эту итерацию

- Оплата заказа пользователем (только отображение статуса `AWAITING_PAYMENT`)
- Управление статусами `DELIVERING`, `COMPLETED`
- Фильтрация по дате в панели модератора
- Назначение трек-номера
