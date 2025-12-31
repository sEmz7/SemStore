// src/utils/tr.ts
import type { TFunction } from "i18next";

type Opt = Record<string, any> | undefined;

export function tr(t: TFunction, key: string, options?: Opt) {
  return t(key, options as any);
}

export function trDef(t: TFunction, key: string, defaultValue: string, options?: Opt) {
  return t(key, { defaultValue, ...(options ?? {}) } as any);
}
