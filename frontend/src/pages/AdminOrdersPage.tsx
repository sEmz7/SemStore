import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import { useAdminOrders } from "../hooks/useAdminOrders";
import StatusBadge from "../components/StatusBadge";
import type { OrderStatus } from "../api/types";

const STATUS_OPTIONS: Array<{ value: OrderStatus | ""; labelKey: string }> = [
  { value: "", labelKey: "admin.allStatuses" },
  { value: "IN_CHECK", labelKey: "orderStatus.IN_CHECK" },
  { value: "AWAITING_PAYMENT", labelKey: "orderStatus.AWAITING_PAYMENT" },
  { value: "CREATED", labelKey: "orderStatus.CREATED" },
  { value: "PAID", labelKey: "orderStatus.PAID" },
  { value: "CANCELED", labelKey: "orderStatus.CANCELED" },
  { value: "DELIVERING", labelKey: "orderStatus.DELIVERING" },
  { value: "COMPLETED", labelKey: "orderStatus.COMPLETED" },
];

export function AdminOrdersPage() {
  const { t, i18n } = useTranslation();
  const {
    orders,
    page,
    totalPages,
    statusFilter,
    loading,
    error,
    changePage,
    changeStatus,
    reload,
  } = useAdminOrders(t);

  function formatDate(dateStr: string): string {
    try {
      return new Intl.DateTimeFormat(i18n.language, {
        day: "numeric",
        month: "long",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      }).format(new Date(dateStr));
    } catch {
      return dateStr;
    }
  }

  return (
    <div className="space-y-6">
      {/* header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <h1 className="text-xl sm:text-2xl font-semibold">{t("admin.title")}</h1>
        </div>
        <div className="flex items-center gap-2">
          {/* status filter */}
          <select
            value={statusFilter}
            onChange={(e) => changeStatus(e.target.value as OrderStatus | "")}
            className="px-3 py-2 rounded-xl text-sm border bg-white dark:bg-slate-950 dark:border-slate-800 dark:text-slate-100"
          >
            {STATUS_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {t(opt.labelKey as any)}
              </option>
            ))}
          </select>

          <button
            disabled={loading}
            onClick={reload}
            className="px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                       disabled:opacity-50
                       dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
          >
            {loading ? t("common.loading") : t("common.refresh")}
          </button>
        </div>
      </div>

      {error && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                        dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200">
          {error}
        </div>
      )}

      {/* orders table */}
      <div className="rounded-3xl border bg-white overflow-hidden dark:bg-slate-950 dark:border-slate-800">
        {orders.length === 0 && !loading ? (
          <div className="p-6 text-sm text-slate-500 dark:text-slate-400">
            {t("admin.noOrders")}
          </div>
        ) : (
          <ul className="divide-y dark:divide-slate-800">
            {orders.map((order) => {
              const statusLabel = t(`orderStatus.${order.status}` as any, {
                defaultValue: order.status,
              });
              return (
                <li
                  key={order.id}
                  className="p-4 sm:p-6 hover:bg-slate-50 transition dark:hover:bg-slate-900/40"
                >
                  <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                    <div className="min-w-0 space-y-1">
                      <div className="text-sm font-semibold break-words">{order.name}</div>
                      <div className="text-xs text-slate-500 dark:text-slate-400 break-all">
                        {t("admin.userId")}: {order.userId}
                      </div>
                      <div className="flex items-center gap-2 flex-wrap">
                        <StatusBadge status={order.status} label={statusLabel} />
                        <span className="text-xs text-slate-400 dark:text-slate-500">
                          {formatDate(order.createdDate)}
                        </span>
                      </div>
                    </div>
                    <Link
                      to={`/admin/orders/${order.id}`}
                      className="shrink-0 w-full sm:w-auto text-center px-4 py-2 rounded-xl text-sm font-medium border
                                 bg-white hover:bg-slate-50 transition
                                 dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
                    >
                      {t("common.open")}
                    </Link>
                  </div>
                </li>
              );
            })}
          </ul>
        )}
      </div>

      {/* pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2">
          <button
            disabled={page === 0 || loading}
            onClick={() => changePage(page - 1)}
            className="px-3 py-2 rounded-xl text-sm border bg-white hover:bg-slate-50 transition
                       disabled:opacity-50
                       dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
          >
            {t("common.back")}
          </button>
          <span className="text-sm text-slate-500 dark:text-slate-400">
            {page + 1} / {totalPages}
          </span>
          <button
            disabled={page >= totalPages - 1 || loading}
            onClick={() => changePage(page + 1)}
            className="px-3 py-2 rounded-xl text-sm border bg-white hover:bg-slate-50 transition
                       disabled:opacity-50
                       dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
          >
            {t("common.next")}
          </button>
        </div>
      )}
    </div>
  );
}

export default AdminOrdersPage;
