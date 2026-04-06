// src/components/OrderStatusTracker.tsx
import type { ReactNode } from "react";
import { useTranslation } from "react-i18next";
import type { OrderStatus } from "../api/types";
import { cn } from "../utils/cn";

type StepState = "done" | "active" | "pending";

type Step = {
  status: OrderStatus;
  labelKey: string;
  icon: ReactNode;
};

const STEPS: Step[] = [
  {
    status: "CREATED",
    labelKey: "orderStatus.CREATED",
    icon: (
      <>
        <line x1="12" y1="5" x2="12" y2="19" />
        <line x1="5" y1="12" x2="19" y2="12" />
      </>
    ),
  },
  {
    status: "IN_CHECK",
    labelKey: "orderStatus.IN_CHECK",
    icon: (
      <>
        <circle cx="11" cy="11" r="7" />
        <line x1="16.5" y1="16.5" x2="22" y2="22" />
      </>
    ),
  },
  {
    status: "AWAITING_PAYMENT",
    labelKey: "orderStatus.AWAITING_PAYMENT",
    icon: (
      <>
        <rect x="2" y="6" width="20" height="14" rx="3" />
        <path d="M16 13h2" />
        <path d="M2 10h20" />
        <path d="M6 6V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v2" />
      </>
    ),
  },
  {
    status: "PAID",
    labelKey: "orderStatus.PAID",
    icon: (
      <>
        <circle cx="12" cy="12" r="9" />
        <polyline points="8.5 12.5 11 15 15.5 9.5" />
      </>
    ),
  },
  {
    status: "DELIVERING",
    labelKey: "orderStatus.DELIVERING",
    icon: (
      <>
        <rect x="1" y="7" width="15" height="11" rx="1" />
        <path d="M16 11h3l3 3v4h-6V11z" />
        <circle cx="5.5" cy="18.5" r="1.5" />
        <circle cx="18.5" cy="18.5" r="1.5" />
      </>
    ),
  },
  {
    status: "COMPLETED",
    labelKey: "orderStatus.COMPLETED",
    icon: <polyline points="4 12 9 17 20 7" />,
  },
];

const FLOW = STEPS.map((s) => s.status);

const circleClass: Record<StepState, string> = {
  done: "bg-slate-900 border-slate-900 dark:bg-white dark:border-white",
  active:
    "bg-white border-slate-900 ring-4 ring-slate-900/10 dark:bg-slate-950 dark:border-white dark:ring-white/10",
  pending:
    "bg-white border-slate-200 dark:bg-slate-950 dark:border-slate-700",
};

const iconClass: Record<StepState, string> = {
  done: "stroke-white dark:stroke-slate-900",
  active: "stroke-slate-900 dark:stroke-white",
  pending: "stroke-slate-300 dark:stroke-slate-600",
};

const lineClass: Record<"done" | "pending", string> = {
  done: "bg-slate-900 dark:bg-white",
  pending: "bg-slate-200 dark:bg-slate-700",
};

const labelClass: Record<StepState, string> = {
  done: "text-slate-700 dark:text-slate-300",
  active: "font-bold text-slate-900 dark:text-slate-50",
  pending: "text-slate-400 dark:text-slate-600",
};

export default function OrderStatusTracker({ status }: { status: OrderStatus }) {
  const { t } = useTranslation();

  const currentIndex = FLOW.indexOf(status);
  if (currentIndex === -1) return null;

  return (
    <div className="flex overflow-x-auto py-1">
      {STEPS.map((step, i) => {
        const state: StepState =
          i < currentIndex ? "done" : i === currentIndex ? "active" : "pending";
        const isLast = i === STEPS.length - 1;

        return (
          <div
            key={step.status}
            className="flex flex-col items-center flex-1 min-w-[72px]"
          >
            <div className="flex items-center w-full">
              <div
                className={cn(
                  "w-12 h-12 rounded-full border-2 flex items-center justify-center flex-shrink-0 z-10",
                  circleClass[state]
                )}
              >
                <svg
                  aria-hidden="true"
                  className={cn("w-5 h-5", iconClass[state])}
                  viewBox="0 0 24 24"
                  fill="none"
                  strokeWidth="1.8"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  {step.icon}
                </svg>
              </div>
              {!isLast && (
                <div
                  className={cn(
                    "flex-1 h-0.5 min-w-3",
                    lineClass[state === "done" ? "done" : "pending"]
                  )}
                />
              )}
            </div>
            <span
              className={cn(
                "mt-2 text-[11px] text-center max-w-[76px] leading-tight font-medium",
                labelClass[state]
              )}
            >
              {t(step.labelKey)}
            </span>
          </div>
        );
      })}
    </div>
  );
}
