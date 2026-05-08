# Admin Moderator Panel Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Создать панель модератора (ROLE_ADMIN) во frontend, где администратор может просматривать все заказы, выставлять цены товарам и отправлять заказы на оплату.

**Architecture:** Отдельный раздел `/admin/*` с `AdminRoute` (аналог `ProtectedRoute`), изолированный API слой (`api/adminOrders.ts` с собственным axios инстансом), два новых хука и две новые страницы. Навигация в `Layout` адаптируется по роли пользователя.

**Tech Stack:** React 19, TypeScript, React Router v7, Axios, Tailwind CSS 3, i18next (ru/en)

---

## Карта файлов

| Действие | Файл |
|----------|------|
| Modify | `frontend/src/api/types.ts` |
| Modify | `frontend/src/api/http.ts` |
| Create | `frontend/src/api/adminOrders.ts` |
| Create | `frontend/src/components/AdminRoute.tsx` |
| Modify | `frontend/src/components/Layout.tsx` |
| Modify | `frontend/src/components/StatusBadge.tsx` |
| Modify | `frontend/src/locales/ru.ts` |
| Modify | `frontend/src/locales/en.ts` |
| Create | `frontend/src/hooks/useAdminOrders.ts` |
| Create | `frontend/src/hooks/useAdminOrderDetails.ts` |
| Create | `frontend/src/pages/AdminOrdersPage.tsx` |
| Create | `frontend/src/pages/AdminOrderDetailsPage.tsx` |
| Modify | `frontend/src/App.tsx` |
| Modify | `frontend/src/pages/OrderDetailsPage.tsx` |

---

## Task 1: Расширить типы в api/types.ts

**Files:**
- Modify: `frontend/src/api/types.ts`

- [ ] **Step 1: Заменить содержимое файла**

Открыть `frontend/src/api/types.ts` и заменить на:

```typescript
export type UserDto = {
  id: string;
  email: string;
  role: "ROLE_ADMIN" | "ROLE_USER";
};

export type JwtAuthDto = {
  token: string;
  refreshToken: string;
};

export type AddressCreateDto = {
  firstname: string;
  lastname: string;
  patronymic: string;
  phone: string;
  city: string;
  street: string;
  building: string;
  postalCode: string;
};

export type AddressUpdateDto = Partial<AddressCreateDto>;

export type AddressDto = AddressCreateDto & {
  id: string;
  user: UserDto;
};

export type OrderStatus =
  | "CREATED"
  | "IN_CHECK"
  | "PENDING"
  | "CANCELED"
  | "ORDERED"
  | "PAID"
  | "AWAITING_PAYMENT"
  | "DELIVERING"
  | "COMPLETED";

export type OrderCreateDto = { name: string; addressId: string };
export type OrderUpdateDto = { name: string; addressId: string };

export type OrderItem = {
  id: string;
  link: string;
  size: string;
  configuration: string;
  price?: number;
};

export type OrderDto = {
  id: string;
  name: string;
  userId: string;
  addressId: string;
  status: OrderStatus;
  createdDate: string;
  trackingNumber?: string;
  items?: OrderItem[];
};

// Admin types
export type OrderShortDto = {
  id: string;
  name: string;
  userId: string;
  addressId: string;
  status: OrderStatus;
  createdDate: string;
  trackingNumber?: string;
};

export type OrderItemDto = {
  id: string;
  orderId: string;
  link: string;
  size: string;
  configuration: string;
  price?: number;
};

export type OrderFullDto = {
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
```

- [ ] **Step 2: Проверить компиляцию**

```bash
cd frontend && npx tsc --noEmit
```

Ожидаемый результат: нет ошибок (или только существующие, не связанные с types.ts)

- [ ] **Step 3: Коммит**

```bash
git add frontend/src/api/types.ts
git commit -m "feat: расширить типы — role в UserDto, новые статусы, admin DTO"
```

---

## Task 2: Добавить adminApi в api/http.ts

**Files:**
- Modify: `frontend/src/api/http.ts`

- [ ] **Step 1: Добавить adminApi инстанс и подключить к interceptors**

Открыть `frontend/src/api/http.ts`. Внести изменения:

1. После строки `const authBaseURL = ...` добавить:
```typescript
const adminBaseURL = import.meta.env.VITE_ADMIN_API_URL ?? "/api/admin";
```

2. После строки `export const authApi = axios.create(...)` добавить:
```typescript
export const adminApi = axios.create({ baseURL: adminBaseURL, withCredentials: true });
```

3. Заменить строку:
```typescript
[userApi, orderApi, authApi].forEach((api) => api.interceptors.request.use(attachAuth));
```
на:
```typescript
[userApi, orderApi, authApi, adminApi].forEach((api) => api.interceptors.request.use(attachAuth));
```

