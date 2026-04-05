import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../api/auth";
import { useTranslation } from "react-i18next";
import FormField from "../components/FormField";
import { isEmailValid, localizeRegisterError } from "../utils/auth";

const PASS_MIN = 4;

export function RegisterPage() {
  const nav = useNavigate();
  const { t } = useTranslation();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [agreed, setAgreed] = useState(false);

  const [touched, setTouched] = useState({ email: false, password: false });

  const emailTrim = email.trim();
  const passTrim = password.trim();

  const emailOk = useMemo(() => isEmailValid(emailTrim), [emailTrim]);
  const passOk = useMemo(() => passTrim.length >= PASS_MIN, [passTrim]);

  const emailError =
    touched.email && emailTrim.length > 0 && !emailOk ? t("errors.emailInvalid") : null;

  const passError =
    touched.password && passTrim.length > 0 && !passOk
      ? t("errors.passwordMinLength", { min: PASS_MIN })
      : null;

  const canSubmit = emailOk && passOk && agreed && !loading;

  async function submit() {
    setTouched({ email: true, password: true });

    if (!emailOk) {
      setErr(t("errors.emailInvalid"));
      return;
    }
    if (!passOk) {
      setErr(t("errors.passwordMinLength", { min: PASS_MIN }));
      return;
    }

    if (!agreed) {
      setErr(t("errors.privacyRequired"));
      return;
    }

    setErr(null);
    setLoading(true);
    try {
      await register(emailTrim, password);
      nav("/verify-email", { replace: true, state: { email: emailTrim } });
    } catch (e: any) {
      setErr(localizeRegisterError(e, t, emailTrim));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-md px-2 sm:px-0">
      <div className="rounded-3xl border bg-white p-4 sm:p-6 shadow-sm dark:bg-slate-950 dark:border-slate-800">
        <div>
          <h1 className="text-2xl font-semibold">{t("auth.register")}</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">{t("auth.subtitleRegister")}</p>
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
            autoComplete="new-password"
            error={passError}
          />

          <label className="flex items-start gap-2 cursor-pointer text-sm text-slate-600 dark:text-slate-300">
            <input
              type="checkbox"
              checked={agreed}
              onChange={(e) => {
                setAgreed(e.target.checked);
                setErr(null);
              }}
              className="mt-0.5 h-4 w-4 shrink-0 rounded border-slate-300 accent-slate-900
                         dark:accent-white dark:border-slate-600"
            />
            <span>
              {t("auth.privacyConsent")}{" "}
              <a
                href="/privacy"
                target="_blank"
                rel="noopener noreferrer"
                className="font-semibold underline underline-offset-4 hover:opacity-80"
              >
                {t("auth.privacyLink")}
              </a>
            </span>
          </label>

          <button
            disabled={!canSubmit}
            onClick={submit}
            className="mt-2 w-full px-4 py-2 rounded-xl text-sm font-semibold
                       bg-slate-900 text-white hover:bg-slate-800 transition
                       disabled:opacity-50 disabled:hover:bg-slate-900
                       dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
          >
            {loading ? t("auth.creating") : t("auth.createAccount")}
          </button>

          <div className="text-sm text-slate-600 dark:text-slate-300">
            {t("auth.haveAccount")}{" "}
            <Link to="/login" className="font-semibold underline underline-offset-4 hover:opacity-80">
              {t("auth.login")}
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default RegisterPage;
