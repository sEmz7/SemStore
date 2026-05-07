# Confirm Order Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Добавить кнопку "Оформить заказ" на страницу деталей заказа, которая вызывает `POST /orders/{id}/confirm` и блокирует редактирование при статусе не `CREATED`.

**Architecture:** Все изменения сосредоточены в одном файле страницы (`OrderDetailsPage.tsx`). Флаг `isEditable = order.status === "CREATED"` управляет disabled-состоянием формы, кнопок удаления и видимостью кнопки подтверждения. Перед кнопкой добавляется информационный баннер при заблокированном статусе.

**Tech Stack:** React 19, TypeScript, Tailwind CSS 3, Axios, i18next

---

### Task 1: Добавить `IN_CHECK` в тип `OrderStatus`

**Files:**
- Modify: `frontend/src/api/types.ts`

- [ ] **Step 1: Обновить тип `OrderStatus`**

В файле `frontend/src/api/types.ts` заменить строку:
```ts
export type OrderStatus = "CREATED" | "PENDING" | "CANCELED" | "ORDERED" | "PAID";
```
на:
```ts
export type OrderStatus = "CREATED" | "IN_CHECK" | "PENDING" | "CANCELED" | "ORDERED" | "PAID";
```

- [ ] **Step 2: Проверить компиляцию**

```bash
cd frontend && npx tsc --noEmit
```
Ожидание: нет ошибок.

---

### Task 2: Добавить функцию `confirmOrder` в API

**Files:**
- Modify: `frontend/src/api/orders.ts`

- [ ] **Step 1: Добавить импорт `OrderDto` (уже импортирован, проверить)**

В `frontend/src/api/orders.ts` убедиться, что `OrderDto` присутствует в импорте:
```ts
import type { OrderCreateDto, OrderDto, OrderItem } from "./types";
```

- [ ] **Step 2: Добавить функцию `confirmOrder` в конец файла**

```ts
export async function confirmOrder(orderId: string): Promise<OrderDto> {
  const { data } = await orderApi.post<OrderDto>(`/${orderId}/confirm`);
  return data;
}
```

- [ ] **Step 3: Проверить компиляцию**

```bash
cd frontend && npx tsc --noEmit
```
Ожидание: нет ошибок.

---

### Task 3: Добавить ключи локализации

**Files:**
- Modify: `frontend/src/locales/ru.ts`
- Modify: `frontend/src/locales/en.ts`

- [ ] **Step 1: Добавить ключ `IN_CHECK` в `orderStatus` в `ru.ts`**

В `frontend/src/locales/ru.ts` найти секцию `orderStatus` и добавить:
```ts
orderStatus: {
  CREATED: "Создан",
  IN_CHECK: "На проверке",   // ← добавить
  ORDERED: "Оформлен",
  PAID: "Оплачен",
  CANCELED: "Отменён",
},
```

- [ ] **Step 2: Добавить ключ `editingLocked` в `order` в `ru.ts`**

В `frontend/src/locales/ru.ts` найти секцию `order` и добавить:
```ts
order: {
  addItem: "Добавить товар",
  hint: "ссылка + размер + конфигурация",
  items: "Товары",
  noItems: "Товаров пока нет.",
  link: "ссылка",
  size: "размер",
  configuration: "конфигурация",
  price: "цена",
  created: "Создан",
  confirm: "Оформить заказ",        // ← добавить
  confirming: "Оформляем...",       // ← добавить
  editingLocked: "Изменение недоступно для текущего статуса заказа", // ← добавить
},
```

- [ ] **Step 3: Добавить ключ `IN_CHECK` в `orderStatus` в `en.ts`**

В `frontend/src/locales/en.ts` найти секцию `orderStatus` и добавить:
```ts
orderStatus: {
  CREATED: "Created",
  IN_CHECK: "In review",   // ← добавить
  ORDERED: "Ordered",
  PAID: "Paid",
  CANCELED: "Canceled",
},
```

- [ ] **Step 4: Добавить ключи `confirm`, `confirming`, `editingLocked` в `order` в `en.ts`**

В `frontend/src/locales/en.ts` найти секцию `order` и добавить:
```ts
order: {
  addItem: "Add item",
  hint: "link + size + configuration",
  items: "Items",
  noItems: "No items yet.",
  link: "link",
  size: "size",
  configuration: "configuration",
  price: "price",
  created: "Created",
  confirm: "Confirm order",                           // ← добавить
  confirming: "Confirming...",                        // ← добавить
  editingLocked: "Editing is not available for the current order status", // ← добавить
},
```

- [ ] **Step 5: Проверить компиляцию**

```bash
cd frontend && npx tsc --noEmit
```
Ожидание: нет ошибок.

---

### Task 4: Обновить `OrderDetailsPage` — флаг, баннер, кнопка, блокировка

**Files:**
- Modify: `frontend/src/pages/OrderDetailsPage.tsx`

- [ ] **Step 1: Добавить импорт `confirmOrder`**

В начале `frontend/src/pages/OrderDetailsPage.tsx` найти:
```ts
import { addOrderItem, deleteOrderItem, getOrderById } from "../api/orders";
```
Заменить на:
```ts
import { addOrderItem, confirmOrder, deleteOrderItem, getOrderById } from "../api/orders";
```

