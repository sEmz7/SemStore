// src/pages/OrderItemPage.tsx
import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getOrderItemById, updateOrderItem } from "../api/orders";
import type { OrderItem } from "../api/types";

const LINK_MIN = 2;
const LINK_MAX = 50;

const SIZE_MIN = 2;
const SIZE_MAX = 30;

const CONF_MIN = 2;
const CONF_MAX = 255;

function cn(...a: Array<string | false | null | undefined>) {
  return a.filter(Boolean).join(" ");
}

function norm(v: unknown) {
  return (v ?? "").toString().trim();
}

function lenBetween(v: string, min: number, max: number) {
  const s = v.trim();
  return s.length >= min && s.length <= max;
}

export default function OrderItemPage() {
  const { t } = useTranslation();
  const nav = useNavigate();

  const params = useParams();
  const orderId = (params as any).orderId ?? (params as any).id ?? "";
  const itemId = (params as any).itemId ?? "";

  const [item, setItem] = useState<OrderItem | null>(null);

  const [link, setLink] = useState("");
  const [size, setSize] = useState("");
  const [configuration, setConfiguration] = useState("");

  const [initial, setInitial] = useState<{ link: string; size: string; configuration: string } | null>(null);

  const [touched, setTouched] = useState({ link: false, size: false, configuration: false });

  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const linkErr = useMemo(() => {
    if (!touched.link) return null;
    if (!link.trim()) return t("errors.required");
    if (!lenBetween(link, LINK_MIN, LINK_MAX))
      return t("errors.fieldLengthBetween", { field: t("order.link"), min: LINK_MIN, max: LINK_MAX });
    return null;
  }, [t, link, touched.link]);

  const sizeErr = useMemo(() => {
    if (!touched.size) return null;
    if (!size.trim()) return t("errors.required");
    if (!lenBetween(size, SIZE_MIN, SIZE_MAX))
      return t("errors.fieldLengthBetween", { field: t("order.size"), min: SIZE_MIN, max: SIZE_MAX });
    return null;
  }, [t, size, touched.size]);

  const confErr = useMemo(() => {
    if (!touched.configuration) return null;
    if (!configuration.trim()) return t("errors.required");
    if (!lenBetween(configuration, CONF_MIN, CONF_MAX))
      return t("errors.fieldLengthBetween", {
        field: t("order.configuration"),
        min: CONF_MIN,
        max: CONF_MAX,
      });
    return null;
  }, [t, configuration, touched.configuration]);

  const isDirty = useMemo(() => {
    if (!initial) return false;
    return norm(link) !== initial.link || norm(size) !== initial.size || norm(configuration) !== initial.configuration;
  }, [initial, link, size, configuration]);

  const canSave = !!orderId && !!itemId && !loading && !saving && !linkErr && !sizeErr && !confErr && isDirty;

  async function reload() {
    if (!orderId || !itemId) return;
    setLoading(true);
    try {
      setErr(null);
      const it = await getOrderItemById(orderId, itemId);

      const l = (it as any)?.link ?? "";
      const s = (it as any)?.size ?? "";
      const c = (it as any)?.configuration ?? "";

      setItem(it);
      setLink(l);
      setSize(s);
      setConfiguration(c);
      setInitial({ link: norm(l), size: norm(s), configuration: norm(c) });

      setTouched({ link: false, size: false, configuration: false });
    } catch (e: any) {
      const msg = e?.response?.data?.message ?? e?.message;
      setErr(typeof msg === "string" ? msg : t("errors.loadItemFail"));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    reload().catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [orderId, itemId]);

  if (!orderId || !itemId) {
    return (
      <div className="rounded-2xl border bg-white p-6 dark:bg-slate-950 dark:border-slate-800">
        <div className="text-lg font-semibold">Route params not found</div>
        <div className="mt-2 text-sm text-slate-600 dark:text-slate-300">
          Expected: <code>/orders/:id/items/:itemId</code> or <code>/orders/:orderId/items/:itemId</code>
        </div>
        <div className="mt-2 text-sm text-slate-600 dark:text-slate-300">
          Got: <code>{JSON.stringify(params)}</code>
        </div>
        <div className="mt-4">
          <Link
            to="/"
            className="px-4 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                       dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
          >
            {t("common.back")}
          </Link>
        </div>
      </div>
    );
  }

  const price =
    typeof (item as any)?.price === "undefined" || (item as any)?.price === null ? "—" : (item as any).price;

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <div className="text-sm text-slate-500 dark:text-slate-400 break-all">
            <Link to="/" className="hover:underline">
              {t("nav.orders")}
            </Link>{" "}
            <span className="mx-1">/</span>
            <Link to={`/orders/${orderId}`} className="hover:underline">
              {orderId}
            </Link>{" "}
            <span className="mx-1">/</span>
            <span>{itemId}</span>
          </div>

          <h1 className="mt-2 text-xl sm:text-2xl font-semibold break-all">{t("item.title", { id: itemId })}</h1>

          <div className="mt-1 text-sm text-slate-600 dark:text-slate-300 break-all">
            {t("item.orderId")}: {orderId}
          </div>

          <div className="mt-1 text-sm text-slate-600 dark:text-slate-300">
            {t("order.price")}: <span className="font-medium">{price}</span>
          </div>
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
                     dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200 break-words"
        >
          {err}
        </div>
      )}

      <div className="rounded-3xl border bg-white p-6 shadow-sm dark:bg-slate-950 dark:border-slate-800">
        <div>
          <div className="text-sm font-semibold">{t("item.editTitle")}</div>
          <div className="text-xs text-slate-500 dark:text-slate-400">{t("order.hint")}</div>
        </div>

        <div className="mt-4 grid gap-3">
          <label className="grid gap-1">
            <span className="text-xs font-medium text-slate-600 dark:text-slate-300">{t("order.link")}</span>
            <input
              value={link}
              maxLength={LINK_MAX}
              onChange={(e) => setLink(e.target.value)}
              onBlur={() => setTouched((p) => ({ ...p, link: true }))}
              className={cn(
                "rounded-xl border px-3 py-2 text-sm outline-none bg-white dark:bg-slate-950 dark:border-slate-800",
                "focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800",
                linkErr && "border-red-300 focus:ring-red-100 dark:border-red-900/60 dark:focus:ring-red-900/30"
              )}
            />
            {linkErr && <div className="text-xs text-red-600 dark:text-red-300">{linkErr}</div>}
          </label>

          <label className="grid gap-1">
            <span className="text-xs font-medium text-slate-600 dark:text-slate-300">{t("order.size")}</span>
            <input
              value={size}
              maxLength={SIZE_MAX}
              onChange={(e) => setSize(e.target.value)}
              onBlur={() => setTouched((p) => ({ ...p, size: true }))}
              className={cn(
                "rounded-xl border px-3 py-2 text-sm outline-none bg-white dark:bg-slate-950 dark:border-slate-800",
                "focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800",
                sizeErr && "border-red-300 focus:ring-red-100 dark:border-red-900/60 dark:focus:ring-red-900/30"
              )}
            />
            {sizeErr && <div className="text-xs text-red-600 dark:text-red-300">{sizeErr}</div>}
          </label>

          <label className="grid gap-1">
            <span className="text-xs font-medium text-slate-600 dark:text-slate-300">{t("order.configuration")}</span>
            <input
              value={configuration}
              maxLength={CONF_MAX}
              onChange={(e) => setConfiguration(e.target.value)}
              onBlur={() => setTouched((p) => ({ ...p, configuration: true }))}
              className={cn(
                "rounded-xl border px-3 py-2 text-sm outline-none bg-white dark:bg-slate-950 dark:border-slate-800",
                "focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800",
                confErr && "border-red-300 focus:ring-red-100 dark:border-red-900/60 dark:focus:ring-red-900/30"
              )}
            />
            {confErr && <div className="text-xs text-red-600 dark:text-red-300">{confErr}</div>}
          </label>

          <div className="mt-2 flex items-center justify-between gap-2">
            <Link
              to={`/orders/${orderId}`}
              className="px-4 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                         dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
            >
              {t("common.back")}
            </Link>

            <button
              disabled={!canSave}
              onClick={async () => {
                setErr(null);
                setTouched({ link: true, size: true, configuration: true });

                if (linkErr || sizeErr || confErr) {
                  setErr(t("errors.fixForm"));
                  return;
                }
                if (!isDirty) return;

                setSaving(true);
                try {
                  await updateOrderItem(orderId, itemId, {
                    link: link.trim(),
                    size: size.trim(),
                    configuration: configuration.trim(),
                  });

                  nav(`/orders/${orderId}`, { replace: true });
                } catch (e: any) {
                  const code = e?.response?.data?.code;
                  if (code === "ORDER_STATUS_NOT_MODIFIABLE") {
                    setErr(t("errors.orderCannotBeModified"));
                  } else {
                    setErr(t("errors.updateItemFail"));
                  }
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
