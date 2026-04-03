# Verify Email Page — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Добавить страницу `/verify-email` с OTP-вводом 6-значного кода для подтверждения email после регистрации.

**Architecture:** Email передаётся через `location.state` (не в URL). Страница содержит 6 отдельных input-ячеек с автопереходом фокуса. При заполнении всех 6 цифр форма отправляется автоматически. При успехе — toast + редирект на `/login`.

**Tech Stack:** React 19, TypeScript, React Router 7, Axios, Tailwind CSS 3, i18next (локализация в `.ts` файлах)

---

### Task 1: Добавить локализационные ключи

**Files:**
- Modify: `frontend/src/locales/ru.ts`
- Modify: `frontend/src/locales/en.ts`

- [ ] **Шаг 1: Добавить ключи `verify` и новые `errors` в `ru.ts`**

В файле `frontend/src/locales/ru.ts` добавить новый блок `verify` перед закрывающей скобкой объекта (после блока `errors`), и расширить блок `errors` четырьмя новыми ключами:

```typescript
// Добавить блок verify после блока profile:
    verify: {
      title: "Подтверждение email",
      subtitle: "Введите код, отправленный на {{email}}",
      submit: "Подтвердить",
      submitting: "Проверяем...",
      resend: "Отправить повторно",
      resendIn: "Повторно через {{sec}} сек",
      success: "Email успешно подтверждён!",
      resendOk: "Код отправлен повторно",
    },
```

В блок `errors` добавить (перед закрывающей скобкой):
```typescript
      verificationCodeInvalid: "Неверный или истёкший код",
      emailAlreadyVerified: "Email уже подтверждён",
      tooManyAttempts: "Превышено количество попыток. Запросите новый код",
      resendTooEarly: "Подождите перед повторной отправкой",
```

- [ ] **Шаг 2: Добавить те же ключи в `en.ts`**

В файле `frontend/src/locales/en.ts` добавить блок `verify` и расширить `errors`:

```typescript
// Блок verify:
    verify: {
      title: "Email verification",
      subtitle: "Enter the code sent to {{email}}",
      submit: "Verify",
      submitting: "Verifying...",
      resend: "Resend code",
      resendIn: "Resend in {{sec}}s",
      success: "Email verified successfully!",
      resendOk: "Code resent",
    },
```

В блок `errors` добавить:
```typescript
      verificationCodeInvalid: "Invalid or expired code",
      emailAlreadyVerified: "Email already verified",
      tooManyAttempts: "Too many attempts. Request a new code",
      resendTooEarly: "Please wait before resending",
```

- [ ] **Шаг 3: Убедиться что TypeScript не падает**

```bash
cd /Users/semzz/Desktop/study/SemStore/frontend
npx tsc --noEmit 2>&1 | head -20
```

Ожидаемый результат: нет ошибок (или только уже существующие несвязанные).

- [ ] **Шаг 4: Коммит**

```bash
git add frontend/src/locales/ru.ts frontend/src/locales/en.ts
git commit -m "feat: добавлены локализационные ключи для страницы верификации email"
```

---

### Task 2: Добавить API-функции

**Files:**
- Modify: `frontend/src/api/auth.ts`

- [ ] **Шаг 1: Добавить `verifyEmail` и `resendVerificationCode` в `auth.ts`**

В конец файла `frontend/src/api/auth.ts` добавить:

```typescript
export async function verifyEmail(email: string, code: string): Promise<void> {
  await authApi.post("/auth/verify-email", { email, code });
}

export async function resendVerificationCode(email: string): Promise<void> {
  await authApi.post("/auth/resend-verification-code", { email });
}
```

- [ ] **Шаг 2: Проверить TypeScript**

```bash
cd /Users/semzz/Desktop/study/SemStore/frontend
npx tsc --noEmit 2>&1 | head -20
```

Ожидаемый результат: нет ошибок.

- [ ] **Шаг 3: Коммит**

```bash
git add frontend/src/api/auth.ts
git commit -m "feat: добавлены API-функции verifyEmail и resendVerificationCode"
```

---

### Task 3: Создать VerifyEmailPage

**Files:**
- Create: `frontend/src/pages/VerifyEmailPage.tsx`

- [ ] **Шаг 1: Создать файл `VerifyEmailPage.tsx`**

Создать файл `frontend/src/pages/VerifyEmailPage.tsx` с полным содержимым:

```typescript
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
      const msg: string = e?.response?.data?.message ?? "";
      setError(localizeVerifyError(status, msg, t));
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
      const msg: string = e?.response?.data?.message ?? "";
      setError(localizeVerifyError(status, msg, t));
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
                className="font-semibold underline underline-offset-4 hover:opacity-80 text-slate-700 dark:text-slate-300"
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

function localizeVerifyError(status: number, msg: string, t: import("i18next").TFunction): string {
  if (status === 404) return t("errors.verificationCodeInvalid");
  if (status === 409) {
    const m = msg.toUpperCase();
    if (m.includes("ALREADY_VERIFIED")) return t("errors.emailAlreadyVerified");
    if (m.includes("TOO_MANY")) return t("errors.tooManyAttempts");
    if (m.includes("RESEND_TOO_EARLY") || m.includes("TOO_EARLY")) return t("errors.resendTooEarly");
    return t("errors.verificationCodeInvalid");
  }
  return t("errors.authUnknown");
}
```

