// src/components/ConfirmDialog.tsx
import { useEffect, useRef } from "react";

export type ConfirmDialogProps = {
  open: boolean;
  title: string;
  description?: string;
  confirmText: string;
  cancelText: string;
  loading?: boolean;
  onConfirm(): void;
  onClose(): void;
};

export function ConfirmDialog({
  open,
  title,
  description,
  confirmText,
  cancelText,
  loading,
  onConfirm,
  onClose,
}: ConfirmDialogProps) {
  const cancelBtnRef = useRef<HTMLButtonElement | null>(null);

  useEffect(() => {
    if (!open) return;

    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    const t = window.setTimeout(() => cancelBtnRef.current?.focus(), 0);

    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape" && !loading) onClose();
    };
    window.addEventListener("keydown", onKeyDown);

    return () => {
      window.clearTimeout(t);
      window.removeEventListener("keydown", onKeyDown);
      document.body.style.overflow = prevOverflow;
    };
  }, [open, loading, onClose]);

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-end sm:items-center justify-center px-4 py-4"
      role="dialog"
      aria-modal="true"
    >
      {/* overlay */}
      <button
        type="button"
        aria-label="Close"
        onClick={() => !loading && onClose()}
        className="absolute inset-0 bg-black/40"
      />

      {/* sheet/dialog */}
      <div
        className="relative w-full max-w-sm rounded-t-2xl sm:rounded-2xl border bg-white p-4 shadow-lg
                   dark:bg-slate-950 dark:border-slate-800"
      >
        <div className="text-base font-semibold">{title}</div>

        {description && (
          <div className="mt-1 text-sm text-slate-600 dark:text-slate-300 break-words">
            {description}
          </div>
        )}

        <div className="mt-4 flex flex-col-reverse sm:flex-row gap-2 sm:justify-end">
          <button
            ref={cancelBtnRef}
            type="button"
            disabled={!!loading}
            onClick={onClose}
            className="w-full sm:w-auto px-4 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                       disabled:opacity-50
                       dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
          >
            {cancelText}
          </button>

          <button
            type="button"
            disabled={!!loading}
            onClick={onConfirm}
            className="w-full sm:w-auto px-4 py-2 rounded-xl text-sm font-semibold border transition disabled:opacity-50
                       border-red-200 text-red-700 hover:bg-red-50
                       dark:border-red-900/50 dark:text-red-300 dark:hover:bg-red-950/40"
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}

export default ConfirmDialog;