4. В функции `pickInstance` добавить ветку для admin перед `return userApi`:
```typescript
function pickInstance(cfg: any) {
  if (cfg?.baseURL === orderBaseURL) return orderApi;
  if (cfg?.baseURL === authBaseURL) return authApi;
  if (cfg?.baseURL === adminBaseURL) return adminApi;
  return userApi;
}
```

5. Заменить строку:
```typescript
[userApi, orderApi, authApi].forEach((api) => {
```
на:
```typescript
[userApi, orderApi, authApi, adminApi].forEach((api) => {
```

- [ ] **Step 2: Проверить компиляцию**

```bash
cd frontend && npx tsc --noEmit
```

Ожидаемый результат: нет новых ошибок

- [ ] **Step 3: Коммит**

```bash
git add frontend/src/api/http.ts
git commit -m "feat: добавить adminApi axios инстанс"
```

---

## Task 3: Создать api/adminOrders.ts

**Files:**
- Create: `frontend/src/api/adminOrders.ts`

- [ ] **Step 1: Создать файл**

Создать `frontend/src/api/adminOrders.ts` со следующим содержимым:

```typescript
import { adminApi } from "./http";
import type { OrderFullDto, OrderItemDto, OrderShortDto, OrderStatus } from "./types";

export type Paged<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
};

export async function listAdminOrders(
  page = 0,
  size = 10,
  status?: OrderStatus
): Promise<Paged<OrderShortDto>> {
  const params: Record<string, unknown> = { page, size };
  if (status) params.status = status;
  const { data } = await adminApi.get<Paged<OrderShortDto>>("/orders", { params });
  return data;
}

export async function getAdminOrder(orderId: string): Promise<OrderFullDto> {
  const { data } = await adminApi.get<OrderFullDto>(`/orders/${orderId}`);
  return data;
}

export async function setItemPrice(
  orderId: string,
  itemId: string,
  price: number
): Promise<OrderItemDto> {
  const { data } = await adminApi.patch<OrderItemDto>(
    `/orders/${orderId}/items/${itemId}`,
    { price }
  );
  return data;
}

export async function submitOrder(orderId: string): Promise<OrderFullDto> {
  const { data } = await adminApi.patch<OrderFullDto>(`/orders/${orderId}/submit`);
  return data;
}
```

- [ ] **Step 2: Проверить компиляцию**

```bash
cd frontend && npx tsc --noEmit
```

Ожидаемый результат: нет ошибок

- [ ] **Step 3: Коммит**

```bash
git add frontend/src/api/adminOrders.ts
git commit -m "feat: admin API функции (listAdminOrders, getAdminOrder, setItemPrice, submitOrder)"
```

---

## Task 4: Создать компонент AdminRoute

**Files:**
- Create: `frontend/src/components/AdminRoute.tsx`

- [ ] **Step 1: Создать файл**

Создать `frontend/src/components/AdminRoute.tsx`:

```typescript
import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export function AdminRoute({ children }: { children: React.ReactNode }) {
  const { user, isLoading } = useAuth();
  if (isLoading) return <div style={{ padding: 16 }}>Loading...</div>;
  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== "ROLE_ADMIN") return <Navigate to="/" replace />;
  return <>{children}</>;
}
```

- [ ] **Step 2: Проверить компиляцию**

```bash
cd frontend && npx tsc --noEmit
```

Ожидаемый результат: нет ошибок

- [ ] **Step 3: Коммит**

```bash
git add frontend/src/components/AdminRoute.tsx
git commit -m "feat: AdminRoute — защищённый маршрут для ROLE_ADMIN"
```

---

## Task 5: Обновить навигацию в Layout.tsx

**Files:**
- Modify: `frontend/src/components/Layout.tsx`

- [ ] **Step 1: Адаптировать nav блок по роли**

В `frontend/src/components/Layout.tsx` найти блок `{user && !isAuthPage ? (` и заменить его содержимое:

```tsx
{user && !isAuthPage ? (
  <>
    {user.role === "ROLE_ADMIN" ? (
      <NavLink to="/admin" className={linkClass}>
        {t("nav.adminPanel")}
      </NavLink>
    ) : (
      <>
        <NavLink to="/" className={linkClass} end>
          {t("nav.orders")}
        </NavLink>
        <NavLink to="/addresses" className={linkClass}>
          {t("nav.addresses")}
        </NavLink>
        <NavLink to="/profile" className={linkClass}>
          {t("nav.profile")}
        </NavLink>
      </>
    )}
  </>
) : (
  <>
    <NavLink to="/login" className={linkClass}>
      {t("nav.login")}
    </NavLink>
    <NavLink to="/register" className={linkClass}>
      {t("nav.register")}
    </NavLink>
  </>
)}
```

