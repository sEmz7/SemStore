// src/pages/OrderItemPage.tsx
import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { getOrderItemById, updateOrderItem } from "../api/orders";
import type { OrderItem } from "../api/types";
import { useTranslation } from "react-i18next";

export function OrderItemPage() {
  const { t } = useTranslation();

  const { orderId, itemId } = useParams<{ orderId: string; itemId: string }>();
  const navigate = useNavigate();

  const [item, setItem] = useState<OrderItem | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const [link, setLink] = useState("");
  const [size, setSize] = useState("");
  const [configuration, setConfiguration] = useState("");
  const [saving, setSaving] = useState(false);

  async function load() {
    if (!orderId || !itemId) return;
    setLoading(true);
    try {
      setErr(null);
      const it = await getOrderItemById(orderId, itemId);
      setItem(it);
      setLink((it.link ?? "").toString());
      setSize((it.size ?? "").toString());
      setConfiguration((it.configuration ?? "").toString());
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? t("errors.loadItemFail"));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [orderId, itemId]);

  if (!orderId || !itemId) return null;

  return (
    <div className="space-y-6">
      <div className="text-sm text-slate-500 dark:text-slate-400">
        <Link to="/" className="hover:underline">
          {t("nav.orders")}
        </Link>{" "}
        <span className="mx-1">/</span>
        <Link to={`/orders/${orderId}`} className="hover:underline break-all">
          {orderId}
        </Link>{" "}
        <span className="mx-1">/</span>
        <span className="break-all">{itemId}</span>
      </div>

      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <h1 className="text-xl sm:text-2xl font-semibold break-all">
            {t("item.title", { id: itemId })}
          </h1>
          <div className="mt-1 text-xs text-slate-500 dark:text-slate-400 break-all">
            {t("item.orderId")}: {orderId}
          </div>

          {typeof (item as any)?.price !== "undefined" && (
            <div className="mt-2 text-sm text-slate-700 dark:text-slate-200">
              {t("order.price")}: <span className="font-semibold">{(item as any).price}</span>
            </div>
          )}
        </div>

        <button
          onClick={load}
          className="px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                     dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
        >
          {loading ? t("common.loading") : t("common.refresh")}
        </button>
      </div>

      {err && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                        dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200">
          {err}
        </div>
      )}

      <div className="rounded-3xl border bg-white p-6 shadow-sm dark:bg-slate-950 dark:border-slate-800">
        <div className="text-sm font-semibold">{t("item.editTitle")}</div>
        <div className="mt-1 text-xs text-slate-500 dark:text-slate-400">{t("order.hint")}</div>

        <div className="mt-4 grid gap-3 sm:grid-cols-3">
          <input
            value={link}
            onChange={(e) => setLink(e.target.value)}
            placeholder="link"
            className="rounded-xl border px-3 py-2 text-sm outline-none
                       bg-white dark:bg-slate-950 dark:border-slate-800
                       focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800 sm:col-span-2"
          />
          <input
            value={size}
            onChange={(e) => setSize(e.target.value)}
            placeholder={t("order.size")}
            className="rounded-xl border px-3 py-2 text-sm outline-none
                       bg-white dark:bg-slate-950 dark:border-slate-800
                       focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800"
          />
          <input
            value={configuration}
            onChange={(e) => setConfiguration(e.target.value)}
            placeholder={t("order.configuration")}
            className="rounded-xl border px-3 py-2 text-sm outline-none
                       bg-white dark:bg-slate-950 dark:border-slate-800
                       focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800 sm:col-span-3"
          />

          <div className="sm:col-span-3 flex justify-end gap-2">
            <button
              onClick={() => navigate(`/orders/${orderId}`)}
              className="px-4 py-2 rounded-xl text-sm border bg-white hover:bg-slate-50 transition
                         dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
            >
              {t("common.back")}
            </button>

            <button
              disabled={saving || !link.trim() || !size.trim() || !configuration.trim()}
              onClick={async () => {
                const dto = {
                  link: link.trim(),
                  size: size.trim(),
                  configuration: configuration.trim(),
                };

                setSaving(true);
                try {
                  setErr(null);
                  const updated = await updateOrderItem(orderId, itemId, dto);
                  setItem(updated);
                  navigate(`/orders/${orderId}`);
                } catch (e: any) {
                  setErr(e?.response?.data?.message ?? t("errors.updateItemFail"));
                } finally {
                  setSaving(false);
                }
              }}
              className="px-4 py-2 rounded-xl text-sm font-semibold
                         bg-slate-900 text-white hover:bg-slate-800 transition
                         disabled:opacity-50 disabled:hover:bg-slate-900
                         dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
            >
              {saving ? t("common.saving") : t("common.save")}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default OrderItemPage;
