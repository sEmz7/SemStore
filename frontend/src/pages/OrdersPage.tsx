import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listAddresses } from "../api/addresses";
import { createOrder, listOrders } from "../api/orders";
import type { AddressDto, OrderDto } from "../api/types";

export function OrdersPage() {
  const [orders, setOrders] = useState<OrderDto[]>([]);
  const [addresses, setAddresses] = useState<AddressDto[]>([]);
  const [addressId, setAddressId] = useState("");
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function reload() {
    setLoading(true);
    try {
      const data: any = await listOrders(0, 20);
      const content = Array.isArray(data) ? data : data?.content ?? [];
      setOrders(content);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    (async () => {
      const a = await listAddresses();
      setAddresses(a);
      if (a[0]) setAddressId(a[0].id);
      await reload();
    })().catch(() => {});
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">Orders</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Create and view your orders
          </p>
        </div>
        <button
          onClick={reload}
          className="px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                     dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
        >
          Refresh
        </button>
      </div>

      {err && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                        dark:border-red-900/50 dark:bg-red-950/40 dark:text-red-300">
          {err}
        </div>
      )}

      <div className="rounded-2xl border bg-white p-4
                      dark:bg-slate-950 dark:border-slate-800">
        <div className="flex flex-col sm:flex-row gap-3 sm:items-center sm:justify-between">
          <div className="flex-1">
            <div className="text-sm font-medium text-slate-700 mb-1 dark:text-slate-200">
              Address
            </div>
            <select
              className="w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
              value={addressId}
              onChange={(e) => setAddressId(e.target.value)}
            >
              {addresses.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.city}, {a.street} {a.building}
                </option>
              ))}
            </select>
          </div>

          <button
            disabled={!addressId}
            onClick={async () => {
              setErr(null);
              try {
                await createOrder({ addressId } as any);
                await reload();
              } catch (e: any) {
                setErr(e?.response?.data?.message ?? "Create order failed");
              }
            }}
            className="px-4 py-2 rounded-xl text-sm font-semibold bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50 transition
                       dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
          >
            Create order
          </button>
        </div>
      </div>

      <div className="rounded-2xl border bg-white overflow-hidden
                      dark:bg-slate-950 dark:border-slate-800">
        <div className="px-4 py-3 border-b flex items-center justify-between dark:border-slate-800">
          <div className="text-sm font-semibold">Recent</div>
          <div className="text-xs text-slate-500 dark:text-slate-400">
            {loading ? "Loading..." : `${orders.length} items`}
          </div>
        </div>

        {orders.length === 0 ? (
          <div className="p-6 text-sm text-slate-500 dark:text-slate-400">
            No orders yet.
          </div>
        ) : (
          <ul className="divide-y dark:divide-slate-800">
            {orders.map((o) => (
              <li key={o.id} className="p-4 hover:bg-slate-50 transition dark:hover:bg-slate-900/40">
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
                  <div className="min-w-0">
                    <Link
                      to={`/orders/${o.id}`}
                      className="font-medium text-slate-900 hover:underline break-all dark:text-slate-50"
                    >
                      {o.id}
                    </Link>
                    <div className="text-xs text-slate-500 mt-1 dark:text-slate-400">
                      {o.createdDate}
                    </div>
                  </div>

                  <span className="inline-flex w-fit items-center rounded-full border px-2.5 py-1 text-xs font-semibold
                                   dark:border-slate-700 dark:text-slate-200">
                    {o.status}
                  </span>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
