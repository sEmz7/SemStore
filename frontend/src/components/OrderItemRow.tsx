// src/components/OrderItemRow.tsx
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import type { OrderItem } from "../api/types";
import { cn } from "../utils/cn";

type Props = {
  orderId: string;
  item: OrderItem;
  isDeleting?: boolean;

  onError(message: string): void;
  onConfirmDelete(itemId: string): void;
};

export default function OrderItemRow({ orderId, item, isDeleting, onError, onConfirmDelete }: Props) {
  const { t } = useTranslation();

  const itemId = (item as any)?.id as string | undefined;
  const canOpen = typeof itemId === "string" && itemId.length > 0;

  const price = (item as any)?.price;
  const hasPrice = typeof price !== "undefined" && price !== null;

  return (
    <li className="p-4 sm:p-6 hover:bg-slate-50 transition dark:hover:bg-slate-900/40">
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
        <div className="min-w-0">
          <div className="text-sm font-semibold break-all">{itemId ?? "(no id)"}</div>

          <div className="mt-1 text-sm text-slate-700 dark:text-slate-200 break-all">
            {(item as any)?.link}
          </div>

          <div className="mt-2 text-xs text-slate-500 dark:text-slate-400 flex flex-wrap gap-x-3 gap-y-1">
            <span>
              {t("order.size")}: <span className="font-medium">{(item as any)?.size}</span>
            </span>

            <span>
              {t("order.configuration")}:{" "}
              <span className="font-medium">{(item as any)?.configuration}</span>
            </span>

            {hasPrice && (
              <span>
                {t("order.price")}: <span className="font-medium">{price}</span>
              </span>
            )}
          </div>
        </div>

        <div className="grid grid-cols-2 sm:flex gap-2 shrink-0">
          <Link
            to={canOpen ? `/orders/${orderId}/items/${itemId}` : "#"}
            onClick={(e) => {
              if (!canOpen) {
                e.preventDefault();
                onError(t("errors.noItemIdOpen"));
              }
            }}
            className={cn(
              "w-full sm:w-auto px-3 py-2 rounded-xl text-sm border bg-white hover:bg-slate-50 transition text-center",
              "dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60",
              !canOpen && "opacity-50"
            )}
          >
            {t("common.open")}
          </Link>

          <button
            disabled={!canOpen || !!isDeleting}
            onClick={() => {
              if (!canOpen) {
                onError(t("errors.noItemIdDelete"));
                return;
              }
              onConfirmDelete(itemId!);
            }}
            className="w-full sm:w-auto px-3 py-2 rounded-xl text-sm font-medium border transition disabled:opacity-50
                       border-red-200 text-red-700 hover:bg-red-50
                       dark:border-red-900/50 dark:text-red-300 dark:hover:bg-red-950/40"
          >
            {isDeleting ? t("common.deleting") : t("common.delete")}
          </button>
        </div>
      </div>
    </li>
  );
}
