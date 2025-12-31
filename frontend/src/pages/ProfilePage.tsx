import { useMemo } from "react";
import { useAuth } from "../auth/AuthContext";
import { useTranslation } from "react-i18next";
import { useAppToast } from "../components/Toast";

async function copyToClipboard(text: string): Promise<boolean> {
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text);
      return true;
    }
  } catch {
    // fall through
  }

  try {
    const ta = document.createElement("textarea");
    ta.value = text;
    ta.setAttribute("readonly", "");
    ta.style.position = "fixed";
    ta.style.left = "-9999px";
    ta.style.top = "-9999px";
    ta.style.opacity = "0";

    document.body.appendChild(ta);
    ta.focus();
    ta.select();
    ta.setSelectionRange(0, ta.value.length);

    const ok = document.execCommand("copy");
    document.body.removeChild(ta);
    return ok;
  } catch {
    return false;
  }
}

export function ProfilePage() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const { showToast } = useAppToast();

  const pretty = useMemo(() => JSON.stringify(user, null, 2), [user]);

  async function onCopy(kind: "email" | "id", text: string) {
    const ok = await copyToClipboard(text);

    if (!ok) {
      showToast(t("profile.copyFail"), "error");
      return;
    }

    showToast(kind === "email" ? t("profile.copySuccessEmail") : t("profile.copySuccessId"), "success");
  }

  if (!user) return null;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">{t("profile.title")}</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">{t("profile.subtitle")}</p>
      </div>

      <div className="rounded-3xl border bg-white p-6 shadow-sm dark:bg-slate-950 dark:border-slate-800">
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="rounded-2xl border p-4 dark:border-slate-800">
            <div className="text-xs font-medium text-slate-500 dark:text-slate-400">{t("profile.email")}</div>
            <div className="mt-1 font-semibold break-all">{user.email}</div>
            <button
              onClick={() => onCopy("email", user.email)}
              className="mt-3 px-3 py-2 rounded-xl text-sm border bg-white hover:bg-slate-50 transition
                         dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
            >
              {t("profile.copyEmail")}
            </button>
          </div>

          <div className="rounded-2xl border p-4 dark:border-slate-800">
            <div className="text-xs font-medium text-slate-500 dark:text-slate-400">{t("profile.userId")}</div>
            <div className="mt-1 font-semibold break-all">{user.id}</div>
            <button
              onClick={() => onCopy("id", user.id)}
              className="mt-3 px-3 py-2 rounded-xl text-sm border bg-white hover:bg-slate-50 transition
                         dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
            >
              {t("profile.copyId")}
            </button>
          </div>
        </div>

        <details className="mt-6">
          <summary className="cursor-pointer text-sm font-semibold">{t("profile.rawJson")}</summary>
          <pre className="mt-3 text-xs rounded-2xl border bg-slate-50 p-4 overflow-auto dark:bg-slate-900/40 dark:border-slate-800">
            {pretty}
          </pre>
        </details>
      </div>
    </div>
  );
}
