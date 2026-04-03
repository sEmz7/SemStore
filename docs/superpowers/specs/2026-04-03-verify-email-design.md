# Дизайн: страница подтверждения email

**Дата:** 2026-04-03  
**Проект:** SemStore Frontend  
**Статус:** Утверждён

---

## Задача

После регистрации backend автоматически отправляет 6-значный код на email пользователя. Пользователь не может войти в систему пока email не подтверждён (backend возвращает 403 `EMAIL_NOT_VERIFIED`). Нужна страница `/verify-email` для ввода кода.

---

## Пользовательский сценарий

1. Пользователь регистрируется → `RegisterPage` перенаправляет на `/verify-email` с `state: { email }`
2. Пользователь пытается войти без верификации → `LoginPage` получает 403, перенаправляет на `/verify-email` с `state: { email }`
3. На странице `/verify-email` пользователь вводит 6-значный код из письма
4. При заполнении всех 6 ячеек форма отправляется автоматически
5. При успехе → toast + редирект на `/login`
6. При ошибке → сообщение об ошибке
7. Кнопка "Отправить повторно" с cooldown 60 секунд

---

## Архитектура и data flow

```
RegisterPage            LoginPage (403 EMAIL_NOT_VERIFIED)
    │                           │
    └─────────► /verify-email ◄─┘
                  state: { email }
                       │
             VerifyEmailPage
             ├─ email из location.state
             ├─ если email нет → redirect /register
             │
             ├─ 6 OTP-инпутов (digits: string[6])
             ├─ при заполнении всех 6 → autosubmit
             │
             ├─ POST /auth/verify-email { email, code }
             │    204 → toast success → navigate /login
             │    ошибка → показать сообщение
             │
             └─ Кнопка resend (cooldown 60сек)
                  POST /auth/resend-verification-code { email }
```

---

## Компоненты и состояние

### Состояние компонента

```typescript
const [digits, setDigits] = useState(['','','','','',''])
const [error, setError] = useState<string | null>(null)
const [loading, setLoading] = useState(false)
const [cooldown, setCooldown] = useState(0)  // секунды до разблокировки resend
```

### Layout

Страница использует тот же inline-стиль что `LoginPage` и `RegisterPage`:
- Белая карточка, `rounded-3xl`, `shadow-sm`, `dark:bg-slate-950`
- `max-w-md`, `mx-auto`
- Заголовок + subtitle с email

### OTP-инпуты (6 ячеек)

- 6 отдельных `<input>` элементов, `maxLength=1`, только цифры
- `inputMode="numeric"`, `pattern="[0-9]"`
- Ввод цифры → фокус переходит на следующую ячейку
- Backspace на пустой ячейке → фокус на предыдущую
- Paste → распределяет цифры по ячейкам начиная с текущей
- При заполнении всех 6 → автоматически вызывается `submit()`
- Визуальное состояние: активная ячейка имеет синий border

### Кнопки

- **"Подтвердить"** — кнопка во всю ширину, `disabled` во время загрузки
- **"Отправить повторно"** / **"Повторно через N сек"** — текстовая ссылка под кнопкой, `disabled` во время cooldown

### Ошибки

Красный блок под заголовком (тот же паттерн что в `LoginPage`):
```tsx
{error && (
  <div className="mt-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                  dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200 break-words">
    {error}
  </div>
)}
```

---

## Обработка ошибок backend

| HTTP | Условие | Текст ошибки (ключ) |
|---|---|---|
| 404 | любое | `errors.verificationCodeInvalid` |
| 409 | `EMAIL_ALREADY_VERIFIED` | `errors.emailAlreadyVerified` |
| 409 | `VERIFICATION_CODE_EXPIRED` или не найден | `errors.verificationCodeInvalid` |
| 409 | `TOO_MANY_ATTEMPTS` | `errors.tooManyAttempts` |
| 409 | `RESEND_TOO_EARLY` | `errors.resendTooEarly` |
| иное | — | `errors.authUnknown` |

---

## Локализация

### Новые ключи в `verify`

| Ключ | RU | EN |
|---|---|---|
| `verify.title` | Подтверждение email | Email verification |
| `verify.subtitle` | Введите код, отправленный на {{email}} | Enter the code sent to {{email}} |
| `verify.submit` | Подтвердить | Verify |
| `verify.submitting` | Проверяем... | Verifying... |
| `verify.resend` | Отправить повторно | Resend code |
| `verify.resendIn` | Повторно через {{sec}} сек | Resend in {{sec}}s |
| `verify.success` | Email успешно подтверждён! | Email verified successfully! |
| `verify.resendOk` | Код отправлен повторно | Code resent |

### Новые ключи в `errors`

| Ключ | RU | EN |
|---|---|---|
| `errors.verificationCodeInvalid` | Неверный или истёкший код | Invalid or expired code |
| `errors.emailAlreadyVerified` | Email уже подтверждён | Email already verified |
| `errors.tooManyAttempts` | Превышено количество попыток. Запросите новый код | Too many attempts. Request a new code |
| `errors.resendTooEarly` | Подождите перед повторной отправкой | Please wait before resending |

---

## Файлы для изменения

| Файл | Действие |
|---|---|
| `src/api/auth.ts` | + `verifyEmail(email, code)`, `resendVerificationCode(email)` |
| `src/pages/VerifyEmailPage.tsx` | создать |
| `src/pages/RegisterPage.tsx` | redirect → `/verify-email` с `state: { email }` |
| `src/pages/LoginPage.tsx` | при 403 `EMAIL_NOT_VERIFIED` → redirect `/verify-email` с `state: { email }` |
| `src/App.tsx` | + route `/verify-email` → `<VerifyEmailPage />` |
| `src/locales/ru.ts` | + ключи `verify.*` и новые `errors.*` |
| `src/locales/en.ts` | + те же ключи на английском |

---

## Архитектурные решения

- **Email в state, не в URL** — email содержит персональные данные, не должен быть в адресной строке
- **Нет нового хука** — логика достаточно простая для inline в компоненте
- **Нет нового компонента** — OTP-инпуты реализуются прямо в странице
- **Страница не защищена ProtectedRoute** — пользователь ещё не авторизован
- **Cooldown через useState + useEffect** — `setInterval` декрементирует счётчик каждую секунду
