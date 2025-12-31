// src/pages/ProfilePage.tsx
import { useAuth } from "../auth/AuthContext";
import { useTranslation } from "react-i18next";
import { useAppToast } from "../components/Toast";
import { copyToClipboard } from "../utils/clipboard";

function InfoCard({
  label,
  value,
  buttonText,
  onCopy,
}: {
  label: string;
  value: string;
  buttonText: string;
  onCopy(): void;
}) {
  return (
    <div className="rounded-2xl border p-4 dark:border-slate-800">
      <div className="text-xs font-medium text-slate-500 dark:text-slate-400">{label}</div>
      <div className="mt-1 font-semibold break-all">{value}</div>

      <button
        onClick={onCopy}
        className="mt-3 px-3 py-2 rounded-xl text-sm border bg-white hover:bg-slate-50 transition
                   dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
      >
        {buttonText}
      </button>
    </div>
  );
}

export function ProfilePage() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const { showToast } = useAppToast();

  if (!user) return null;

  async function handleCopy(text: string, successKey: "profile.copySuccessEmail" | "profile.copySuccessId") {
    const ok = await copyToClipboard(text);

    if (!ok) {
      showToast(t("profile.copyFail"), "error");
      return;
    }

    showToast(t(successKey), "success");
  }

  const pretty = JSON.stringify(user, null, 2);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">{t("profile.title")}</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">{t("profile.subtitle")}</p>
      </div>

      <div className="rounded-3xl border bg-white p-6 shadow-sm dark:bg-slate-950 dark:border-slate-800">
        <div className="grid gap-4 sm:grid-cols-2">
          <InfoCard
            label={t("profile.email")}
            value={user.email}
            buttonText={t("profile.copyEmail")}
            onCopy={() => handleCopy(user.email, "profile.copySuccessEmail")}
          />

          <InfoCard
            label={t("profile.userId")}
            value={user.id}
            buttonText={t("profile.copyId")}
            onCopy={() => handleCopy(user.id, "profile.copySuccessId")}
          />
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

export default ProfilePage;