- [ ] **Step 2: Проверить компиляцию**

```bash
cd frontend && npx tsc --noEmit
```

Ожидаемый результат: нет ошибок

- [ ] **Step 3: Коммит**

```bash
git add frontend/src/components/Layout.tsx
git commit -m "feat: навигация адаптируется по роли пользователя"
```

---

## Task 6: Добавить цвета новых статусов в StatusBadge.tsx

**Files:**
- Modify: `frontend/src/components/StatusBadge.tsx`

- [ ] **Step 1: Добавить стили для новых статусов**

В `frontend/src/components/StatusBadge.tsx` в объект `STATUS_STYLES` добавить три новые записи:

```typescript
const STATUS_STYLES: Record<string, string> = {
  PAID: "border-emerald-200 bg-emerald-50 text-emerald-800 dark:border-emerald-900/40 dark:bg-emerald-950/40 dark:text-emerald-200",
  CANCELED: "border-red-200 bg-red-50 text-red-700 dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200",
  ORDERED: "border-indigo-200 bg-indigo-50 text-indigo-800 dark:border-indigo-900/40 dark:bg-indigo-950/40 dark:text-indigo-200",
  IN_CHECK: "border-slate-200 bg-slate-50 text-slate-800 dark:border-slate-800 dark:bg-slate-900/40 dark:text-slate-100",
  AWAITING_PAYMENT: "border-amber-200 bg-amber-50 text-amber-800 dark:border-amber-900/40 dark:bg-amber-950/40 dark:text-amber-200",
  DELIVERING: "border-blue-200 bg-blue-50 text-blue-800 dark:border-blue-900/40 dark:bg-blue-950/40 dark:text-blue-200",
  COMPLETED: "border-emerald-200 bg-emerald-50 text-emerald-800 dark:border-emerald-900/40 dark:bg-emerald-950/40 dark:text-emerald-200",
};
```

- [ ] **Step 2: Проверить компиляцию**

```bash
cd frontend && npx tsc --noEmit
```

- [ ] **Step 3: Коммит**

```bash
git add frontend/src/components/StatusBadge.tsx
git commit -m "feat: добавить цвета для статусов AWAITING_PAYMENT, DELIVERING, COMPLETED"
```

---

## Task 7: Обновить локализацию (ru.ts и en.ts)

**Files:**
- Modify: `frontend/src/locales/ru.ts`
- Modify: `frontend/src/locales/en.ts`

- [ ] **Step 1: Обновить ru.ts**

В `frontend/src/locales/ru.ts`:

1. В секции `nav` добавить после `logout`:
```typescript
adminPanel: "Панель модератора",
```

2. Секцию `orderStatus` заменить на:
```typescript
orderStatus: {
  PENDING: "Ожидает",
  CREATED: "Создан",
  IN_CHECK: "В обработке",
  ORDERED: "Оформлен",
  PAID: "Оплачен",
  CANCELED: "Отменён",
  AWAITING_PAYMENT: "Ожидает оплаты",
  DELIVERING: "Доставляется",
  COMPLETED: "Завершён",
},
```

3. В секции `order` добавить после `confirmDialogDescription`:
```typescript
awaitingPaymentBanner: "Ваш заказ готов к оплате. Пожалуйста, оплатите заказ.",
```

4. После секции `profile` добавить новую секцию `admin`:
```typescript
admin: {
  title: "Панель модератора",
  filterByStatus: "Фильтр по статусу",
  allStatuses: "Все статусы",
  userId: "ID пользователя",
  noOrders: "Нет заказов с выбранным статусом.",
  totalPrice: "Итого",
  setPrice: "Цена",
  savePrice: "Сохранить",
  savingPrice: "Сохраняем...",
  submitOrder: "Выставить на оплату",
  submitting: "Выставляем...",
  submitDialogTitle: "Выставить заказ на оплату?",
  submitDialogDescription: "Статус заказа изменится на «Ожидает оплаты». Пользователь получит уведомление для оплаты.",
  allPricesRequired: "Выставьте цены всем товарам перед отправкой на оплату.",
},
```

5. В секции `errors` добавить после `resendTooEarly`:
```typescript
setPriceFail: "Не удалось выставить цену",
submitOrderFail: "Не удалось выставить заказ на оплату",
loadAdminOrdersFail: "Не удалось загрузить заказы",
loadAdminOrderFail: "Не удалось загрузить заказ",
```

- [ ] **Step 2: Обновить en.ts**

В `frontend/src/locales/en.ts` внести аналогичные изменения:

1. В секции `nav` добавить:
```typescript
adminPanel: "Moderator Panel",
```

