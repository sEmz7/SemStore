// src/components/LanguageSwitcher.tsx
import { useTranslation } from "react-i18next";

export function LanguageSwitcher() {
  const { i18n } = useTranslation();
  const current = i18n.resolvedLanguage || i18n.language || "ru";

  return (
    <select
      value={current.startsWith("en") ? "en" : "ru"}
      onChange={(e) => i18n.changeLanguage(e.target.value)}
      className="rounded-xl border px-3 py-2 text-sm bg-white hover:bg-slate-50 transition
                 dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
      aria-label="Language"
      title="Language"
    >
      <option value="ru">RU</option>
      <option value="en">EN</option>
    </select>
  );
}

export default LanguageSwitcher;
