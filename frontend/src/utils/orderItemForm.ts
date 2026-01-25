import type { TFunction } from "i18next";

export type FieldKey = "link" | "size" | "configuration";
export type OrderItemForm = Record<FieldKey, string>;

export const ORDER_ITEM_LIMITS: Record<
  FieldKey,
  { min: number; max: number; labelKey: string; placeholder: string }
> = {
  link: { min: 2, max: 50, labelKey: "order.link", placeholder: "https://example.com/product/1" },
  size: { min: 2, max: 30, labelKey: "order.size", placeholder: "42.5" },
  configuration: { min: 2, max: 255, labelKey: "order.configuration", placeholder: "black" },
};

export function normStr(v: unknown) {
  return (v ?? "").toString().trim();
}

export function validateLen(v: string, min: number, max: number) {
  const s = v.trim();
  if (!s) return { ok: false as const, kind: "required" as const };
  if (s.length < min) return { ok: false as const, kind: "min" as const };
  if (s.length > max) return { ok: false as const, kind: "max" as const };
  return { ok: true as const };
}

export function clientErrorFor(value: string, min: number, max: number, label: string, t: TFunction) {
  const v = validateLen(value, min, max);
  if (v.ok) return null;
  if (v.kind === "required") return t("errors.required");
  return t("errors.fieldLengthBetween", { field: label, min, max });
}
