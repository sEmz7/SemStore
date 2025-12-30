// src/pages/LoginPage.tsx
import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { useTranslation } from "react-i18next";
import type { TFunction } from "i18next";

function isEmailValid(email: string) {
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

function Field({
  label,
  type = "text",
  value,
  onChange,
  placeholder,
  error,
  onBlur,
  onFocus,
}: {
  label: string;
  type?: string;
  value: string;
  placeholder?: string;
  error?: string | null;
  onChange(v: string): void;
  onBlur?(): void;
  onFocus?(): void;
}) {
  return (
    <label className="grid gap-1">
      <span className="text-xs font-medium text-slate-600 dark:text-slate-300">
        {label}
      </span>
      <input
        type={type}
        value={value}
        placeholder={placeholder ?? label}
        onChange={(e) => onChange(e.target.value)}
        onBlur={onBlur}
        onFocus={onFocus}
        className={cn(
          "w-full rounded-xl border px-3 py-2 text-sm outline-none",
          "bg-white dark:bg-slate-950 dark:border-slate-800",
          "focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800",
          error && "border-red-300 focus:ring-red-200 dark:border-red-900/50 dark:focus:ring-red-950/40"
        )}
      />
      {!!error && <div className="text-xs text-red-600 dark:text-red-300 break-words">{error}</div>}
    </label>
  );
}

function cn(...a: Array<string | false | null | undefined>) {
  return a.filter(Boolean).join(" ");
}

function localizeApiErrorMessage(msg: unknown, t: TFunction): string | null {
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

  if (/[а-яё]/i.test(msg)) return msg;

  return msg || null;
}

export function LoginPage() {
  const nav = useNavigate();
  const { login } = useAuth();
  const { t } = useTranslation();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const [touchedEmail, setTouchedEmail] = useState(false);
  const [touchedPass, setTouchedPass] = useState(false);

  const emailOk = useMemo(() => isEmailValid(email), [email]);
  const passOk = useMemo(() => password.trim().length > 0, [password]);

  const emailError =
    touchedEmail && email.trim().length > 0 && !emailOk ? t("errors.emailInvalid") : null;

  const passError =
    touchedPass && password.trim().length === 0 ? t("errors.required") : null;

  const canSubmit = emailOk && passOk;

  return (
    <div className="mx-auto max-w-md px-2 sm:px-0">
      <div className="rounded-3xl border bg-white p-4 sm:p-6 shadow-sm dark:bg-slate-950 dark:border-slate-800">
        <div>
          <h1 className="text-2xl font-semibold">{t("auth.login")}</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            {t("auth.subtitleLogin")}
          </p>
        </div>

        {err && (
          <div className="mt-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                          dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200 break-words">
            {err}
          </div>
        )}

        <div className="mt-5 grid gap-3">
          <Field
            label={t("auth.email")}
            type="email"
            value={email}
            onChange={(v) => {
              setEmail(v);
              setErr(null);
            }}
            onBlur={() => setTouchedEmail(true)}
            onFocus={() => setTouchedEmail(true)}
            placeholder={t("auth.enterEmail")}
            error={emailError}
          />

          <Field
            label={t("auth.password")}
            type="password"
            value={password}
            onChange={(v) => {
              setPassword(v);
              setErr(null);
            }}
            onBlur={() => setTouchedPass(true)}
            onFocus={() => setTouchedPass(true)}
            placeholder="••••••••"
            error={passError}
          />

          <button
            disabled={!canSubmit || loading}
            onClick={async () => {
              setTouchedEmail(true);
              setTouchedPass(true);

              if (!emailOk) {
                setErr(t("errors.emailInvalid"));
                return;
              }
              if (!passOk) {
                setErr(t("errors.required"));
                return;
              }

              setErr(null);
              setLoading(true);
              try {
                await login(email.trim(), password);
                nav("/", { replace: true });
              } catch (e: any) {
                const msg = e?.response?.data?.message ?? e?.message;
                setErr(localizeApiErrorMessage(msg, t) ?? t("errors.authUnknown"));
              } finally {
                setLoading(false);
              }
            }}
            className="mt-2 w-full px-4 py-2 rounded-xl text-sm font-semibold
                       bg-slate-900 text-white hover:bg-slate-800 transition
                       disabled:opacity-50 disabled:hover:bg-slate-900
                       dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
          >
            {loading ? t("auth.signingIn") : t("auth.signIn")}
          </button>

          <div className="text-sm text-slate-600 dark:text-slate-300">
            {t("auth.noAccount")}{" "}
            <Link
              to="/register"
              className="font-semibold underline underline-offset-4 hover:opacity-80"
            >
              {t("auth.register")}
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
