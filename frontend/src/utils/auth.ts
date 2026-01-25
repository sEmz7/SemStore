// src/utils/auth.ts
import type { TFunction } from "i18next";
export const PASS_MIN = 4;

export function isEmailValid(email: string) {
  const e = email.trim();
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(e);
}


function isEmailBackendError(msg: string) {
  const m = msg.toLowerCase();
  return (
    m.includes("email") &&
    (m.includes("well-formed") ||
      m.includes("valid") ||
      m.includes("format") ||
      m.includes("должно иметь формат адреса электронной почты"))
  );
}

function hasRu(s: string) {
  return /[а-яё]/i.test(s);
}

export function localizeLoginError(msg: unknown, t: TFunction): string | null {
  if (typeof msg !== "string") return null;

  if (msg.startsWith("errors.")) return t(msg);

  if (isEmailBackendError(msg)) return t("errors.emailInvalid");

  const m = msg.toLowerCase();
  if (
    m.includes("invalid password") ||
    m.includes("bad credentials") ||
    m.includes("unauthorized") ||
    m.includes("wrong password") ||
    m.includes("invalid credentials")
  ) {
    return t("errors.invalidPassword");
  }

  if (hasRu(msg)) return msg;
  return msg || null;
}

export function localizeRegisterError(e: any, t: TFunction, email: string) {
  const msg: string = e?.response?.data?.message ?? e?.message ?? "";
  const m = msg.toLowerCase();

  if (m.includes("already exists") || m.includes("already exist") || m.includes("exists")) {
    return t("errors.userAlreadyExists", { email });
  }

  if (msg && isEmailBackendError(msg)) return t("errors.emailInvalid");
  if (msg && hasRu(msg)) return msg;

  return msg ? msg : t("errors.authUnknown");
}