- [ ] **Шаг 2: Проверить TypeScript**

```bash
cd /Users/semzz/Desktop/study/SemStore/frontend
npx tsc --noEmit 2>&1 | head -30
```

Ожидаемый результат: нет ошибок связанных с новым файлом.

- [ ] **Шаг 3: Коммит**

```bash
git add frontend/src/pages/VerifyEmailPage.tsx
git commit -m "feat: создана страница VerifyEmailPage с OTP-вводом"
```

---

### Task 4: Добавить маршрут в App.tsx

**Files:**
- Modify: `frontend/src/App.tsx`

- [ ] **Шаг 1: Импортировать VerifyEmailPage и добавить маршрут**

В файле `frontend/src/App.tsx`:

1. Добавить импорт после существующих импортов страниц:
```typescript
import { VerifyEmailPage } from "./pages/VerifyEmailPage";
```

2. Добавить маршрут после `<Route path="/register" .../>`:
```tsx
<Route path="/verify-email" element={<VerifyEmailPage />} />
```

- [ ] **Шаг 2: Проверить TypeScript**

```bash
cd /Users/semzz/Desktop/study/SemStore/frontend
npx tsc --noEmit 2>&1 | head -20
```

Ожидаемый результат: нет ошибок.

- [ ] **Шаг 3: Коммит**

```bash
git add frontend/src/App.tsx
git commit -m "feat: добавлен маршрут /verify-email"
```

---

### Task 5: Обновить RegisterPage — редирект на /verify-email

**Files:**
- Modify: `frontend/src/pages/RegisterPage.tsx:53-54`

- [ ] **Шаг 1: Изменить редирект после успешной регистрации**

В файле `frontend/src/pages/RegisterPage.tsx` найти строку (около строки 54):
```typescript
      nav("/login", { replace: true });
```

Заменить на:
```typescript
      nav("/verify-email", { replace: true, state: { email: emailTrim } });
```

- [ ] **Шаг 2: Проверить TypeScript**

```bash
cd /Users/semzz/Desktop/study/SemStore/frontend
npx tsc --noEmit 2>&1 | head -20
```

Ожидаемый результат: нет ошибок.

- [ ] **Шаг 3: Коммит**

```bash
git add frontend/src/pages/RegisterPage.tsx
git commit -m "feat: после регистрации редирект на /verify-email"
```

---

### Task 6: Обновить LoginPage — обработка 403 EMAIL_NOT_VERIFIED

**Files:**
- Modify: `frontend/src/pages/LoginPage.tsx:50-55`

- [ ] **Шаг 1: Обработать 403 в catch-блоке LoginPage**

В файле `frontend/src/pages/LoginPage.tsx` найти catch-блок (около строки 52):
```typescript
    } catch (e: any) {
      const msg = e?.response?.data?.message ?? e?.message;
      setErr(localizeLoginError(msg, t) ?? t("errors.authUnknown"));
    }
```

Заменить на:
```typescript
    } catch (e: any) {
      const status: number = e?.response?.status;
      const msg: string = e?.response?.data?.message ?? e?.message ?? "";
      if (status === 403 && msg.toUpperCase().includes("EMAIL_NOT_VERIFIED")) {
        nav("/verify-email", { state: { email: emailTrim } });
        return;
      }
      setErr(localizeLoginError(msg, t) ?? t("errors.authUnknown"));
    }
```

- [ ] **Шаг 2: Проверить TypeScript**

```bash
cd /Users/semzz/Desktop/study/SemStore/frontend
npx tsc --noEmit 2>&1 | head -20
```

Ожидаемый результат: нет ошибок.

- [ ] **Шаг 3: Коммит**

```bash
git add frontend/src/pages/LoginPage.tsx
git commit -m "feat: LoginPage перенаправляет на /verify-email при 403 EMAIL_NOT_VERIFIED"
```

---

### Task 7: Финальная проверка сборки

- [ ] **Шаг 1: Запустить полный build**

```bash
cd /Users/semzz/Desktop/study/SemStore/frontend
npm run build 2>&1 | tail -20
```

Ожидаемый результат: `✓ built in ...` без ошибок.

- [ ] **Шаг 2: Запустить dev-сервер и проверить вручную**

```bash
cd /Users/semzz/Desktop/study/SemStore/frontend
npm run dev
```

Проверить в браузере:
1. Перейти на `/register`, зарегистрировать нового пользователя → должен попасть на `/verify-email`
2. На странице `/verify-email` ввести 6 цифр → должна отправиться форма автоматически
3. Backspace на пустой ячейке → фокус переходит назад
4. Вставить код через Ctrl+V → цифры распределяются по ячейкам
5. Кнопка "Отправить повторно" → после нажатия блокируется на 60 секунд
6. Перейти на `/login`, войти с неподтверждённым email → редирект на `/verify-email`

- [ ] **Шаг 3: Финальный коммит (если нужны правки по итогам проверки)**

```bash
git add -p
git commit -m "fix: правки после ручной проверки"
```
