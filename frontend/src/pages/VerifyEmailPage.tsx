import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { verifyEmail, resendVerificationCode } from "../api/auth";
import { useAppToast } from "../components/Toast";

const CODE_LENGTH = 6;
const RESEND_COOLDOWN = 60;

export function VerifyEmailPage() {
  const { t } = useTranslation();
  const nav = useNavigate();
  const location = useLocation();
  const { showToast } = useAppToast();

  const email: string | undefined = (location.state as any)?.email;

  useEffect(() => {
    if (!email) nav("/register", { replace: true });
  }, [email, nav]);

  const [digits, setDigits] = useState<string[]>(Array(CODE_LENGTH).fill(""));
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [cooldown, setCooldown] = useState(0);

  const inputRefs = useRef<Array<HTMLInputElement | null>>(Array(CODE_LENGTH).fill(null));
  const submitCalledRef = useRef(false);

  // Cooldown timer
  useEffect(() => {
    if (cooldown <= 0) return;
    const id = setInterval(() => setCooldown((c) => c - 1), 1000);
    return () => clearInterval(id);
  }, [cooldown]);

  async function submit(code: string) {
    if (loading || !email) return;
    setError(null);
    setLoading(true);
    try {
      await verifyEmail(email, code);
      showToast(t("verify.success"), "success");
      nav("/login", { replace: true });
    } catch (e: any) {
      submitCalledRef.current = false;
      const status: number = e?.response?.status;
      const code: string = e?.response?.data?.code ?? "";
      setError(localizeVerifyError(status, code, t));
      setDigits(Array(CODE_LENGTH).fill(""));
      inputRefs.current[0]?.focus();
    } finally {
      setLoading(false);
    }
  }

  function handleChange(index: number, value: string) {
    const digit = value.replace(/\D/g, "").slice(-1);
    const newDigits = [...digits];
    newDigits[index] = digit;
    setDigits(newDigits);
    setError(null);

    if (digit && index < CODE_LENGTH - 1) {
      inputRefs.current[index + 1]?.focus();
    }

    if (newDigits.every((d) => d !== "") && !submitCalledRef.current) {
      submitCalledRef.current = true;
      submit(newDigits.join(""));
    }
  }

  function handleKeyDown(index: number, e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Backspace" && digits[index] === "" && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  }

  function handlePaste(e: React.ClipboardEvent<HTMLInputElement>) {
    e.preventDefault();
    const pasted = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, CODE_LENGTH);
    if (!pasted) return;
    const newDigits = [...digits];
    for (let i = 0; i < pasted.length; i++) {
      newDigits[i] = pasted[i];
    }
    setDigits(newDigits);
    const focusIndex = Math.min(pasted.length, CODE_LENGTH - 1);
    inputRefs.current[focusIndex]?.focus();
    if (newDigits.every((d) => d !== "") && !submitCalledRef.current) {
      submitCalledRef.current = true;
      submit(newDigits.join(""));
    }
  }

  async function handleResend() {
    if (cooldown > 0 || !email) return;
    try {
      await resendVerificationCode(email);
      showToast(t("verify.resendOk"), "success");
      setCooldown(RESEND_COOLDOWN);
    } catch (e: any) {
      const status: number = e?.response?.status;
      const code: string = e?.response?.data?.code ?? "";
      setError(localizeVerifyError(status, code, t));
    }
  }

  if (!email) return null;

  return (
    <div className="mx-auto max-w-md px-2 sm:px-0">
      <div className="rounded-3xl border bg-white p-4 sm:p-6 shadow-sm dark:bg-slate-950 dark:border-slate-800">
        <div>
          <h1 className="text-2xl font-semibold">{t("verify.title")}</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            {t("verify.subtitle", { email })}
          </p>
        </div>

        {error && (
          <div className="mt-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                          dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200 break-words">
            {error}
          </div>
        )}

        <div className="mt-5 grid gap-4">
          <div className="flex gap-2 justify-center">
            {digits.map((digit, i) => (
              <input
                key={i}
                ref={(el) => { inputRefs.current[i] = el; }}
                type="text"
                inputMode="numeric"
                pattern="[0-9]"
                maxLength={1}
                value={digit}
                autoFocus={i === 0}
                autoComplete={i === 0 ? "one-time-code" : "off"}
                onChange={(e) => handleChange(i, e.target.value)}
                onKeyDown={(e) => handleKeyDown(i, e)}
                onPaste={handlePaste}
                disabled={loading}
                className={`
                  w-11 h-12 text-center text-xl font-semibold rounded-xl border-2 outline-none
                  transition-colors bg-white dark:bg-slate-900
                  ${digit
                    ? "border-slate-900 dark:border-white text-slate-900 dark:text-white"
                    : "border-slate-200 dark:border-slate-700 text-slate-400"}
                  focus:border-blue-500 dark:focus:border-blue-400
                  disabled:opacity-50
                `}
              />
            ))}
          </div>

          <button
            disabled={loading || digits.some((d) => d === "")}
            onClick={() => {
              if (!submitCalledRef.current) {
                submitCalledRef.current = true;
                submit(digits.join(""));
              }
            }}
            className="w-full px-4 py-2 rounded-xl text-sm font-semibold
                       bg-slate-900 text-white hover:bg-slate-800 transition
                       disabled:opacity-50 disabled:hover:bg-slate-900
                       dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
          >
            {loading ? t("verify.submitting") : t("verify.submit")}
          </button>

          <div className="text-center text-sm text-slate-500 dark:text-slate-400">
            {cooldown > 0 ? (
              <span>{t("verify.resendIn", { sec: cooldown })}</span>
            ) : (
              <button
                onClick={handleResend}
                disabled={loading}
                className="font-semibold underline underline-offset-4 hover:opacity-80 text-slate-700 dark:text-slate-300 disabled:opacity-50"
              >
                {t("verify.resend")}
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default VerifyEmailPage;

function localizeVerifyError(status: number, code: string, t: import("i18next").TFunction): string {
  switch (code) {
    case "INVALID_VERIFICATION_CODE":
    case "CODE_NOT_FOUND":
    case "VERIFICATION_CODE_EXPIRED": return t("errors.verificationCodeInvalid");
    case "USER_ALREADY_VERIFIED":     return t("errors.emailAlreadyVerified");
    case "TO_MANY_ATTEMPTS_TO_VERIFY_EMAIL": return t("errors.tooManyAttempts");
    case "WAIT_FOR_RESEND_CODE":      return t("errors.resendTooEarly");
  }
  if (status === 404) return t("errors.verificationCodeInvalid");
  return t("errors.authUnknown");
}
