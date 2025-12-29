// src/pages/OrderDetailsPage.tsx
import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { addOrderItem, deleteOrderItem, getOrderById } from "../api/orders";
import type { OrderDto, OrderItem } from "../api/types";
import { useTranslation } from "react-i18next";

function cn(...a: Array<string | false | null | undefined>) {
  return a.filter(Boolean).join(" ");
}

const LINK_MIN = 2;
const LINK_MAX = 50;
const SIZE_MIN = 2;
const SIZE_MAX = 50;
const CONF_MIN = 2;
const CONF_MAX = 50;

function lenBetween(v: string, min: number, max: number) {
  const s = v.trim();
  return s.length >= min && s.length <= max;
}

function parseSpringValidationMessage(msg: string) {
  // field: "on field 'link'"
  const fieldMatch = msg.match(/on field '([^']+)'/);
  const field = fieldMatch?.[1];

  // ru: "от 2 до 50"
  const ruRange = msg.match(/от\s+(\d+)\s+до\s+(\d+)/i);
  if (ruRange) {
    return { field, min: Number(ruRange[1]), max: Number(ruRange[2]) };
  }

  // en: "between 2 and 50"
  const enRange = msg.match(/between\s+(\d+)\s+and\s+(\d+)/i);
  if (enRange) {
    return { field, min: Number(enRange[1]), max: Number(enRange[2]) };
  }

  // fallback: "...,50,2" (max,min) sometimes appears
  const nums = msg.match(/,(\d+),(\d+)\]/);
  if (nums) {
    const a = Number(nums[2]);
    const b = Number(nums[1]);
    if (Number.isFinite(a) && Number.isFinite(b)) return { field, min: a, max: b };
  }

  return null;
}

function fieldKeyBySpringField(field?: string) {
  if (!field) return null;
  if (field === "link") return "order.link";
  if (field === "size") return "order.size";
  if (field === "configuration") return "order.configuration";
  return null;
}

function StatusBadge({
  status,
  label,
}: {
  status: string;
  label: string;
}) {
  const cls =
    status === "PAID"
      ? "border-emerald-200 bg-emerald-50 text-emerald-800 dark:border-emerald-900/40 dark:bg-emerald-950/40 dark:text-emerald-200"
      : status === "CANCELED"
      ? "border-red-200 bg-red-50 text-red-700 dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200"
      : status === "ORDERED"
      ? "border-indigo-200 bg-indigo-50 text-indigo-800 dark:border-indigo-900/40 dark:bg-indigo-950/40 dark:text-indigo-200"
      : "border-slate-200 bg-slate-50 text-slate-800 dark:border-slate-800 dark:bg-slate-900/40 dark:text-slate-100";

  return (
    <span className={cn("inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold", cls)}>
      {label}
    </span>
  );
}

