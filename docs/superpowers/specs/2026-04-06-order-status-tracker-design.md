# Order Status Tracker — Design Spec

**Date:** 2026-04-06  
**Branch:** feat-add-admin-profile  
**Status:** Approved

---

## Overview

Add a visual order status tracker to `OrderDetailsPage`. The tracker displays the progression of an order through its lifecycle as a horizontal row of icon circles connected by lines, each with a label below.

---

## Status Flow

```
CREATED → IN_CHECK → AWAITING_PAYMENT → PAID → DELIVERING → COMPLETED
```

Statuses outside this flow (`CANCELED`, `PENDING`, `ORDERED`) — tracker is not shown.

---

## Component

**New file:** `frontend/src/components/OrderStatusTracker.tsx`

**Props:**
```typescript
type Props = { status: OrderStatus }
```

**Step config** (static array, defined inside the component file):
```typescript
type Step = { status: OrderStatus; labelKey: string; icon: JSX.Element }
```

| Step | Status | labelKey | Icon |
|------|--------|----------|------|
| 1 | `CREATED` | `orderStatus.CREATED` | Plus |
| 2 | `IN_CHECK` | `orderStatus.IN_CHECK` | Magnifying glass |
| 3 | `AWAITING_PAYMENT` | `orderStatus.AWAITING_PAYMENT` | Wallet |
| 4 | `PAID` | `orderStatus.PAID` | Coin with checkmark |
| 5 | `DELIVERING` | `orderStatus.DELIVERING` | Truck |
| 6 | `COMPLETED` | `orderStatus.COMPLETED` | Checkmark |

Icons are inline SVG (no external library). Each step shows its own icon regardless of completion state.

**State logic** (determined by index):
- `stepIndex < currentIndex` → `done`
- `stepIndex === currentIndex` → `active`
- `stepIndex > currentIndex` → `pending`

If `status` is not in the 6-step flow → component returns `null`.

---

## Visual Style (Monochrome — Style A)

| State | Circle | Icon | Line |
|-------|--------|------|------|
| `done` | `bg-slate-900 border-slate-900` | white | `bg-slate-900` |
| `active` | `bg-white border-slate-900 ring-4 ring-slate-900/10` | slate-900 | `bg-slate-200` |
| `pending` | `bg-white border-slate-200` | slate-300 | `bg-slate-200` |

**Dark mode overrides:**
| State | Circle | Icon | Line |
|-------|--------|------|------|
| `done` | `dark:bg-white dark:border-white` | slate-900 | `dark:bg-white` |
| `active` | `dark:border-white dark:ring-white/10` | white | `dark:bg-slate-700` |
| `pending` | `dark:border-slate-700` | slate-600 | `dark:bg-slate-700` |

Circle size: 48×48px. Label: 11px, centered, max-width 76px.

---

## Placement in OrderDetailsPage

The tracker is rendered as a standalone card inserted between the page header and the status banner:

```
[Header: title + StatusBadge + action buttons]
[Tracker card]                                   ← new
[Banner: IN_CHECK / AWAITING_PAYMENT / locked]
[Add item form card]
[Items list card]
```

Card wrapper uses the same style as existing cards:
```
rounded-3xl border bg-white px-4 py-5 shadow-sm
dark:bg-slate-950 dark:border-slate-800
```

Render condition:
```tsx
{order && <div className="...card styles..."><OrderStatusTracker status={order.status} /></div>}
```

Since `OrderStatusTracker` returns `null` for unrecognized statuses, the card will render an empty wrapper in those cases. To avoid that, wrap with a check:
```tsx
{order && !["CANCELED", "PENDING", "ORDERED"].includes(order.status) && (
  <div className="..."><OrderStatusTracker status={order.status} /></div>
)}
```

---

## Responsiveness

- Container: `overflow-x: auto` — horizontal scroll inside the card only
- Each step: `min-width: 72px` — prevents collapse on narrow screens
- No vertical stacking on mobile — horizontal scroll is acceptable for 6 steps

---

## Localization

Uses existing `orderStatus.*` keys from `ru.ts` / `en.ts`. No new translation keys required.

---

## Out of Scope

- Admin panel (`AdminOrderDetailsPage`) — not included in this iteration
- Animated transitions between states
- Detailed delivery sub-statuses (noted as future possibility)
- `CANCELED` state visualization (not shown at all per decision)
