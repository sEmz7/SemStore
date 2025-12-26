import { useMemo, useState } from "react";
import { useAuth } from "../auth/AuthContext";

export function ProfilePage() {
  const { user } = useAuth();
  const [copied, setCopied] = useState<string | null>(null);

  const pretty = useMemo(() => JSON.stringify(user, null, 2), [user]);

  async function copy(text: string, label: string) {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(label);
      setTimeout(() => setCopied(null), 1200);
    } catch {
      // ignore
    }
  }

  if (!user) return null;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">Profile</h1>
        <p className="text-sm text-slate-500 dark:text-slate-400">
          Your account info
        </p>
      </div>

      <div className="rounded-3xl border bg-white p-6 shadow-sm dark:bg-slate-950 dark:border-slate-800">
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="rounded-2xl border p-4 dark:border-slate-800">
            <div className="text-xs font-medium text-slate-500 dark:text-slate-400">
              Email
            </div>
            <div className="mt-1 font-semibold break-all">{user.email}</div>
            <button
              onClick={() => copy(user.email, "email")}
              className="mt-3 px-3 py-2 rounded-xl text-sm border bg-white hover:bg-slate-50 transition
                         dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
            >
              Copy email {copied === "email" ? "✅" : ""}
            </button>
          </div>

          <div className="rounded-2xl border p-4 dark:border-slate-800">
            <div className="text-xs font-medium text-slate-500 dark:text-slate-400">
              User ID
            </div>
            <div className="mt-1 font-semibold break-all">{user.id}</div>
            <button
              onClick={() => copy(user.id, "id")}
              className="mt-3 px-3 py-2 rounded-xl text-sm border bg-white hover:bg-slate-50 transition
                         dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
            >
              Copy id {copied === "id" ? "✅" : ""}
            </button>
          </div>
        </div>

        <details className="mt-6">
          <summary className="cursor-pointer text-sm font-semibold">
            Raw JSON
          </summary>
          <pre className="mt-3 text-xs rounded-2xl border bg-slate-50 p-4 overflow-auto dark:bg-slate-900/40 dark:border-slate-800">
            {pretty}
          </pre>
        </details>
      </div>
    </div>
  );
}
