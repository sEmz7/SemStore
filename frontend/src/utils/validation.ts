// src/utils/validation.ts
import type { TFunction } from "i18next";
import type { OrderItemField } from "./orderItemLimits";
import { ORDER_ITEM_LIMITS } from "./orderItemLimits";

export type SpringRangeError = {
  field?: string;
  min: number;
  max: number;
};


export function parseSpringValidationMessage(msg: string): SpringRangeError | null {
  if (!msg) return null;

  const field = msg.match(/on field '([^']+)'/i)?.[1];

  // RU: "от 2 до 50"
  const ru = msg.match(/от\s+(\d+)\s+до\s+(\d+)/i);
  if (ru) return { field, min: Number(ru[1]), max: Number(ru[2]) };

  // EN: "between 2 and 50"
  const en = msg.match(/between\s+(\d+)\s+and\s+(\d+)/i);
  if (en) return { field, min: Number(en[1]), max: Number(en[2]) };

  // Иногда: "[...,50,2]"
  const nums = msg.match(/,(\d+),(\d+)\]/);
  if (nums) {
    const max = Number(nums[1]);
    const min = Number(nums[2]);
    if (Number.isFinite(min) && Number.isFinite(max)) return { field, min, max };
  }

  return null;
}


export function validateRequiredLen(
  value: string,
  t: TFunction,
  opt: { label: string; min: number; max: number }
): string | null {
  const v = (value ?? "").trim();
  if (!v) return t("errors.required");

  if (v.length < opt.min || v.length > opt.max) {
    return t("errors.fieldLengthBetween", { field: opt.label, min: opt.min, max: opt.max });
  }
  return null;
}


export function validateOrderItemField(key: OrderItemField, value: string, t: TFunction): string | null {
  const lim = ORDER_ITEM_LIMITS[key];
  return validateRequiredLen(value, t, { label: t(lim.labelKey), min: lim.min, max: lim.max });
}


export function localizeOrderItemLengthErrorFromBackend(msg: string, t: TFunction): string | null {
  const parsed = parseSpringValidationMessage(msg);
  if (!parsed?.field || !parsed?.min || !parsed?.max) return null;

  const map: Record<string, OrderItemField> = {
    link: "link",
    size: "size",
    configuration: "configuration",
  };

  const key = map[parsed.field];
  if (!key) return null;

  const lim = ORDER_ITEM_LIMITS[key];
  return validateRequiredLen("x", t, { label: t(lim.labelKey), min: parsed.min, max: parsed.max });
}