2. Секцию `orderStatus` заменить на:
```typescript
orderStatus: {
  PENDING: "Pending",
  CREATED: "Created",
  IN_CHECK: "In progress",
  ORDERED: "Ordered",
  PAID: "Paid",
  CANCELED: "Canceled",
  AWAITING_PAYMENT: "Awaiting payment",
  DELIVERING: "Delivering",
  COMPLETED: "Completed",
},
```

3. В секции `order` добавить:
```typescript
awaitingPaymentBanner: "Your order is ready for payment. Please proceed to pay.",
```

4. Добавить секцию `admin`:
```typescript
admin: {
  title: "Moderator Panel",
  filterByStatus: "Filter by status",
  allStatuses: "All statuses",
  userId: "User ID",
  noOrders: "No orders with the selected status.",
  totalPrice: "Total",
  setPrice: "Price",
  savePrice: "Save",
  savingPrice: "Saving...",
  submitOrder: "Submit for payment",
  submitting: "Submitting...",
  submitDialogTitle: "Submit order for payment?",
  submitDialogDescription: "The order status will change to \"Awaiting payment\". The user will be notified to pay.",
  allPricesRequired: "Set prices for all items before submitting for payment.",
},
```

5. В секции `errors` добавить:
```typescript
setPriceFail: "Failed to set price",
submitOrderFail: "Failed to submit order for payment",
loadAdminOrdersFail: "Failed to load orders",
loadAdminOrderFail: "Failed to load order",
```

- [ ] **Step 3: Проверить компиляцию**

```bash
cd frontend && npx tsc --noEmit
```

Ожидаемый результат: нет ошибок

- [ ] **Step 4: Коммит**

```bash
git add frontend/src/locales/ru.ts frontend/src/locales/en.ts
git commit -m "feat: локализация — admin панель, новые статусы"
```

---

## Task 8: Создать хук useAdminOrders

**Files:**
- Create: `frontend/src/hooks/useAdminOrders.ts`

- [ ] **Step 1: Создать файл**

Создать `frontend/src/hooks/useAdminOrders.ts`:

```typescript
import { useCallback, useEffect, useState } from "react";
import type { TFunction } from "i18next";
import { listAdminOrders } from "../api/adminOrders";
import type { OrderShortDto, OrderStatus } from "../api/types";

const PAGE_SIZE = 10;

export function useAdminOrders(t: TFunction) {
  const [orders, setOrders] = useState<OrderShortDto[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [statusFilter, setStatusFilter] = useState<OrderStatus | "">( "IN_CHECK");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const reload = useCallback(
    async (p: number, status: OrderStatus | "") => {
      setLoading(true);
      try {
        setError(null);
        const data = await listAdminOrders(p, PAGE_SIZE, status || undefined);
        setOrders(data.content);
        setTotalPages(Math.max(1, data.totalPages));
      } catch (e: any) {
        setError(e?.response?.data?.message ?? t("errors.loadAdminOrdersFail"));
      } finally {
        setLoading(false);
      }
    },
    [t]
  );

  useEffect(() => {
    reload(page, statusFilter);
  }, [page, statusFilter, reload]);

  function changePage(p: number) {
    setPage(p);
  }

  function changeStatus(s: OrderStatus | "") {
    setPage(0);
    setStatusFilter(s);
  }

  return {
    orders,
    page,
    totalPages,
    statusFilter,
    loading,
    error,
    changePage,
    changeStatus,
    reload: () => reload(page, statusFilter),
  };
}
```

- [ ] **Step 2: Проверить компиляцию**

```bash
cd frontend && npx tsc --noEmit
```

Ожидаемый результат: нет ошибок

- [ ] **Step 3: Коммит**

```bash
git add frontend/src/hooks/useAdminOrders.ts
git commit -m "feat: хук useAdminOrders — список заказов для модератора"
```

---

## Task 9: Создать хук useAdminOrderDetails

**Files:**
- Create: `frontend/src/hooks/useAdminOrderDetails.ts`

- [ ] **Step 1: Создать файл**

Создать `frontend/src/hooks/useAdminOrderDetails.ts`:

