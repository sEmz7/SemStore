# Design: Подтверждение заказа (feat-confirm-order)

**Дата:** 2026-04-04  
**Ветка:** feat-confirm-order

---

## Цель

Добавить на страницу деталей заказа кнопку "Оформить заказ", которая переводит заказ в статус `IN_CHECK`. После подтверждения пользователь не может изменять состав заказа — форма добавления и кнопки удаления блокируются.

---

## Бэкенд (уже готов)

```
POST /api/orders/{orderId}/confirm
→ 200 OrderDto  — заказ переведён в IN_CHECK
→ 400           — нет товаров или статус не CREATED
```

Проверка происходит мгновенно: после `IN_CHECK` заказ автоматически переходит в `CREATED` (валиден) или `CANCELED` (не валиден) на стороне бэкенда.

---

## Изменения

### 1. `api/types.ts`
Добавить `"IN_CHECK"` в `OrderStatus`:
```ts
export type OrderStatus = "CREATED" | "IN_CHECK" | "PENDING" | "CANCELED" | "ORDERED" | "PAID";
```

### 2. `api/orders.ts`
Добавить функцию:
```ts
export async function confirmOrder(orderId: string): Promise<OrderDto> {
  const { data } = await orderApi.post<OrderDto>(`/${orderId}/confirm`);
  return data;
}
```

### 3. Локализация (`ru.ts` / `en.ts`)
```ts
// orderStatus
IN_CHECK: "На проверке"  // en: "In review"

// order
editingLocked: "Изменение недоступно для текущего статуса заказа"
// en: "Editing is not available for the current order status"
```

### 4. `pages/OrderDetailsPage.tsx`

**Флаг редактируемости:**
```ts
const isEditable = order?.status === "CREATED";
```

**Кнопка "Оформить заказ"** — в шапке рядом с "Обновить":
- Видна только когда `isEditable && items.length > 0`
- При клике: `confirmOrder(id)` → `reload()`
- Показывает loading state во время запроса

**Баннер блокировки** — amber/yellow блок под шапкой:
- Показывается когда `!isEditable && order !== null`
- Текст: `order.editingLocked`

**Форма добавления товара:**
- Все `<FormField>` и кнопка "Добавить товар" получают `disabled={!isEditable}`

**Кнопки удаления товаров:**
- `disabled={!isEditable || isDeleting}`
- Кнопка "Открыть" остаётся активной всегда

---

## Затронутые файлы

| Файл | Изменение |
|---|---|
| `frontend/src/api/types.ts` | Добавить `IN_CHECK` в `OrderStatus` |
| `frontend/src/api/orders.ts` | Добавить `confirmOrder()` |
| `frontend/src/locales/ru.ts` | Ключи `IN_CHECK`, `editingLocked` |
| `frontend/src/locales/en.ts` | Ключи `IN_CHECK`, `editingLocked` |
| `frontend/src/pages/OrderDetailsPage.tsx` | Кнопка, баннер, блокировка |
