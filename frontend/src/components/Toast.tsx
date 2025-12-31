import { createContext, useCallback, useContext, useEffect, useRef, useState } from "react";

export type ToastKind = "success" | "error" | "info";

type ToastState = {
  open: boolean;
  text: string;
  kind: ToastKind;
};

export type ToastApi = {
  showToast: (text: string, kind?: ToastKind, ms?: number) => void;
  hideToast: () => void;
};

const ToastContext = createContext<ToastApi | null>(null);

function cn(...a: Array<string | false | null | undefined>) {
  return a.filter(Boolean).join(" ");
}

function ToastView({ toast, onClose }: { toast: ToastState; onClose: () => void }) {
  const isError = toast.kind === "error";
  const isSuccess = toast.kind === "success";

  return (
    <div
      className={cn(
        "fixed z-50",
        "left-1/2 -translate-x-1/2 sm:left-auto sm:right-4 sm:translate-x-0",
        toast.open ? "pointer-events-auto" : "pointer-events-none"
      )}
      style={{ bottom: "calc(1rem + env(safe-area-inset-bottom))" }}
      aria-live={isError ? "assertive" : "polite"}
      aria-atomic="true"
    >
      <div
        role={isError ? "alert" : "status"}
        className={cn(
          "relative",
          "w-[calc(100vw-2rem)] sm:w-[22rem] max-w-[28rem]",
          "rounded-2xl border shadow-lg px-4 py-3 text-sm",
          "bg-white text-slate-900 dark:bg-slate-950 dark:text-slate-100 dark:border-slate-800",
          "transition-all duration-200",
          toast.open ? "opacity-100 translate-y-0" : "opacity-0 translate-y-2",
          isSuccess && "border-emerald-200 dark:border-emerald-900/40",
          isError && "border-red-200 dark:border-red-900/40"
        )}
      >
        <div className="pr-10 break-words">{toast.text}</div>

        <button
          type="button"
          onClick={onClose}
          className={cn(
            "absolute right-2 top-1/2 -translate-y-1/2",
            "w-9 h-9 rounded-xl inline-flex items-center justify-center",
            "text-slate-500 hover:text-slate-900 hover:bg-slate-100 transition",
            "dark:text-slate-400 dark:hover:text-slate-100 dark:hover:bg-slate-900/60"
          )}
          aria-label="Close"
        >
          ✕
        </button>
      </div>
    </div>
  );
}

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toast, setToast] = useState<ToastState>({
    open: false,
    text: "",
    kind: "info",
  });

  const timerRef = useRef<number | null>(null);

  useEffect(() => {
    return () => {
      if (timerRef.current) window.clearTimeout(timerRef.current);
    };
  }, []);

  const hideToast = useCallback(() => {
    if (timerRef.current) window.clearTimeout(timerRef.current);
    timerRef.current = null;
    setToast((p) => ({ ...p, open: false }));
  }, []);

  const showToast = useCallback((text: string, kind: ToastKind = "info", ms?: number) => {
    if (timerRef.current) window.clearTimeout(timerRef.current);

    const duration = typeof ms === "number" ? ms : kind === "error" ? 2600 : 1600;

    setToast({ open: true, text, kind });

    timerRef.current = window.setTimeout(() => {
      setToast((p) => ({ ...p, open: false }));
      timerRef.current = null;
    }, duration);
  }, []);

  return (
    <ToastContext.Provider value={{ showToast, hideToast }}>
      <ToastView toast={toast} onClose={hideToast} />
      {children}
    </ToastContext.Provider>
  );
}

export function useAppToast(): ToastApi {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error("useAppToast must be used within <ToastProvider />");
  return ctx;
}

export default ToastView;