```typescript
import { useCallback, useEffect, useState } from "react";
import type { TFunction } from "i18next";
import { getAdminOrder, setItemPrice, submitOrder } from "../api/adminOrders";
import type { OrderFullDto } from "../api/types";

export function useAdminOrderDetails(orderId: string | undefined, t: TFunction) {
  const [order, setOrder] = useState<OrderFullDto | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // per-item price input values (itemId → string)
  const [priceInputs, setPriceInputs] = useState<Record<string, string>>({});
  const [savingPriceId, setSavingPriceId] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const reload = useCallback(async () => {
    if (!orderId) return;
    setLoading(true);
    try {
      setError(null);
      const data = await getAdminOrder(orderId);
      setOrder(data);
      // initialise price inputs from loaded data (show existing prices)
      const inputs: Record<string, string> = {};
      data.items.forEach((item) => {
        if (item.price != null) inputs[item.id] = String(item.price);
      });
      setPriceInputs((prev) => ({ ...inputs, ...prev }));
    } catch (e: any) {
      setError(e?.response?.data?.message ?? t("errors.loadAdminOrderFail"));
    } finally {
      setLoading(false);
    }
  }, [orderId, t]);

  useEffect(() => {
    reload();
  }, [reload]);

  function setPriceInput(itemId: string, value: string) {
    setPriceInputs((prev) => ({ ...prev, [itemId]: value }));
  }

  async function saveItemPrice(itemId: string): Promise<void> {
    if (!orderId) return;
    const raw = priceInputs[itemId] ?? "";
    const price = parseFloat(raw);
    if (!raw.trim() || isNaN(price) || price <= 0) return;

    setSavingPriceId(itemId);
    try {
      setError(null);
      const updated = await setItemPrice(orderId, itemId, price);
      // update item price in local order state without full reload
      setOrder((prev) => {
        if (!prev) return prev;
        return {
          ...prev,
          items: prev.items.map((item) =>
            item.id === itemId ? { ...item, price: updated.price } : item
          ),
        };
      });
    } catch (e: any) {
      setError(e?.response?.data?.message ?? t("errors.setPriceFail"));
    } finally {
      setSavingPriceId(null);
    }
  }

  async function handleSubmit(): Promise<void> {
    if (!orderId) return;
    setSubmitting(true);
    try {
      setError(null);
      const updated = await submitOrder(orderId);
      setOrder(updated);
    } catch (e: any) {
      setError(e?.response?.data?.message ?? t("errors.submitOrderFail"));
    } finally {
      setSubmitting(false);
    }
  }

  const allPricesSet =
    (order?.items.length ?? 0) > 0 &&
    (order?.items.every((item) => item.price != null && item.price > 0) ?? false);

  const canSubmit =
    order?.status === "IN_CHECK" && allPricesSet && !submitting;

  return {
    order,
    loading,
    error,
    setError,
    priceInputs,
    setPriceInput,
    savingPriceId,
    saveItemPrice,
    submitting,
    handleSubmit,
    canSubmit,
    allPricesSet,
    reload,
  };
}
```

- [ ] **Step 2: Проверить компиляцию**

```bash
cd frontend && npx tsc --noEmit
```

Ожидаемый результат: нет ошибок

- [ ] **Step 3: Коммит**

```bash
git add frontend/src/hooks/useAdminOrderDetails.ts
git commit -m "feat: хук useAdminOrderDetails — выставление цен и submit заказа"
```

---

## Task 10: Создать страницу AdminOrdersPage

**Files:**
- Create: `frontend/src/pages/AdminOrdersPage.tsx`

- [ ] **Step 1: Создать файл**

Создать `frontend/src/pages/AdminOrdersPage.tsx`:

