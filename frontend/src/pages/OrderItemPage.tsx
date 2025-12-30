// src/pages/OrderItemPage.tsx
import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { getOrderItemById, updateOrderItem } from "../api/orders";
import type { OrderItem } from "../api/types";
import { useTranslation } from "react-i18next";
import type { TFunction } from "i18next";

const MIN_LEN = 2;
const MAX_LEN = 50;

function cn(...a: Array<string | false | null | undefined>) {
  return a.filter(Boolean).join(" ");
}

function hasRu(s: string) {
  return /[а-яё]/i.test(s);
}

function norm(v: string) {
  return (v ?? "").toString().trim();
}

function validateValue(v: string, min: number, max: number) {
  const s = v.trim();
  if (!s) return { ok: false, kind: "required" as const };
  if (s.length < min) return { ok: false, kind: "min" as const };
  if (s.length > max) return { ok: false, kind: "max" as const };
  return { ok: true as const };
}

function localizeValidationFromBackend(
  msg: string,
  t: TFunction,
  fieldLabels: Record<string, string>
) {
  const m = msg.toLowerCase();

  const fieldMatch =
    msg.match(/on field '([^']+)'/i)?.[1] ??
    msg.match(/field\s+'([^']+)'/i)?.[1];

  const rangeMatch =
    msg.match(/(\d+)\s*до\s*(\d+)/i) ?? msg.match(/(\d+)\s*to\s*(\d+)/i);

  if (fieldMatch && rangeMatch) {
    const fieldKey = fieldMatch.trim();
    const min = Number(rangeMatch[1]);
    const max = Number(rangeMatch[2]);
    const label = fieldLabels[fieldKey] ?? fieldKey;

    return {
      field: fieldKey,
      text: t("errors.fieldLengthBetween", { field: label, min, max }),
    };
  }

  if (m.includes("validation failed") || m.includes("field error")) {
    return { field: null as any, text: t("errors.fixForm") };
  }

  if (hasRu(msg)) return { field: null as any, text: msg };
  return { field: null as any, text: msg || t("errors.updateItemFail") };
}

