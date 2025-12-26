import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { addOrderItem, deleteOrderItem, getOrderById } from "../api/orders";
import type { OrderDto } from "../api/types";

function cn(...a: Array<string | false | null | undefined>) {
  return a.filter(Boolean).join(" ");
}

function StatusBadge({ status }: { status: string }) {
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
      {status}
    </span>
  );
}

export function OrderDetailsPage() {
  const { id } = useParams<{ id: string }>();

  const [order, setOrder] = useState<OrderDto | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const [link, setLink] = useState("");
  const [size, setSize] = useState("");
  const [configuration, setConfiguration] = useState("");
  const [saving, setSaving] = useState(false);

  const items = useMemo(() => order?.items ?? [], [order]);

  async function reload() {
    if (!id) return;
    setLoading(true);
    try {
      setErr(null);
      setOrder(await getOrderById(id));
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? "Failed to load order");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    reload();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  if (!id) return null;

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <div className="text-sm text-slate-500 dark:text-slate-400">
            <Link to="/" className="hover:underline">
              Orders
            </Link>{" "}
            <span className="mx-1">/</span>
            <span className="break-all">{id}</span>
          </div>

          <div className="mt-2 flex flex-wrap items-center gap-3">
            <h1 className="text-xl sm:text-2xl font-semibold break-all">{id}</h1>
            {order?.status && <StatusBadge status={order.status} />}
          </div>

          {order?.createdDate && (
            <div className="mt-1 text-sm text-slate-500 dark:text-slate-400">
              Created: {order.createdDate}
            </div>
          )}
        </div>

        <button
          onClick={reload}
          className="px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                     dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
        >
          {loading ? "Loading..." : "Refresh"}
        </button>
      </div>

      {err && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                        dark:border-red-900/40 dark:bg-red-950/40 dark:text-red-200">
          {err}
        </div>
      )}

      {/* Add item */}
      <div className="rounded-3xl border bg-white p-6 shadow-sm dark:bg-slate-950 dark:border-slate-800">
        <div className="flex items-center justify-between gap-3">
          <div>
            <div className="text-sm font-semibold">Add item</div>
            <div className="text-xs text-slate-500 dark:text-slate-400">
              link + size + configuration
            </div>
          </div>
        </div>

        <div className="mt-4 grid gap-3 sm:grid-cols-3">
          <input
            value={link}
            onChange={(e) => setLink(e.target.value)}
            placeholder="https://example.com/product/1"
            className="rounded-xl border px-3 py-2 text-sm outline-none
                       bg-white dark:bg-slate-950 dark:border-slate-800
                       focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800 sm:col-span-2"
          />
          <input
            value={size}
            onChange={(e) => setSize(e.target.value)}
            placeholder="42.5"
            className="rounded-xl border px-3 py-2 text-sm outline-none
                       bg-white dark:bg-slate-950 dark:border-slate-800
                       focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800"
          />
          <input
            value={configuration}
            onChange={(e) => setConfiguration(e.target.value)}
            placeholder="black"
            className="rounded-xl border px-3 py-2 text-sm outline-none
                       bg-white dark:bg-slate-950 dark:border-slate-800
                       focus:ring-2 focus:ring-slate-200 dark:focus:ring-slate-800 sm:col-span-3"
          />

          <div className="sm:col-span-3 flex justify-end">
            <button
              disabled={saving || !link.trim() || !size.trim() || !configuration.trim()}
              onClick={async () => {
                if (!id) return;
                setSaving(true);
                try {
                  await addOrderItem(id, { link, size, configuration });
                  setLink("");
                  setSize("");
                  setConfiguration("");
                  await reload();
                } catch (e: any) {
                  setErr(e?.response?.data?.message ?? "Failed to add item");
                } finally {
                  setSaving(false);
                }
              }}
              className="px-4 py-2 rounded-xl text-sm font-semibold
                         bg-slate-900 text-white hover:bg-slate-800 transition
                         disabled:opacity-50 disabled:hover:bg-slate-900
                         dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
            >
              {saving ? "Saving..." : "Add item"}
            </button>
          </div>
        </div>
      </div>

      {/* Items list */}
      <div className="rounded-3xl border bg-white overflow-hidden dark:bg-slate-950 dark:border-slate-800">
        <div className="px-6 py-4 border-b flex items-center justify-between dark:border-slate-800">
          <div className="text-sm font-semibold">Items</div>
          <div className="text-xs text-slate-500 dark:text-slate-400">
            {items.length} items
          </div>
        </div>

        {items.length === 0 ? (
          <div className="p-6 text-sm text-slate-500 dark:text-slate-400">
            No items yet.
          </div>
        ) : (
          <ul className="divide-y dark:divide-slate-800">
            {items.map((it) => (
              <li key={it.id} className="p-6 hover:bg-slate-50 transition dark:hover:bg-slate-900/40">
                <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
                  <div className="min-w-0">
                    <div className="text-sm font-semibold break-all">{it.id}</div>
                    <div className="mt-1 text-sm text-slate-700 dark:text-slate-200 break-all">
                      <a className="underline underline-offset-4 hover:opacity-80" href={it.link} target="_blank" rel="noreferrer">
                        {it.link}
                      </a>
                    </div>
                    <div className="mt-2 text-xs text-slate-500 dark:text-slate-400">
                      size: <span className="font-medium">{it.size}</span> • configuration:{" "}
                      <span className="font-medium">{it.configuration}</span>
                      {typeof it.price !== "undefined" && (
                        <>
                          {" "}
                          • price: <span className="font-medium">{it.price}</span>
                        </>
                      )}
                    </div>
                  </div>

                  <button
                    onClick={async () => {
                      if (!id) return;
                      try {
                        await deleteOrderItem(id, it.id);
                        await reload();
                      } catch (e: any) {
                        setErr(e?.response?.data?.message ?? "Delete failed");
                      }
                    }}
                    className="px-3 py-2 rounded-xl text-sm border bg-white hover:bg-slate-50 transition
                               dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
                  >
                    Delete
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