```tsx
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import { useAdminOrders } from "../hooks/useAdminOrders";
import StatusBadge from "../components/StatusBadge";
import type { OrderStatus } from "../api/types";

const STATUS_OPTIONS: Array<{ value: OrderStatus | ""; labelKey: string }> = [
  { value: "", labelKey: "admin.allStatuses" },
  { value: "IN_CHECK", labelKey: "orderStatus.IN_CHECK" },
  { value: "AWAITING_PAYMENT", labelKey: "orderStatus.AWAITING_PAYMENT" },
  { value: "CREATED", labelKey: "orderStatus.CREATED" },
  { value: "PAID", labelKey: "orderStatus.PAID" },
  { value: "CANCELED", labelKey: "orderStatus.CANCELED" },
  { value: "DELIVERING", labelKey: "orderStatus.DELIVERING" },
  { value: "COMPLETED", labelKey: "orderStatus.COMPLETED" },
];

export function AdminOrdersPage() {
  const { t, i18n } = useTranslation();
  const {
    orders,
    page,
    totalPages,
    statusFilter,
    loading,
    error,
    changePage,
    changeStatus,
    reload,
  } = useAdminOrders(t);

  function formatDate(dateStr: string): string {
    try {
      return new Intl.DateTimeFormat(i18n.language, {
        day: "numeric",
        month: "long",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      }).format(new Date(dateStr));
    } catch {
      return dateStr;
    }
  }

  return (
    <div className="space-y-6">
      {/* header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <h1 className="text-xl sm:text-2xl font-semibold">{t("admin.title")}</h1>
        </div>
        <div className="flex items-center gap-2">
          {/* status filter */}
          <select
            value={statusFilter}
            onChange={(e) => changeStatus(e.target.value as OrderStatus | "")}
            className="px-3 py-2 rounded-xl text-sm border bg-white dark:bg-slate-950 dark:border-slate-800 dark:text-slate-100"
          >
            {STATUS_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {t(opt.labelKey as any)}
              </option>
            ))}
          </select>

          <button
            disabled={loading}
            onClick={reload}
            className="px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                       disabled:opacity-50
                       dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
          >
            {loading ? t("common.loading") : t("common.refresh")}
          </button>
        </div>
      </div>

      {error && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                        dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200">
          {error}
        </div>
      )}

      {/* orders table */}
      <div className="rounded-3xl border bg-white overflow-hidden dark:bg-slate-950 dark:border-slate-800">
        {orders.length === 0 && !loading ? (
          <div className="p-6 text-sm text-slate-500 dark:text-slate-400">
            {t("admin.noOrders")}
          </div>
        ) : (
          <ul className="divide-y dark:divide-slate-800">
            {orders.map((order) => {
              const statusLabel = t(`orderStatus.${order.status}` as any, {
                defaultValue: order.status,
              });
              return (
                <li
                  key={order.id}
                  className="p-4 sm:p-6 hover:bg-slate-50 transition dark:hover:bg-slate-900/40"
                >
                  <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                    <div className="min-w-0 space-y-1">
                      <div className="text-sm font-semibold break-words">{order.name}</div>
                      <div className="text-xs text-slate-500 dark:text-slate-400 break-all">
                        {t("admin.userId")}: {order.userId}
                      </div>
                      <div className="flex items-center gap-2 flex-wrap">
                        <StatusBadge status={order.status} label={statusLabel} />
                        <span className="text-xs text-slate-400 dark:text-slate-500">
                          {formatDate(order.createdDate)}
                        </span>
                      </div>
                    </div>
                    <Link
                      to={`/admin/orders/${order.id}`}
                      className="shrink-0 w-full sm:w-auto text-center px-4 py-2 rounded-xl text-sm font-medium border
                                 bg-white hover:bg-slate-50 transition
                                 dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
                    >
                      {t("common.open")}
                    </Link>
                  </div>
                </li>
              );
            })}
          </ul>
        )}
      </div>

      {/* pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <button
            disabled={page === 0 || loading}
            onClick={() => changePage(page - 1)}
            className="px-3 py-2 rounded-xl text-sm border bg-white hover:bg-slate-50 transition
                       disabled:opacity-50
                       dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
          >
            {t("common.back")}
          </button>
          <span className="text-sm text-slate-500 dark:text-slate-400">
            {page + 1} / {totalPages}
          </span>
          <button
            disabled={page >= totalPages - 1 || loading}
            onClick={() => changePage(page + 1)}
            className="px-3 py-2 rounded-xl text-sm border bg-white hover:bg-slate-50 transition
                       disabled:opacity-50
                       dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
          >
            {t("common.next")}
          </button>
        </div>
      )}
    </div>
  );
}

export default AdminOrdersPage;
```

- [ ] **Step 2: Проверить компиляцию**

```bash
cd frontend && npx tsc --noEmit
```

Ожидаемый результат: нет ошибок

- [ ] **Step 3: Коммит**

```bash
git add frontend/src/pages/AdminOrdersPage.tsx
git commit -m "feat: страница AdminOrdersPage — список всех заказов"
```

---

## Task 11: Создать страницу AdminOrderDetailsPage

**Files:**
- Create: `frontend/src/pages/AdminOrderDetailsPage.tsx`

- [ ] **Step 1: Создать файл**

Создать `frontend/src/pages/AdminOrderDetailsPage.tsx`:

