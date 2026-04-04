import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { addOrderItem, confirmOrder, deleteOrderItem, getOrderById } from "../api/orders";
import type { OrderDto, OrderItem } from "../api/types";
import { useTranslation } from "react-i18next";
import ConfirmDialog from "../components/ConfirmDialog";
import FormField from "../components/FormField";
import StatusBadge from "../components/StatusBadge";
import { localizeBackendLengthError } from "../utils/springValidation";
import { cn } from "../utils/cn";

type FieldKey = "link" | "size" | "configuration";

const LIMITS: Record<FieldKey, { min: number; max: number; labelKey: string; placeholder: string }> = {
  link: { min: 2, max: 50, labelKey: "order.link", placeholder: "https://example.com/product/1" },
  size: { min: 2, max: 30, labelKey: "order.size", placeholder: "42.5" },
  configuration: { min: 2, max: 255, labelKey: "order.configuration", placeholder: "black" },
};

function validateLen(v: string, min: number, max: number) {
  const s = v.trim();
  if (!s) return { ok: false as const, kind: "required" as const };
  if (s.length < min) return { ok: false as const, kind: "min" as const };
  if (s.length > max) return { ok: false as const, kind: "max" as const };
  return { ok: true as const };
}

export function OrderDetailsPage() {
  const { t } = useTranslation();
  const { id } = useParams<{ id: string }>();

  const [order, setOrder] = useState<OrderDto | null>(null);
  const [pageError, setPageError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const [form, setForm] = useState<Record<FieldKey, string>>({
    link: "",
    size: "",
    configuration: "",
  });

  const [touched, setTouched] = useState<Record<FieldKey, boolean>>({
    link: false,
    size: false,
    configuration: false,
  });

  const [serverErrors, setServerErrors] = useState<Partial<Record<FieldKey, string>>>({});
  const [saving, setSaving] = useState(false);
  const [confirming, setConfirming] = useState(false);

  const [deletingItemId, setDeletingItemId] = useState<string | null>(null);
  const [confirmDelete, setConfirmDelete] = useState<{ itemId: string } | null>(null);

  const labels = useMemo<Record<FieldKey, string>>(
    () => ({
      link: t("order.link"),
      size: t("order.size"),
      configuration: t("order.configuration"),
    }),
    [t]
  );

  const items = useMemo<OrderItem[]>(() => (order?.items ?? []) as OrderItem[], [order]);

  const isEditable = order?.status === "CREATED";

  function setField(key: FieldKey, value: string) {
    setForm((p) => ({ ...p, [key]: value }));
    if (touched[key]) setServerErrors((p) => ({ ...p, [key]: undefined }));
  }

  function touchField(key: FieldKey) {
    setTouched((p) => ({ ...p, [key]: true }));
  }

  function touchAll() {
    setTouched({ link: true, size: true, configuration: true });
  }

  const clientErrors = useMemo(() => {
    const out: Partial<Record<FieldKey, string>> = {};
    (Object.keys(LIMITS) as FieldKey[]).forEach((k) => {
      const lim = LIMITS[k];
      const v = validateLen(form[k], lim.min, lim.max);
      if (!v.ok) {
        out[k] =
          v.kind === "required"
            ? t("errors.required")
            : t("errors.fieldLengthBetween", { field: labels[k], min: lim.min, max: lim.max });
      }
    });
    return out;
  }, [form, labels, t]);

  const isValid = !clientErrors.link && !clientErrors.size && !clientErrors.configuration;
  const canSubmit = isValid && !saving;

  function shownError(key: FieldKey) {
    return serverErrors[key] ?? (touched[key] ? clientErrors[key] : null);
  }

  async function reload() {
    if (!id) return;
    setLoading(true);
    try {
      setPageError(null);
      setOrder(await getOrderById(id));
    } catch (e: any) {
      setPageError(e?.response?.data?.message ?? t("errors.loadOrderFail"));
    } finally {
      setLoading(false);
    }
  }

  async function handleConfirm() {
    if (!id) return;
    setConfirming(true);
    try {
      setPageError(null);
      await confirmOrder(id);
      await reload();
    } catch (e: any) {
      setPageError(e?.response?.data?.message ?? t("errors.confirmOrderFail"));
    } finally {
      setConfirming(false);
    }
  }

  async function addItem() {
    if (!id) return;

    setPageError(null);
    setServerErrors({});
    touchAll();

    if (!canSubmit) {
      setPageError(t("errors.fixForm"));
      return;
    }

    setSaving(true);
    try {
      await addOrderItem(id, {
        link: form.link.trim(),
        size: form.size.trim(),
        configuration: form.configuration.trim(),
      });

      setForm({ link: "", size: "", configuration: "" });
      setTouched({ link: false, size: false, configuration: false });
      await reload();
    } catch (e: any) {
      const msg: string = e?.response?.data?.message ?? e?.message ?? "";

      const parsed = localizeBackendLengthError<FieldKey>(msg, t, {
        fieldLabels: labels,
        fieldNameMap: { link: "link", size: "size", configuration: "configuration" },
        defaultTextKey: "errors.addItemFail",
      });

      if (parsed.field) {
        setServerErrors((p) => ({ ...p, [parsed.field!]: parsed.text }));
        setPageError(t("errors.fixForm"));
      } else {
        setPageError(parsed.text);
      }
    } finally {
      setSaving(false);
    }
  }

  async function doDeleteItem(itemId: string) {
    if (!id) return;
    setDeletingItemId(itemId);
    try {
      setPageError(null);
      await deleteOrderItem(id, itemId);
      await reload();
    } catch (e: any) {
      setPageError(e?.response?.data?.message ?? t("errors.deleteFail"));
    } finally {
      setDeletingItemId(null);
    }
  }

  useEffect(() => {
    reload().catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  if (!id) return null;

  const title = order?.name?.trim() ? order!.name : id;
  const status = order?.status ?? "";
  const statusLabel = status ? t(`orderStatus.${status}`, { defaultValue: status }) : "";

  return (
    <div className="space-y-6">
      {/* header */}
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
        <div className="min-w-0">
          <div className="text-sm text-slate-500 dark:text-slate-400">
            <Link to="/" className="hover:underline">
              {t("nav.orders")}
            </Link>{" "}
            <span className="mx-1">/</span>
            <span className="break-words">{title}</span>
          </div>

          <div className="mt-2 flex flex-wrap items-center gap-3">
            <h1 className="text-xl sm:text-2xl font-semibold break-words">{title}</h1>
            {status && <StatusBadge status={status} label={statusLabel} />}
          </div>

          <div className="mt-1 text-xs text-slate-500 dark:text-slate-400 break-all">
            {t("orders.id")}: {id}
          </div>

          {order?.createdDate && (
            <div className="mt-1 text-sm text-slate-500 dark:text-slate-400">
              {t("order.created")}: {order.createdDate}
            </div>
          )}
        </div>

        <div className="flex gap-2 w-full sm:w-auto">
          {isEditable && items.length > 0 && (
            <button
              disabled={confirming}
              onClick={handleConfirm}
              className="w-full sm:w-auto px-4 py-2 rounded-xl text-sm font-semibold transition
                         bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50
                         dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
            >
              {confirming ? t("order.confirming") : t("order.confirm")}
            </button>
          )}
          <button
            disabled={loading || confirming}
            onClick={reload}
            className="w-full sm:w-auto px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                       disabled:opacity-50
                       dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
          >
            {loading ? t("common.loading") : t("common.refresh")}
          </button>
        </div>
      </div>

      {pageError && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                        dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200 break-words">
          {pageError}
        </div>
      )}

      {!isEditable && order !== null && (
        <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800
                        dark:border-amber-900/50 dark:bg-amber-950/40 dark:text-amber-300">
          {t("order.editingLocked")}
        </div>
      )}

      {/* add item */}
      <div className="rounded-3xl border bg-white p-4 sm:p-6 shadow-sm dark:bg-slate-950 dark:border-slate-800">
        <div>
          <div className="text-sm font-semibold">{t("order.addItem")}</div>
          <div className="text-xs text-slate-500 dark:text-slate-400">{t("order.hint")}</div>
        </div>

        <div className="mt-4 grid gap-3 sm:grid-cols-3">
          <div className="sm:col-span-2">
            <FormField
              label={labels.link}
              value={form.link}
              onChange={(v) => setField("link", v)}
              onBlur={() => touchField("link")}
              placeholder={LIMITS.link.placeholder}
              maxLength={LIMITS.link.max}
              error={shownError("link")}
              disabled={!isEditable}
            />
          </div>

          <div>
            <FormField
              label={labels.size}
              value={form.size}
              onChange={(v) => setField("size", v)}
              onBlur={() => touchField("size")}
              placeholder={LIMITS.size.placeholder}
              maxLength={LIMITS.size.max}
              error={shownError("size")}
              disabled={!isEditable}
            />
          </div>

          <div className="sm:col-span-3">
            <FormField
              label={labels.configuration}
              value={form.configuration}
              onChange={(v) => setField("configuration", v)}
              onBlur={() => touchField("configuration")}
              placeholder={LIMITS.configuration.placeholder}
              maxLength={LIMITS.configuration.max}
              error={shownError("configuration")}
              disabled={!isEditable}
            />
          </div>

          <div className="sm:col-span-3 flex justify-end">
            <button
              disabled={!canSubmit || !isEditable}
              onClick={addItem}
              className="w-full sm:w-auto px-4 py-2 rounded-xl text-sm font-semibold
                         bg-slate-900 text-white hover:bg-slate-800 transition
                         disabled:opacity-50 disabled:hover:bg-slate-900
                         dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
            >
              {saving ? t("common.saving") : t("order.addItem")}
            </button>
          </div>
        </div>
      </div>

      {/* items */}
      <div className="rounded-3xl border bg-white overflow-hidden dark:bg-slate-950 dark:border-slate-800">
        <div className="px-4 sm:px-6 py-4 border-b flex items-center justify-between dark:border-slate-800 gap-3">
          <div className="text-sm font-semibold">{t("order.items")}</div>
          <div className="text-xs text-slate-500 dark:text-slate-400 shrink-0">
            {t("orders.itemsCount", { count: items.length })}
          </div>
        </div>

        {items.length === 0 ? (
          <div className="p-6 text-sm text-slate-500 dark:text-slate-400">{t("order.noItems")}</div>
        ) : (
          <ul className="divide-y dark:divide-slate-800">
            {items.map((it, idx) => {
              const itemId = it.id;
              const canOpen = typeof itemId === "string" && itemId.length > 0;
              const isDeleting = deletingItemId === itemId;

              return (
                <li
                  key={itemId ?? `idx-${idx}`}
                  className="p-4 sm:p-6 hover:bg-slate-50 transition dark:hover:bg-slate-900/40"
                >
                  <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
                    <div className="min-w-0">
                      <div className="text-sm font-semibold break-all">{itemId ?? "(no id)"}</div>

                      <div className="mt-1 text-sm text-slate-700 dark:text-slate-200 break-all">
                        {it.link}
                      </div>

                      <div className="mt-2 text-xs text-slate-500 dark:text-slate-400 flex flex-wrap gap-x-3 gap-y-1">
                        <span>
                          {t("order.size")}: <span className="font-medium">{it.size}</span>
                        </span>
                        <span>
                          {t("order.configuration")}:{" "}
                          <span className="font-medium">{it.configuration}</span>
                        </span>
                        {typeof (it as any).price !== "undefined" && (
                          <span>
                            {t("order.price")}: <span className="font-medium">{(it as any).price}</span>
                          </span>
                        )}
                      </div>
                    </div>

                    <div className="grid grid-cols-2 sm:flex gap-2 shrink-0">
                      <Link
                        to={canOpen ? `/orders/${id}/items/${itemId}` : "#"}
                        onClick={(e) => {
                          if (!canOpen) {
                            e.preventDefault();
                            setPageError(t("errors.noItemIdOpen"));
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
                        disabled={!canOpen || isDeleting || !isEditable}
                        onClick={() => {
                          if (!canOpen) {
                            setPageError(t("errors.noItemIdDelete"));
                            return;
                          }
                          setConfirmDelete({ itemId: itemId! });
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
            })}
          </ul>
        )}
      </div>

      <ConfirmDialog
        open={!!confirmDelete}
        title={t("common.confirmDeleteTitle")}
        description={confirmDelete ? t("common.confirmDeleteText", { id: confirmDelete.itemId }) : undefined}
        confirmText={t("common.delete")}
        cancelText={t("common.cancel")}
        loading={!!confirmDelete && deletingItemId === confirmDelete.itemId}
        onClose={() => setConfirmDelete(null)}
        onConfirm={async () => {
          if (!confirmDelete) return;
          const itemId = confirmDelete.itemId;
          await doDeleteItem(itemId);
          setConfirmDelete(null);
        }}
      />
    </div>
  );
}

export default OrderDetailsPage;