- [ ] **Step 2: Добавить стейт `confirming`**

После строки `const [saving, setSaving] = useState(false);` добавить:
```ts
const [confirming, setConfirming] = useState(false);
```

- [ ] **Step 3: Добавить флаг `isEditable`**

После объявления `const items = useMemo(...)` добавить:
```ts
const isEditable = order?.status === "CREATED";
```

- [ ] **Step 4: Добавить функцию `handleConfirm`**

После функции `reload` добавить:
```ts
async function handleConfirm() {
  if (!id) return;
  setConfirming(true);
  try {
    setPageError(null);
    await confirmOrder(id);
    await reload();
  } catch (e: any) {
    setPageError(e?.response?.data?.message ?? t("errors.loadOrderFail"));
  } finally {
    setConfirming(false);
  }
}
```

- [ ] **Step 5: Добавить кнопку "Оформить заказ" в шапку**

В шапке страницы найти кнопку "Обновить":
```tsx
<button
  onClick={reload}
  className="w-full sm:w-auto px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
             dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
>
  {loading ? t("common.loading") : t("common.refresh")}
</button>
```
Заменить на:
```tsx
<div className="flex gap-2 w-full sm:w-auto">
  {isEditable && items.length > 0 && (
    <button
      disabled={confirming}
      onClick={handleConfirm}
      className="w-full sm:w-auto px-4 py-2 rounded-xl text-sm font-semibold transition
                 bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50
                 dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
    >
      {confirming ? t("order.confirming") : t("order.confirm")}
    </button>
  )}
  <button
    onClick={reload}
    className="w-full sm:w-auto px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
               dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
  >
    {loading ? t("common.loading") : t("common.refresh")}
  </button>
</div>
```

- [ ] **Step 6: Добавить баннер блокировки**

После блока `{pageError && ...}` добавить:
```tsx
{!isEditable && order !== null && (
  <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800
                  dark:border-amber-900/50 dark:bg-amber-950/40 dark:text-amber-300">
    {t("order.editingLocked")}
  </div>
)}
```

- [ ] **Step 7: Заблокировать форму добавления товара**

В форме добавления товара найти все три `<FormField>` и добавить `disabled={!isEditable}`:
```tsx
<FormField
  label={labels.link}
  value={form.link}
  onChange={(v) => setField("link", v)}
  onBlur={() => touchField("link")}
  placeholder={LIMITS.link.placeholder}
  maxLength={LIMITS.link.max}
  error={shownError("link")}
  disabled={!isEditable}   // ← добавить
/>
```
```tsx
<FormField
  label={labels.size}
  value={form.size}
  onChange={(v) => setField("size", v)}
  onBlur={() => touchField("size")}
  placeholder={LIMITS.size.placeholder}
  maxLength={LIMITS.size.max}
  error={shownError("size")}
  disabled={!isEditable}   // ← добавить
/>
```
```tsx
<FormField
  label={labels.configuration}
  value={form.configuration}
  onChange={(v) => setField("configuration", v)}
  onBlur={() => touchField("configuration")}
  placeholder={LIMITS.configuration.placeholder}
  maxLength={LIMITS.configuration.max}
  error={shownError("configuration")}
  disabled={!isEditable}   // ← добавить
/>
```

- [ ] **Step 8: Заблокировать кнопку "Добавить товар"**

Найти кнопку добавления товара:
```tsx
<button
  disabled={!canSubmit}
  onClick={addItem}
  ...
>
```
Заменить атрибут `disabled`:
```tsx
<button
  disabled={!canSubmit || !isEditable}
  onClick={addItem}
  ...
>
```

- [ ] **Step 9: Заблокировать кнопки удаления товаров**

Найти кнопку удаления в списке товаров:
```tsx
<button
  disabled={!canOpen || isDeleting}
  onClick={() => { ... }}
  ...
>
```
Заменить атрибут `disabled`:
```tsx
<button
  disabled={!canOpen || isDeleting || !isEditable}
  onClick={() => { ... }}
  ...
>
```

- [ ] **Step 10: Проверить компиляцию**

```bash
cd frontend && npx tsc --noEmit
```
Ожидание: нет ошибок.

- [ ] **Step 11: Запустить dev-сервер и проверить вручную**

```bash
cd frontend && npm run dev
```

Сценарии проверки:
1. Открыть заказ со статусом `CREATED` с товарами — кнопка "Оформить заказ" видна, форма и кнопки удаления активны, баннера нет.
2. Открыть заказ со статусом `CREATED` без товаров — кнопка "Оформить заказ" скрыта.
3. Нажать "Оформить заказ" — статус меняется, баннер появляется, форма и кнопки удаления блокируются.
4. Открыть заказ с любым статусом кроме `CREATED` — баннер виден, форма заблокирована, кнопки удаления недоступны, "Открыть" товар работает.

- [ ] **Step 12: Закоммитить изменения**

```bash
git add frontend/src/api/types.ts \
        frontend/src/api/orders.ts \
        frontend/src/locales/ru.ts \
        frontend/src/locales/en.ts \
        frontend/src/pages/OrderDetailsPage.tsx
git commit -m "feat: подтверждение заказа — кнопка и блокировка редактирования"
```