export function OrderItemPage() {
  const { t } = useTranslation();
  const nav = useNavigate();

  const params = useParams();
  const orderId = (params as any).id ?? (params as any).orderId ?? "";
  const itemId = (params as any).itemId ?? "";

  const [item, setItem] = useState<OrderItem | null>(null);

  const [link, setLink] = useState("");
  const [size, setSize] = useState("");
  const [configuration, setConfiguration] = useState("");
  const [initial, setInitial] = useState<{ link: string; size: string; configuration: string } | null>(
    null
  );

  const [touched, setTouched] = useState({
    link: false,
    size: false,
    configuration: false,
  });

  const [fieldErr, setFieldErr] = useState<
    Partial<Record<"link" | "size" | "configuration", string>>
  >({});

  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const fieldLabels = useMemo(
    () => ({
      link: t("order.link"),
      size: t("order.size"),
      configuration: t("order.configuration"),
    }),
    [t]
  );

  const linkV = validateValue(link, MIN_LEN, MAX_LEN);
  const sizeV = validateValue(size, MIN_LEN, MAX_LEN);
  const confV = validateValue(configuration, MIN_LEN, MAX_LEN);

  const isDirty = useMemo(() => {
    if (!initial) return false;
    return (
      norm(link) !== initial.link ||
      norm(size) !== initial.size ||
      norm(configuration) !== initial.configuration
    );
  }, [initial, link, size, configuration]);

  const canSave =
    !!orderId &&
    !!itemId &&
    !loading &&
    !saving &&
    linkV.ok &&
    sizeV.ok &&
    confV.ok &&
    isDirty; 

  function computeClientFieldErrors() {
    const next: Partial<Record<"link" | "size" | "configuration", string>> = {};

    if (!linkV.ok) {
      next.link =
        linkV.kind === "required"
          ? t("errors.required")
          : t("errors.fieldLengthBetween", {
              field: fieldLabels.link,
              min: MIN_LEN,
              max: MAX_LEN,
            });
    }

    if (!sizeV.ok) {
      next.size =
        sizeV.kind === "required"
          ? t("errors.required")
          : t("errors.fieldLengthBetween", {
              field: fieldLabels.size,
              min: MIN_LEN,
              max: MAX_LEN,
            });
    }

    if (!confV.ok) {
      next.configuration =
        confV.kind === "required"
          ? t("errors.required")
          : t("errors.fieldLengthBetween", {
              field: fieldLabels.configuration,
              min: MIN_LEN,
              max: MAX_LEN,
            });
    }

    return next;
  }

  async function reload() {
    if (!orderId || !itemId) return;
    setLoading(true);
    try {
      setErr(null);
      setFieldErr({});
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
          Expected: <code>/orders/:id/items/:itemId</code> or{" "}
          <code>/orders/:orderId/items/:itemId</code>
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

          <h1 className="mt-2 text-xl sm:text-2xl font-semibold break-all">
            {t("item.title", { id: itemId })}
          </h1>

          <div className="mt-1 text-sm text-slate-600 dark:text-slate-300 break-all">
            {t("item.orderId")}: {orderId}
          </div>

          <div className="mt-1 text-sm text-slate-600 dark:text-slate-300">
            {t("order.price")}:{" "}
            <span className="font-medium">
              {typeof (item as any)?.price === "undefined" || (item as any)?.price === null
                ? "—"
                : (item as any).price}
            </span>
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
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                        dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200">
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
            <span className="text-xs font-medium text-slate-600 dark:text-slate-300">
              {t("order.link")}
            </span>
            <input
              value={link}
              onChange={(e) => {
                setLink(e.target.value);
                if (touched.link) setFieldErr((p) => ({ ...p, link: undefined }));
              }}
              onBlur={() => {
                setTouched((p) => ({ ...p, link: true }));
                const next = computeClientFieldErrors();
                setFieldErr((p) => ({ ...p, link: next.link }));
              }}
              className={cn(
                "rounded-xl border px-3 py-2 text-sm outline-none bg-white dark:bg-slate-950 dark:border-slate-800",
                "focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800",
                fieldErr.link &&
                  "border-red-300 focus:ring-red-100 dark:border-red-900/60 dark:focus:ring-red-900/30"
              )}
            />
            {fieldErr.link && <div className="text-xs text-red-600 dark:text-red-300">{fieldErr.link}</div>}
          </label>

          <label className="grid gap-1">
            <span className="text-xs font-medium text-slate-600 dark:text-slate-300">
              {t("order.size")}
            </span>
            <input
              value={size}
              onChange={(e) => {
                setSize(e.target.value);
                if (touched.size) setFieldErr((p) => ({ ...p, size: undefined }));
              }}
              onBlur={() => {
                setTouched((p) => ({ ...p, size: true }));
                const next = computeClientFieldErrors();
                setFieldErr((p) => ({ ...p, size: next.size }));
              }}
              className={cn(
                "rounded-xl border px-3 py-2 text-sm outline-none bg-white dark:bg-slate-950 dark:border-slate-800",
                "focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800",
                fieldErr.size &&
                  "border-red-300 focus:ring-red-100 dark:border-red-900/60 dark:focus:ring-red-900/30"
              )}
            />
            {fieldErr.size && <div className="text-xs text-red-600 dark:text-red-300">{fieldErr.size}</div>}
          </label>

          <label className="grid gap-1">
            <span className="text-xs font-medium text-slate-600 dark:text-slate-300">
              {t("order.configuration")}
            </span>
            <input
              value={configuration}
              onChange={(e) => {
                setConfiguration(e.target.value);
                if (touched.configuration)
                  setFieldErr((p) => ({ ...p, configuration: undefined }));
              }}
              onBlur={() => {
                setTouched((p) => ({ ...p, configuration: true }));
                const next = computeClientFieldErrors();
                setFieldErr((p) => ({ ...p, configuration: next.configuration }));
              }}
              className={cn(
                "rounded-xl border px-3 py-2 text-sm outline-none bg-white dark:bg-slate-950 dark:border-slate-800",
                "focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800",
                fieldErr.configuration &&
                  "border-red-300 focus:ring-red-100 dark:border-red-900/60 dark:focus:ring-red-900/30"
              )}
            />
            {fieldErr.configuration && (
              <div className="text-xs text-red-600 dark:text-red-300">{fieldErr.configuration}</div>
            )}
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

                const next = computeClientFieldErrors();
                setTouched({ link: true, size: true, configuration: true });
                setFieldErr(next);

                if (next.link || next.size || next.configuration) {
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
                  const msg: string = e?.response?.data?.message ?? e?.message ?? "";

                  if (msg) {
                    const parsed = localizeValidationFromBackend(msg, t, fieldLabels);

                    if (
                      parsed.field === "link" ||
                      parsed.field === "size" ||
                      parsed.field === "configuration"
                    ) {
                      setFieldErr((p) => ({ ...p, [parsed.field]: parsed.text }));
                      setErr(t("errors.fixForm"));
                    } else {
                      setErr(parsed.text);
                    }
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

export default OrderItemPage;