```tsx
import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAdminOrderDetails } from "../hooks/useAdminOrderDetails";
import StatusBadge from "../components/StatusBadge";
import ConfirmDialog from "../components/ConfirmDialog";

export function AdminOrderDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const { t, i18n } = useTranslation();
  const {
    order,
    loading,
    error,
    setError,
    priceInputs,
    setPriceInput,
    savingPriceId,
    saveItemPrice,
    submitting,
    handleSubmit,
    canSubmit,
    allPricesSet,
    reload,
  } = useAdminOrderDetails(id, t);

  const [confirmSubmitOpen, setConfirmSubmitOpen] = useState(false);

  function formatDate(dateStr: string): string {
    try {
      return new Intl.DateTimeFormat(i18n.language, {
        day: "numeric",
        month: "long",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      }).format(new Date(dateStr));
    } catch {
      return dateStr;
    }
  }

  if (!id) return null;

  const title = order ? (order.name?.trim() || id) : "";
  const status = order?.status ?? "";
  const statusLabel = status ? t(`orderStatus.${status}` as any, { defaultValue: status }) : "";
  const isInCheck = order?.status === "IN_CHECK";

  return (
    <div className="space-y-6">
      {/* breadcrumb + header */}
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
        <div className="min-w-0">
          <div className="text-sm text-slate-500 dark:text-slate-400">
            <Link to="/admin" className="hover:underline">
              {t("admin.title")}
            </Link>
            <span className="mx-1">/</span>
            <span className="break-words">{title}</span>
          </div>

          <div className="mt-2 flex flex-wrap items-center gap-3">
            <h1 className="text-xl sm:text-2xl font-semibold break-words">{title}</h1>
            {status && <StatusBadge status={status} label={statusLabel} />}
          </div>

          {order?.createdDate && (
            <div className="mt-1 text-sm text-slate-500 dark:text-slate-400">
              {formatDate(order.createdDate)}
            </div>
          )}

          {order?.totalPrice != null && (
            <div className="mt-1 text-sm font-semibold">
              {t("admin.totalPrice")}: {order.totalPrice}
            </div>
          )}

          <div className="mt-1 text-xs text-slate-500 dark:text-slate-400 break-all">
            {t("admin.userId")}: {order?.userId}
          </div>
        </div>

        <div className="flex gap-2 shrink-0">
          {isInCheck && (
            <button
              disabled={!canSubmit}
              onClick={() => {
                if (!allPricesSet) {
                  setError(t("admin.allPricesRequired"));
                  return;
                }
                setConfirmSubmitOpen(true);
              }}
              className="w-full sm:w-auto px-6 py-3 rounded-xl text-base font-semibold transition
                         bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50
                         dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
            >
              {submitting ? t("admin.submitting") : t("admin.submitOrder")}
            </button>
          )}
          <button
            disabled={loading || submitting}
            onClick={reload}
            className="w-full sm:w-auto px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                       disabled:opacity-50
                       dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
          >
            {loading ? t("common.loading") : t("common.refresh")}
          </button>
        </div>
      </div>

      {error && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                        dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200 break-words">
          {error}
        </div>
      )}

      {/* items */}
      <div className="rounded-3xl border bg-white overflow-hidden dark:bg-slate-950 dark:border-slate-800">
        <div className="px-4 sm:px-6 py-4 border-b dark:border-slate-800">
          <div className="text-sm font-semibold">{t("order.items")}</div>
        </div>

        {!order || order.items.length === 0 ? (
          <div className="p-6 text-sm text-slate-500 dark:text-slate-400">
            {t("order.noItems")}
          </div>
        ) : (
          <ul className="divide-y dark:divide-slate-800">
            {order.items.map((item) => {
              const isSaving = savingPriceId === item.id;
              const inputVal = priceInputs[item.id] ?? (item.price != null ? String(item.price) : "");
              const parsedPrice = parseFloat(inputVal);
              const priceValid = inputVal.trim() !== "" && !isNaN(parsedPrice) && parsedPrice > 0;

              return (
                <li
                  key={item.id}
                  className="p-4 sm:p-6"
                >
                  <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
                    <div className="min-w-0 space-y-1">
                      <div className="text-sm font-semibold break-all">{item.link}</div>
                      <div className="text-xs text-slate-500 dark:text-slate-400 flex flex-wrap gap-x-3 gap-y-1">
                        <span>
                          {t("order.size")}: <span className="font-medium">{item.size}</span>
                        </span>
                        <span>
                          {t("order.configuration")}:{" "}
                          <span className="font-medium">{item.configuration}</span>
                        </span>
                        {item.price != null && (
                          <span>
                            {t("order.price")}: <span className="font-medium text-emerald-700 dark:text-emerald-400">{item.price}</span>
                          </span>
                        )}
                      </div>
                    </div>

                    {isInCheck && (
                      <div className="flex items-center gap-2 shrink-0">
                        <input
                          type="number"
                          min="0.01"
                          step="0.01"
                          placeholder={t("admin.setPrice")}
                          value={inputVal}
                          onChange={(e) => setPriceInput(item.id, e.target.value)}
                          className="w-28 px-3 py-2 rounded-xl text-sm border bg-white dark:bg-slate-950 dark:border-slate-800 dark:text-slate-100
                                     focus:outline-none focus:ring-2 focus:ring-slate-400"
                        />
                        <button
                          disabled={!priceValid || isSaving}
                          onClick={() => saveItemPrice(item.id)}
                          className="px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                                     disabled:opacity-50
                                     dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
                        >
                          {isSaving ? t("admin.savingPrice") : t("admin.savePrice")}
                        </button>
                      </div>
                    )}
                  </div>
                </li>
              );
            })}
          </ul>
        )}
      </div>

      <ConfirmDialog
        open={confirmSubmitOpen}
        title={t("admin.submitDialogTitle")}
        description={t("admin.submitDialogDescription")}
        confirmText={t("admin.submitOrder")}
        cancelText={t("common.cancel")}
        loading={submitting}
        onClose={() => setConfirmSubmitOpen(false)}
        onConfirm={async () => {
          setConfirmSubmitOpen(false);
          await handleSubmit();
        }}
      />
    </div>
  );
}

export default AdminOrderDetailsPage;
```

