// src/hooks/useAuthForm.ts
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { PASS_MIN, isEmailValid } from "../utils/auth";

type Mode = "login" | "register";

export function useAuthForm(mode: Mode) {
  const { t } = useTranslation();

  const [form, setForm] = useState({ email: "", password: "" });
  const [touched, setTouched] = useState({ email: false, password: false });

  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const emailOk = isEmailValid(form.email);
  const passOk = mode === "login" ? form.password.trim().length > 0 : form.password.trim().length >= PASS_MIN;

  const emailError =
    touched.email && form.email.trim().length > 0 && !emailOk ? t("errors.emailInvalid") : null;

  const passError =
    touched.password && form.password.trim().length > 0 && !passOk
      ? mode === "login"
        ? t("errors.required")
        : t("errors.passwordMinLength", { min: PASS_MIN })
      : null;

  const canSubmit = emailOk && passOk && !loading;

  function setField(key: "email" | "password", value: string) {
    setForm((p) => ({ ...p, [key]: value }));
    setErr(null);
  }

  function touch(key: "email" | "password") {
    setTouched((p) => ({ ...p, [key]: true }));
  }

  function touchAll() {
    touch("email");
    touch("password");
  }

  return {
    t,
    form,
    touched,
    err,
    setErr,
    loading,
    setLoading,
    emailOk,
    passOk,
    emailError,
    passError,
    canSubmit,
    setField,
    touch,
    touchAll,
  };
}