export function OrderDetailsPage() {
  const { t } = useTranslation();
  const { id } = useParams<{ id: string }>();

  const [order, setOrder] = useState<OrderDto | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const [link, setLink] = useState("");
  const [size, setSize] = useState("");
  const [configuration, setConfiguration] = useState("");
  const [saving, setSaving] = useState(false);

  const [touched, setTouched] = useState({
    link: false,
    size: false,
    configuration: false,
  });

  const [deletingItemId, setDeletingItemId] = useState<string | null>(null);

  const items = useMemo<OrderItem[]>(() => (order?.items ?? []) as OrderItem[], [order]);

  async function reload() {
    if (!id) return;
    setLoading(true);
    try {
      setErr(null);
      setOrder(await getOrderById(id));
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? t("errors.loadOrderFail"));
    } finally {
      setLoading(false);
    }
  }

  function fieldLenMsg(fieldKey: string, min: number, max: number) {
    return t("errors.fieldLengthBetween", { field: t(fieldKey), min, max });
  }

  const linkErr =
    !link.trim()
      ? t("errors.required")
      : !lenBetween(link, LINK_MIN, LINK_MAX)
      ? fieldLenMsg("order.link", LINK_MIN, LINK_MAX)
      : null;

  const sizeErr =
    !size.trim()
      ? t("errors.required")
      : !lenBetween(size, SIZE_MIN, SIZE_MAX)
      ? fieldLenMsg("order.size", SIZE_MIN, SIZE_MAX)
      : null;

  const confErr =
    !configuration.trim()
      ? t("errors.required")
      : !lenBetween(configuration, CONF_MIN, CONF_MAX)
      ? fieldLenMsg("order.configuration", CONF_MIN, CONF_MAX)
      : null;

  const canSubmit = !linkErr && !sizeErr && !confErr && !saving;

  function formatApiError(e: any) {
    const msg = e?.response?.data?.message;
    if (typeof msg !== "string" || !msg.trim()) return t("errors.addItemFail");

    // try to parse spring validation and show a clean localized message
    const parsed = parseSpringValidationMessage(msg);
    if (parsed?.min && parsed?.max) {
      const k = fieldKeyBySpringField(parsed.field);
      if (k) return fieldLenMsg(k, parsed.min, parsed.max);
    }

    return msg; // fallback (e.g. other backend errors)
  }

  useEffect(() => {
    reload();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  if (!id) return null;

  const title = order?.name?.trim() ? order!.name : id;

  const status = order?.status ?? "";
  const statusLabel = status ? t(`orderStatus.${status}`, { defaultValue: status }) : "";

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4">
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

        <button
          onClick={reload}
          className="px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                     dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
        >
          {loading ? t("common.loading") : t("common.refresh")}
        </button>
      </div>

      {err && (
        <div
          className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                        dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200"
        >
          {err}
        </div>
      )}

      {/* Add item */}
      <div className="rounded-3xl border bg-white p-6 shadow-sm dark:bg-slate-950 dark:border-slate-800">
        <div className="flex items-center justify-between gap-3">
          <div>
            <div className="text-sm font-semibold">{t("order.addItem")}</div>
            <div className="text-xs text-slate-500 dark:text-slate-400">{t("order.hint")}</div>
          </div>
        </div>

        <div className="mt-4 grid gap-3 sm:grid-cols-3">
          {/* LINK */}
          <div className="sm:col-span-2">
            <input
              value={link}
              maxLength={LINK_MAX}
              onChange={(e) => setLink(e.target.value)}
              onBlur={() => setTouched((p) => ({ ...p, link: true }))}
              placeholder="https://example.com/product/1"
              className={cn(
                "w-full rounded-xl border px-3 py-2 text-sm outline-none bg-white dark:bg-slate-950 dark:border-slate-800",
                "focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800",
                touched.link && linkErr && "border-red-300 focus:ring-red-100 dark:border-red-900/60 dark:focus:ring-red-900/30"
              )}
            />
            {touched.link && linkErr && (
              <div className="mt-1 text-xs text-red-600 dark:text-red-300">{linkErr}</div>
            )}
          </div>

          {/* SIZE */}
          <div>
            <input
              value={size}
              maxLength={SIZE_MAX}
              onChange={(e) => setSize(e.target.value)}
              onBlur={() => setTouched((p) => ({ ...p, size: true }))}
              placeholder="42.5"
              className={cn(
                "w-full rounded-xl border px-3 py-2 text-sm outline-none bg-white dark:bg-slate-950 dark:border-slate-800",
                "focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800",
                touched.size && sizeErr && "border-red-300 focus:ring-red-100 dark:border-red-900/60 dark:focus:ring-red-900/30"
              )}
            />
            {touched.size && sizeErr && (
              <div className="mt-1 text-xs text-red-600 dark:text-red-300">{sizeErr}</div>
            )}
          </div>

          {/* CONFIG */}
          <div className="sm:col-span-3">
            <input
              value={configuration}
              maxLength={CONF_MAX}
              onChange={(e) => setConfiguration(e.target.value)}
              onBlur={() => setTouched((p) => ({ ...p, configuration: true }))}
              placeholder="black"
              className={cn(
                "w-full rounded-xl border px-3 py-2 text-sm outline-none bg-white dark:bg-slate-950 dark:border-slate-800",
                "focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800",
                touched.configuration && confErr && "border-red-300 focus:ring-red-100 dark:border-red-900/60 dark:focus:ring-red-900/30"
              )}
            />
            {touched.configuration && confErr && (
              <div className="mt-1 text-xs text-red-600 dark:text-red-300">{confErr}</div>
            )}
          </div>

          <div className="sm:col-span-3 flex justify-end">
            <button
              disabled={!canSubmit}
              onClick={async () => {
                setErr(null);

                // mark touched to show errors
                setTouched({ link: true, size: true, configuration: true });

                if (!canSubmit) {
                  setErr(t("errors.fixForm"));
                  return;
                }

                setSaving(true);
                try {
                  await addOrderItem(id, {
                    link: link.trim(),
                    size: size.trim(),
                    configuration: configuration.trim(),
                  });

                  setLink("");
                  setSize("");
                  setConfiguration("");
                  setTouched({ link: false, size: false, configuration: false });

                  await reload();
                } catch (e: any) {
                  setErr(formatApiError(e));
                } finally {
                  setSaving(false);
                }
              }}
              className="px-4 py-2 rounded-xl text-sm font-semibold
                         bg-slate-900 text-white hover:bg-slate-800 transition
                         disabled:opacity-50 disabled:hover:bg-slate-900
                         dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
            >
              {saving ? t("common.saving") : t("order.addItem")}
            </button>
          </div>
        </div>
      </div>

      {/* Items list */}
      <div className="rounded-3xl border bg-white overflow-hidden dark:bg-slate-950 dark:border-slate-800">
        <div className="px-6 py-4 border-b flex items-center justify-between dark:border-slate-800">
          <div className="text-sm font-semibold">{t("order.items")}</div>
          <div className="text-xs text-slate-500 dark:text-slate-400">
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
                <li key={itemId ?? `idx-${idx}`} className="p-6 hover:bg-slate-50 transition dark:hover:bg-slate-900/40">
                  <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
                    <div className="min-w-0">
                      <div className="text-sm font-semibold break-all">{itemId ?? "(no id)"}</div>

                      <div className="mt-1 text-sm text-slate-700 dark:text-slate-200 break-all">{it.link}</div>

                      <div className="mt-2 text-xs text-slate-500 dark:text-slate-400">
                        {t("order.size")}: <span className="font-medium">{it.size}</span> •{" "}
                        {t("order.configuration")}: <span className="font-medium">{it.configuration}</span>
                        {typeof (it as any).price !== "undefined" && (
                          <>
                            {" "}
                            • {t("order.price")}: <span className="font-medium">{(it as any).price}</span>
                          </>
                        )}
                      </div>
                    </div>

                    <div className="flex gap-2 shrink-0">
                      <Link
                        to={canOpen ? `/orders/${id}/items/${itemId}` : "#"}
                        onClick={(e) => {
                          if (!canOpen) {
                            e.preventDefault();
                            setErr(t("errors.noItemIdOpen"));
                          }
                        }}
                        className={cn(
                          "px-3 py-2 rounded-xl text-sm border bg-white hover:bg-slate-50 transition",
                          "dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60",
                          !canOpen && "opacity-50 pointer-events-none"
                        )}
                      >
                        {t("common.open")}
                      </Link>

                      <button
                        onClick={async () => {
                          if (!canOpen) {
                            setErr(t("errors.noItemIdDelete"));
                            return;
                          }
                          setDeletingItemId(itemId!);
                          try {
                            setErr(null);
                            await deleteOrderItem(id, itemId!);
                            await reload();
                          } catch (e: any) {
                            setErr(e?.response?.data?.message ?? t("errors.deleteFail"));
                          } finally {
                            setDeletingItemId(null);
                          }
                        }}
                        disabled={!canOpen || isDeleting}
                        className="px-3 py-2 rounded-xl text-sm border bg-white hover:bg-slate-50 transition
                                   disabled:opacity-50
                                   dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
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
    </div>
  );
}
