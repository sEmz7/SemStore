// src/components/AuthCard.tsx
import React from "react";

export function AuthCard({
  title,
  subtitle,
  error,
  children,
}: {
  title: string;
  subtitle?: string;
  error?: string | null;
  children: React.ReactNode;
}) {
  return (
    <div className="mx-auto max-w-md px-2 sm:px-0">
      <div className="rounded-3xl border bg-white p-4 sm:p-6 shadow-sm dark:bg-slate-950 dark:border-slate-800">
        <div>
          <h1 className="text-2xl font-semibold">{title}</h1>
          {!!subtitle && <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">{subtitle}</p>}
        </div>

        {!!error && (
          <div
            className="mt-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                       dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200 break-words"
          >
            {error}
          </div>
        )}

        <div className="mt-5">{children}</div>
      </div>
    </div>
  );
}

export default AuthCard;
