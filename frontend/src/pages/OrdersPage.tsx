// src/pages/OrdersPage.tsx
import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { listAddresses } from "../api/addresses";
import { createOrder, deleteOrder, listOrders, updateOrder } from "../api/orders";
import type { AddressDto, OrderDto } from "../api/types";
import { useTranslation } from "react-i18next";

function pickAddressId(o: any): string {
  return o?.addressId ?? o?.address?.id ?? "";
}

const NAME_MIN = 2;
const NAME_MAX = 255;

function isNameValid(name: string) {
  const n = name.trim();
  return n.length >= NAME_MIN && n.length <= NAME_MAX;
}

export function OrdersPage() {
  const { t } = useTranslation();

  const [orders, setOrders] = useState<OrderDto[]>([]);
  const [addresses, setAddresses] = useState<AddressDto[]>([]);
  const [addressId, setAddressId] = useState("");
  const [orderName, setOrderName] = useState("");

  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const [deletingId, setDeletingId] = useState<string | null>(null);

  // edit order
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editName, setEditName] = useState("");
  const [editAddressId, setEditAddressId] = useState("");
  const [updatingId, setUpdatingId] = useState<string | null>(null);

  const addressOptions = useMemo(
    () =>
      (addresses ?? []).map((a) => ({
        id: a.id,
        label: `${a.city}, ${a.street} ${a.building}`,
      })),
    [addresses]
  );

  const createNameOk = isNameValid(orderName);
  const editNameOk = isNameValid(editName);

  async function reload() {
    setLoading(true);
    try {
      const data: any = await listOrders(0, 20);
      const content = Array.isArray(data) ? data : data?.content ?? [];
      setOrders(content);
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? t("errors.loadOrderFail"));
    } finally {
      setLoading(false);
    }
  }

  async function onDeleteOrder(orderId: string) {
    setErr(null);
    setDeletingId(orderId);
    try {
      await deleteOrder(orderId);
      setOrders((prev) => prev.filter((o) => o.id !== orderId));

      if (editingId === orderId) {
        cancelEdit();
      }
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? t("errors.deleteOrderFail"));
    } finally {
      setDeletingId(null);
    }
  }

  function startEdit(o: OrderDto) {
    setErr(null);
    setEditingId(o.id);
    setEditName((o.name ?? "").toString());

    const currentAddr = pickAddressId(o as any);
    setEditAddressId(currentAddr || (addresses[0]?.id ?? ""));
  }

  function cancelEdit() {
    setEditingId(null);
    setEditName("");
    setEditAddressId("");
    setUpdatingId(null);
  }

  async function saveEdit(orderId: string) {
    const name = editName.trim();
    const addr = editAddressId;

    if (!addr) {
      setErr(t("errors.fillOrderAndAddress"));
      return;
    }
    if (!isNameValid(name)) {
      setErr(t("errors.orderNameLength", { min: NAME_MIN, max: NAME_MAX }));
      return;
    }

    setErr(null);
    setUpdatingId(orderId);
    try {
      const updated = await updateOrder(orderId, { name, addressId: addr });

      setOrders((prev) =>
        prev.map((o) => (o.id === orderId ? ({ ...o, ...(updated as any) } as OrderDto) : o))
      );

      cancelEdit();
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? t("errors.updateOrderFail"));
    } finally {
      setUpdatingId(null);
    }
  }

  useEffect(() => {
    (async () => {
      const a = await listAddresses();
      setAddresses(a);
      if (a[0]) setAddressId(a[0].id);
      await reload();
    })().catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">{t("orders.title")}</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">{t("orders.subtitle")}</p>
        </div>
        <button
          onClick={reload}
          className="px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                     dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
        >
          {t("common.refresh")}
        </button>
      </div>

      {err && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                        dark:border-red-900/50 dark:bg-red-950/40 dark:text-red-300">
          {err}
        </div>
      )}

      {/* Create order */}
      <div className="rounded-2xl border bg-white p-4 dark:bg-slate-950 dark:border-slate-800">
        <div className="grid gap-3 sm:grid-cols-3 sm:items-end">
          <div className="sm:col-span-1">
            <div className="text-sm font-medium text-slate-700 mb-1 dark:text-slate-200">
              {t("orders.name")}
            </div>
            <input
              value={orderName}
              maxLength={NAME_MAX}
              onChange={(e) => setOrderName(e.target.value)}
              placeholder={t("orders.orderNamePlaceholder")}
              className="w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
            />
            {!!orderName.trim() && !createNameOk && (
              <div className="mt-1 text-xs text-red-600 dark:text-red-300">
                {t("errors.orderNameLength", { min: NAME_MIN, max: NAME_MAX })}
              </div>
            )}
          </div>

          <div className="sm:col-span-1">
            <div className="text-sm font-medium text-slate-700 mb-1 dark:text-slate-200">
              {t("orders.address")}
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

          <div className="sm:col-span-1 flex justify-end">
            <button
              disabled={!addressId || !createNameOk}
              onClick={async () => {
                setErr(null);

                const name = orderName.trim();
                if (!addressId) {
                  setErr(t("errors.fillOrderAndAddress"));
                  return;
                }
                if (!isNameValid(name)) {
                  setErr(t("errors.orderNameLength", { min: NAME_MIN, max: NAME_MAX }));
                  return;
                }

                try {
                  await createOrder({ addressId, name });
                  setOrderName("");
                  await reload();
                } catch (e: any) {
                  setErr(e?.response?.data?.message ?? t("errors.createOrderFail"));
                }
              }}
              className="w-full sm:w-auto px-4 py-2 rounded-xl text-sm font-semibold bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50 transition
                         dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
            >
              {t("orders.createOrder")}
            </button>
          </div>
        </div>
      </div>

      {/* Orders list */}
      <div className="rounded-2xl border bg-white overflow-hidden dark:bg-slate-950 dark:border-slate-800">
        <div className="px-4 py-3 border-b flex items-center justify-between dark:border-slate-800">
          <div className="text-sm font-semibold">{t("orders.recent")}</div>
          <div className="text-xs text-slate-500 dark:text-slate-400">
            {loading ? t("common.loading") : t("orders.itemsCount", { count: orders.length })}
          </div>
        </div>

        {orders.length === 0 ? (
          <div className="p-6 text-sm text-slate-500 dark:text-slate-400">
            {t("orders.noOrders")}
          </div>
        ) : (
          <ul className="divide-y dark:divide-slate-800">
            {orders.map((o) => {
              const isDeleting = deletingId === o.id;
              const isEditing = editingId === o.id;
              const isUpdating = updatingId === o.id;

              const statusLabel = t(`orderStatus.${o.status}`, { defaultValue: o.status });

              return (
                <li key={o.id} className="p-4 hover:bg-slate-50 transition dark:hover:bg-slate-900/40">
                  <div className="flex flex-col gap-3">
                    <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-2">
                      <div className="min-w-0">
                        {!isEditing ? (
                          <Link
                            to={`/orders/${o.id}`}
                            className="font-medium text-slate-900 hover:underline break-words dark:text-slate-50"
                          >
                            {o.name || o.id}
                          </Link>
                        ) : (
                          <div className="grid gap-2 sm:grid-cols-3">
                            <div className="sm:col-span-2">
                              <input
                                value={editName}
                                maxLength={NAME_MAX}
                                onChange={(e) => setEditName(e.target.value)}
                                placeholder={t("orders.orderNameEditPlaceholder")}
                                className="w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                                           dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
                              />
                              {!!editName.trim() && !editNameOk && (
                                <div className="mt-1 text-xs text-red-600 dark:text-red-300">
                                  {t("errors.orderNameLength", { min: NAME_MIN, max: NAME_MAX })}
                                </div>
                              )}
                            </div>

                            <select
                              value={editAddressId}
                              onChange={(e) => setEditAddressId(e.target.value)}
                              className="w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                                         dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
                            >
                              {addressOptions.map((x) => (
                                <option key={x.id} value={x.id}>
                                  {x.label}
                                </option>
                              ))}
                            </select>
                          </div>
                        )}

                        <div className="text-xs text-slate-500 mt-1 dark:text-slate-400 break-all">
                          {t("orders.id")}: {o.id}
                          {o.createdDate ? ` • ${o.createdDate}` : ""}
                        </div>
                      </div>

                      <div className="flex items-center gap-2">
                        <span
                          className="inline-flex w-fit items-center rounded-full border px-2.5 py-1 text-xs font-semibold
                                     dark:border-slate-700 dark:text-slate-200"
                        >
                          {statusLabel}
                        </span>

                        {!isEditing ? (
                          <>
                            <button
                              onClick={() => startEdit(o)}
                              disabled={isDeleting}
                              className="inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold
                                         bg-white hover:bg-slate-50 disabled:opacity-50 transition
                                         dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
                            >
                              {t("orders.edit")}
                            </button>

                            <button
                              disabled={isDeleting}
                              onClick={() => onDeleteOrder(o.id)}
                              className="inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold
                                         border-red-200 text-red-700 hover:bg-red-50 disabled:opacity-50 transition
                                         dark:border-red-900/50 dark:text-red-300 dark:hover:bg-red-950/40"
                            >
                              {isDeleting ? t("common.deleting") : t("orders.delete")}
                            </button>
                          </>
                        ) : (
                          <>
                            <button
                              onClick={() => saveEdit(o.id)}
                              disabled={isUpdating || !editAddressId || !editNameOk}
                              className="inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold
                                         bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50 transition
                                         dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
                            >
                              {isUpdating ? t("common.saving") : t("common.save")}
                            </button>

                            <button
                              onClick={cancelEdit}
                              disabled={isUpdating}
                              className="inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold
                                         bg-white hover:bg-slate-50 disabled:opacity-50 transition
                                         dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
                            >
                              {t("common.cancel")}
                            </button>
                          </>
                        )}
                      </div>
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

export default OrdersPage;
