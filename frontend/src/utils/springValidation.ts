import type { TFunction } from "i18next";

export type FieldErrorResult<F extends string> = {
  field: F | null;
  text: string;
};

function hasCyrillic(s: string) {
  return /[а-яё]/i.test(s);
}

function parseField(msg: string): string | null {
  return (
    msg.match(/on field '([^']+)'/i)?.[1] ??
    msg.match(/field\s+'([^']+)'/i)?.[1] ??
    null
  );
}

function parseRange(msg: string): { min: number; max: number } | null {
  const ru =
    msg.match(/от\s*(\d+)\s*до\s*(\d+)/i) ??
    msg.match(/(\d+)\s*до\s*(\d+)/i);
  if (ru) return { min: Number(ru[1]), max: Number(ru[2]) };

  const en =
    msg.match(/between\s*(\d+)\s*and\s*(\d+)/i) ??
    msg.match(/(\d+)\s*to\s*(\d+)/i);
  if (en) return { min: Number(en[1]), max: Number(en[2]) };

  const nums = msg.match(/,(\d+),(\d+)\]/);
  if (nums) {
    const a = Number(nums[2]);
    const b = Number(nums[1]);
    if (Number.isFinite(a) && Number.isFinite(b)) return { min: a, max: b };
  }

  return null;
}


export function localizeBackendLengthError<F extends string>(
  msg: string,
  t: TFunction,
  opts: {
    fieldLabels: Record<F, string>;
    fieldNameMap: Record<string, F>; 
    defaultTextKey: string; 
    fixFormKey?: string; 
  }
): FieldErrorResult<F> {
  const lower = msg.toLowerCase();
  const fixFormKey = opts.fixFormKey ?? "errors.fixForm";

  if (lower.includes("validation failed") || lower.includes("field error")) {
    return { field: null, text: t(fixFormKey) };
  }

  const springField = parseField(msg);
  const range = parseRange(msg);

  if (springField && range) {
    const mapped = opts.fieldNameMap[springField.trim()];
    if (mapped) {
      return {
        field: mapped,
        text: t("errors.fieldLengthBetween", {
          field: opts.fieldLabels[mapped],
          min: range.min,
          max: range.max,
        }),
      };
    }
  }

  if (hasCyrillic(msg)) return { field: null, text: msg };
  return { field: null, text: msg || t(opts.defaultTextKey) };
}