- [ ] **Step 2: Проверить компиляцию**

```bash
cd frontend && npx tsc --noEmit
```

Ожидаемый результат: нет ошибок

- [ ] **Step 3: Коммит**

```bash
git add frontend/src/pages/AdminOrderDetailsPage.tsx
git commit -m "feat: страница AdminOrderDetailsPage — выставление цен и submit заказа"
```

---

## Task 12: Зарегистрировать admin маршруты в App.tsx

**Files:**
- Modify: `frontend/src/App.tsx`

- [ ] **Step 1: Добавить импорты и маршруты**

В `frontend/src/App.tsx`:

1. Добавить импорты после существующих:
```typescript
import { AdminRoute } from "./components/AdminRoute";
import { AdminOrdersPage } from "./pages/AdminOrdersPage";
import { AdminOrderDetailsPage } from "./pages/AdminOrderDetailsPage";
```

2. Добавить маршруты перед `<Route path="/login" ...>`:
```tsx
<Route
  path="/admin"
  element={
    <AdminRoute>
      <AdminOrdersPage />
    </AdminRoute>
  }
/>

<Route
  path="/admin/orders/:id"
  element={
    <AdminRoute>
      <AdminOrderDetailsPage />
    </AdminRoute>
  }
/>
```

- [ ] **Step 2: Проверить компиляцию**

```bash
cd frontend && npx tsc --noEmit
```

Ожидаемый результат: нет ошибок

- [ ] **Step 3: Коммит**

```bash
git add frontend/src/App.tsx
git commit -m "feat: зарегистрировать admin маршруты /admin и /admin/orders/:id"
```

---

## Task 13: Добавить баннер AWAITING_PAYMENT в OrderDetailsPage

**Files:**
- Modify: `frontend/src/pages/OrderDetailsPage.tsx`

- [ ] **Step 1: Добавить баннер для статуса AWAITING_PAYMENT**

В `frontend/src/pages/OrderDetailsPage.tsx` найти блок баннера:

```tsx
{!isEditable && order !== null && (
  <div className="rounded-3xl border bg-white px-4 py-3 text-sm shadow-sm text-slate-600
                  dark:bg-slate-950 dark:border-slate-800 dark:text-slate-400">
    {status === "IN_CHECK" ? t("order.inCheckBanner") : t("order.editingLocked")}
  </div>
)}
```

Заменить на:

```tsx
{!isEditable && order !== null && (
  <div className="rounded-3xl border bg-white px-4 py-3 text-sm shadow-sm text-slate-600
                  dark:bg-slate-950 dark:border-slate-800 dark:text-slate-400">
    {status === "IN_CHECK"
      ? t("order.inCheckBanner")
      : status === "AWAITING_PAYMENT"
      ? t("order.awaitingPaymentBanner")
      : t("order.editingLocked")}
  </div>
)}
```

- [ ] **Step 2: Проверить финальную компиляцию**

```bash
cd frontend && npm run build
```

Ожидаемый результат: build завершается без ошибок

- [ ] **Step 3: Коммит**

```bash
git add frontend/src/pages/OrderDetailsPage.tsx
git commit -m "feat: баннер AWAITING_PAYMENT в странице заказа пользователя"
```

---

## Проверка покрытия спецификации

| Требование из спеца | Задача |
|---------------------|--------|
| role в UserDto | Task 1 |
| adminApi инстанс с interceptors | Task 2 |
| listAdminOrders, getAdminOrder, setItemPrice, submitOrder | Task 3 |
| AdminRoute (редирект ROLE_USER → /) | Task 4 |
| Навигация: admin видит только "Панель модератора" | Task 5 |
| StatusBadge для AWAITING_PAYMENT, DELIVERING, COMPLETED | Task 6 |
| Локализация всех новых ключей | Task 7 |
| useAdminOrders (список + пагинация + фильтр) | Task 8 |
| useAdminOrderDetails (цены + submit) | Task 9 |
| AdminOrdersPage (таблица, фильтр, пагинация) | Task 10 |
| AdminOrderDetailsPage (inline price, submit button) | Task 11 |
| Маршруты /admin и /admin/orders/:id | Task 12 |
| Баннер AWAITING_PAYMENT для пользователя | Task 13 |
