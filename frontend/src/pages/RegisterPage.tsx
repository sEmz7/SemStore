// src/pages/RegisterPage.tsx
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../api/auth";
import { useTranslation } from "react-i18next";

function Field({
  label,
  type = "text",
  value,
  onChange,
  placeholder,
}: {
  label: string;
  type?: string;
  value: string;
  placeholder?: string;
  onChange(v: string): void;
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
        className="w-full rounded-xl border px-3 py-2 text-sm outline-none
                   bg-white dark:bg-slate-950 dark:border-slate-800
                   focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800"
      />
    </label>
  );
}

export function RegisterPage() {
  const { t } = useTranslation();
  const nav = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const canSubmit = email.trim().length > 0 && password.trim().length >= 4;

  return (
    <div className="mx-auto max-w-md">
      <div className="rounded-3xl border bg-white p-6 shadow-sm dark:bg-slate-950 dark:border-slate-800">
        <div>
          <h1 className="text-2xl font-semibold">{t("auth.register")}</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            {t("auth.subtitleRegister")}
          </p>
        </div>

        {err && (
          <div className="mt-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                          dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200">
            {err}
          </div>
        )}

        <div className="mt-5 grid gap-3">
          <Field
            label={t("auth.email")}
            value={email}
            onChange={setEmail}
            placeholder="new@test.com"
          />
          <Field
            label={t("auth.password")}
            type="password"
            value={password}
            onChange={setPassword}
            placeholder={t("auth.passwordHint")}
          />

          <button
            disabled={!canSubmit || loading}
            onClick={async () => {
              setErr(null);
              setLoading(true);
              try {
                await register(email, password);
                nav("/login", { replace: true });
              } catch (e: any) {
                setErr(e?.response?.data?.message ?? "Register failed");
              } finally {
                setLoading(false);
              }
            }}
            className="mt-2 w-full px-4 py-2 rounded-xl text-sm font-semibold
                       bg-slate-900 text-white hover:bg-slate-800 transition
                       disabled:opacity-50 disabled:hover:bg-slate-900
                       dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
          >
            {loading ? t("auth.creating") : t("auth.createAccount")}
          </button>

          <div className="text-sm text-slate-600 dark:text-slate-300">
            {t("auth.haveAccount")}{" "}
            <Link
              to="/login"
              className="font-semibold underline underline-offset-4 hover:opacity-80"
            >
              {t("auth.login")}
            </Link>
          </div>
        </div>
      </div>

      <p className="mt-3 text-xs text-slate-500 dark:text-slate-400">
        {t("auth.passwordValidationNote")}
      </p>
    </div>
  );
}
