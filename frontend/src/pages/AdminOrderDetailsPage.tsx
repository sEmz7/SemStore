import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAdminOrderDetails } from "../hooks/useAdminOrderDetails";
import StatusBadge from "../components/StatusBadge";
import ConfirmDialog from "../components/ConfirmDialog";

export function AdminOrderDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const { t, i18n } = useTranslation();
  const {
    order,
    loading,
    error,
    setError,
    priceInputs,
    setPriceInput,
    savingPriceId,
    saveItemPrice,
    submitting,
    handleSubmit,
    canSubmit,
    allPricesSet,
    reload,
  } = useAdminOrderDetails(id, t);

  const [confirmSubmitOpen, setConfirmSubmitOpen] = useState(false);

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

  if (!id) return null;

  const title = order ? (order.name?.trim() || id) : "";
  const status = order?.status ?? "";
  const statusLabel = status ? t(`orderStatus.${status}` as any, { defaultValue: status }) : "";
  const isInCheck = order?.status === "IN_CHECK";

  return (
    <div className="space-y-6">
      {/* breadcrumb + header */}
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
        <div className="min-w-0">
          <div className="text-sm text-slate-500 dark:text-slate-400">
            <Link to="/admin" className="hover:underline">
              {t("admin.title")}
            </Link>
            <span className="mx-1">/</span>
            <span className="break-words">{title}</span>
          </div>

          <div className="mt-2 flex flex-wrap items-center gap-3">
            <h1 className="text-xl sm:text-2xl font-semibold break-words">{title}</h1>
            {status && <StatusBadge status={status} label={statusLabel} />}
          </div>

          {order?.createdDate && (
            <div className="mt-1 text-sm text-slate-500 dark:text-slate-400">
              {formatDate(order.createdDate)}
            </div>
          )}

          {order?.totalPrice != null && (
            <div className="mt-1 text-sm font-semibold">
              {t("admin.totalPrice")}: {order.totalPrice}
            </div>
          )}

          <div className="mt-1 text-xs text-slate-500 dark:text-slate-400 break-all">
            {t("admin.userId")}: {order?.userId}
          </div>
        </div>

        <div className="flex gap-2 shrink-0">
          {isInCheck && (
            <button
              disabled={!canSubmit}
              onClick={() => {
                if (!allPricesSet) {
                  setError(t("admin.allPricesRequired"));
                  return;
                }
                setConfirmSubmitOpen(true);
              }}
              className="w-full sm:w-auto px-6 py-3 rounded-xl text-base font-semibold transition
                         bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50
                         dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
            >
              {submitting ? t("admin.submitting") : t("admin.submitOrder")}
            </button>
          )}
          <button
            disabled={loading || submitting}
            onClick={reload}
            className="w-full sm:w-auto px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                       disabled:opacity-50
                       dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
          >
            {loading ? t("common.loading") : t("common.refresh")}
          </button>
        </div>
      </div>

      {error && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                        dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200 break-words">
          {error}
        </div>
      )}

      {/* items */}
      <div className="rounded-3xl border bg-white overflow-hidden dark:bg-slate-950 dark:border-slate-800">
        <div className="px-4 sm:px-6 py-4 border-b dark:border-slate-800">
          <div className="text-sm font-semibold">{t("order.items")}</div>
        </div>

        {!order || order.items.length === 0 ? (
          <div className="p-6 text-sm text-slate-500 dark:text-slate-400">
            {t("order.noItems")}
          </div>
        ) : (
          <ul className="divide-y dark:divide-slate-800">
            {order.items.map((item) => {
              const isSaving = savingPriceId === item.id;
              const inputVal = priceInputs[item.id] ?? (item.price != null ? String(item.price) : "");
              const parsedPrice = parseFloat(inputVal);
              const priceValid = inputVal.trim() !== "" && !isNaN(parsedPrice) && parsedPrice > 0;

              return (
                <li
                  key={item.id}
                  className="p-4 sm:p-6"
                >
                  <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
                    <div className="min-w-0 space-y-1">
                      <div className="text-sm font-semibold break-all">{item.link}</div>
                      <div className="text-xs text-slate-500 dark:text-slate-400 flex flex-wrap gap-x-3 gap-y-1">
                        <span>
                          {t("order.size")}: <span className="font-medium">{item.size}</span>
                        </span>
                        <span>
                          {t("order.configuration")}:{" "}
                          <span className="font-medium">{item.configuration}</span>
                        </span>
                        {item.price != null && (
                          <span>
                            {t("order.price")}: <span className="font-medium text-emerald-700 dark:text-emerald-400">{item.price}</span>
                          </span>
                        )}
                      </div>
                    </div>

                    {isInCheck && (
                      <div className="flex items-center gap-2 shrink-0">
                        <input
                          type="number"
                          min="0.01"
                          step="0.01"
                          placeholder={t("admin.setPrice")}
                          value={inputVal}
                          onChange={(e) => setPriceInput(item.id, e.target.value)}
                          className="w-28 px-3 py-2 rounded-xl text-sm border bg-white dark:bg-slate-950 dark:border-slate-800 dark:text-slate-100
                                     focus:outline-none focus:ring-2 focus:ring-slate-400"
                        />
                        <button
                          disabled={!priceValid || isSaving}
                          onClick={() => saveItemPrice(item.id)}
                          className="px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                                     disabled:opacity-50
                                     dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
                        >
                          {isSaving ? t("admin.savingPrice") : t("admin.savePrice")}
                        </button>
                      </div>
                    )}
                  </div>
                </li>
              );
            })}
          </ul>
        )}
      </div>

      <ConfirmDialog
        open={confirmSubmitOpen}
        title={t("admin.submitDialogTitle")}
        description={t("admin.submitDialogDescription")}
        confirmText={t("admin.submitOrder")}
        cancelText={t("common.cancel")}
        loading={submitting}
        onClose={() => setConfirmSubmitOpen(false)}
        onConfirm={async () => {
          setConfirmSubmitOpen(false);
          await handleSubmit();
        }}
      />
    </div>
  );
}

export default AdminOrderDetailsPage;
