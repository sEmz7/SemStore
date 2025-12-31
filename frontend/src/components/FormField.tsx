import type {
  FocusEventHandler,
  HTMLInputTypeAttribute,
  InputHTMLAttributes,
} from "react";
import { cn } from "../utils/cn";

type InputMode = InputHTMLAttributes<HTMLInputElement>["inputMode"];

export default function FormField({
  label,
  value,
  onChange,
  placeholder,
  maxLength,
  error,
  type = "text",
  inputMode,
  autoComplete,
  disabled,
  onBlur,
  onFocus,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;

  placeholder?: string;
  maxLength?: number;
  error?: string | null;

  type?: HTMLInputTypeAttribute;
  inputMode?: InputMode;
  autoComplete?: string;

  disabled?: boolean;
  onBlur?: FocusEventHandler<HTMLInputElement>;
  onFocus?: FocusEventHandler<HTMLInputElement>;
}) {
  return (
    <label className="grid gap-1">
      <span className="text-xs font-medium text-slate-600 dark:text-slate-300">
        {label}
      </span>

      <input
        type={type}
        value={value}
        placeholder={placeholder ?? label}
        maxLength={maxLength}
        inputMode={inputMode}
        autoComplete={autoComplete}
        disabled={disabled}
        onBlur={onBlur}
        onFocus={onFocus}
        onChange={(e) => onChange(e.target.value)}
        className={cn(
          "w-full rounded-xl border px-3 py-2 text-sm outline-none",
          "bg-white dark:bg-slate-950 dark:border-slate-800",
          "focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800",
          disabled && "opacity-50 cursor-not-allowed",
          error &&
            "border-red-300 focus:ring-red-200 dark:border-red-900/50 dark:focus:ring-red-950/40"
        )}
      />

      {!!error && (
        <div className="text-xs text-red-600 dark:text-red-300 break-words">
          {error}
        </div>
      )}
    </label>
  );
}
