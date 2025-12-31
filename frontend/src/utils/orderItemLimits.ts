// src/utils/orderItemLimits.ts
export type OrderItemField = "link" | "size" | "configuration";

export const ORDER_ITEM_LIMITS: Record<OrderItemField, { min: number; max: number; labelKey: string }> = {
  link: { min: 2, max: 50, labelKey: "order.link" },
  size: { min: 2, max: 30, labelKey: "order.size" },
  configuration: { min: 2, max: 255, labelKey: "order.configuration" },
};
