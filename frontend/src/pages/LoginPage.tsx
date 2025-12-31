import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { useTranslation } from "react-i18next";
import FormField from "../components/FormField";
import { isEmailValid, localizeLoginError } from "../utils/auth";

export function LoginPage() {
  const nav = useNavigate();
  const { login } = useAuth();
  const { t } = useTranslation();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const [touched, setTouched] = useState({ email: false, password: false });

  const emailTrim = email.trim();
  const passTrim = password.trim();

  const emailOk = useMemo(() => isEmailValid(emailTrim), [emailTrim]);
  const passOk = useMemo(() => passTrim.length > 0, [passTrim]);

  const emailError =
    touched.email && emailTrim.length > 0 && !emailOk ? t("errors.emailInvalid") : null;

  const passError =
    touched.password && passTrim.length === 0 ? t("errors.required") : null;

  const canSubmit = emailOk && passOk && !loading;

  async function submit() {
    setTouched({ email: true, password: true });

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
      await login(emailTrim, password);
      nav("/", { replace: true });
    } catch (e: any) {
      const msg = e?.response?.data?.message ?? e?.message;
      setErr(localizeLoginError(msg, t) ?? t("errors.authUnknown"));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-md px-2 sm:px-0">
      <div className="rounded-3xl border bg-white p-4 sm:p-6 shadow-sm dark:bg-slate-950 dark:border-slate-800">
        <div>
          <h1 className="text-2xl font-semibold">{t("auth.login")}</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">{t("auth.subtitleLogin")}</p>
        </div>

        {err && (
          <div className="mt-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                          dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200 break-words">
            {err}
          </div>
        )}

        <div className="mt-5 grid gap-3">
          <FormField
            label={t("auth.email")}
            type="email"
            value={email}
            onChange={(v) => {
              setEmail(v);
              setErr(null);
            }}
            onBlur={() => setTouched((p) => ({ ...p, email: true }))}
            placeholder={t("auth.enterEmail")}
            autoComplete="email"
            error={emailError}
          />

          <FormField
            label={t("auth.password")}
            type="password"
            value={password}
            onChange={(v) => {
              setPassword(v);
              setErr(null);
            }}
            onBlur={() => setTouched((p) => ({ ...p, password: true }))}
            placeholder="••••••••"
            autoComplete="current-password"
            error={passError}
          />

          <button
            disabled={!canSubmit}
            onClick={submit}
            className="mt-2 w-full px-4 py-2 rounded-xl text-sm font-semibold
                       bg-slate-900 text-white hover:bg-slate-800 transition
                       disabled:opacity-50 disabled:hover:bg-slate-900
                       dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
          >
            {loading ? t("auth.signingIn") : t("auth.signIn")}
          </button>

          <div className="text-sm text-slate-600 dark:text-slate-300">
            {t("auth.noAccount")}{" "}
            <Link to="/register" className="font-semibold underline underline-offset-4 hover:opacity-80">
              {t("auth.register")}
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
