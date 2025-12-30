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

function cn(...a: Array<string | false | null | undefined>) {
  return a.filter(Boolean).join(" ");
}

type MenuState = {
  id: string;
  top: number;
  left: number;
};

const MENU_W = 176; 
const MENU_GAP = 8;

function normStr(v: unknown) {
  return (v ?? "").toString().trim();
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
  const [initialEdit, setInitialEdit] = useState<{ name: string; addressId: string } | null>(null);
  const [updatingId, setUpdatingId] = useState<string | null>(null);

  // pagination
  const [page, setPage] = useState(0);
  const size = 10;
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  // menu
  const [menuOpenId, setMenuOpenId] = useState<string | null>(null);
  const [menu, setMenu] = useState<MenuState | null>(null);

  useEffect(() => {
    function onDocPointerDown(e: PointerEvent) {
      if (!menuOpenId) return;
      const target = e.target as HTMLElement;
      if (target.closest(`[data-orders-menu-root="${menuOpenId}"]`)) return;
      if (target.closest(`[data-orders-menu-popup="${menuOpenId}"]`)) return;
      setMenuOpenId(null);
      setMenu(null);
    }
    function onKeyDown(e: KeyboardEvent) {
      if (e.key === "Escape") {
        setMenuOpenId(null);
        setMenu(null);
      }
    }
    function onScrollOrResize() {
      if (menuOpenId) {
        setMenuOpenId(null);
        setMenu(null);
      }
    }

    document.addEventListener("pointerdown", onDocPointerDown);
    document.addEventListener("keydown", onKeyDown);
    window.addEventListener("scroll", onScrollOrResize, { passive: true });
    window.addEventListener("resize", onScrollOrResize);

    return () => {
      document.removeEventListener("pointerdown", onDocPointerDown);
      document.removeEventListener("keydown", onKeyDown);
      window.removeEventListener("scroll", onScrollOrResize as any);
      window.removeEventListener("resize", onScrollOrResize as any);
    };
  }, [menuOpenId]);

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

  const isDirtyEdit = useMemo(() => {
    if (!editingId || !initialEdit) return false;
    return normStr(editName) !== initialEdit.name || normStr(editAddressId) !== initialEdit.addressId;
  }, [editingId, initialEdit, editName, editAddressId]);

  async function reload(p = page) {
    setLoading(true);
    try {
      const data: any = await listOrders(p, size);

      if (Array.isArray(data)) {
        setOrders(data);
        setTotalElements(data.length);
        setTotalPages(1);
      } else {
        const content = Array.isArray(data?.content) ? data.content : [];
        const te = Number(data?.totalElements ?? content.length);
        const tp = Number(data?.totalPages ?? Math.max(1, Math.ceil(te / size)));

        setOrders(content);
        setTotalElements(te);
        setTotalPages(tp);
      }
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

      const nextTotal = Math.max(0, totalElements - 1);
      const nextTotalPages = Math.max(1, Math.ceil(nextTotal / size));
      const nextPage = Math.min(page, nextTotalPages - 1);

      setTotalElements(nextTotal);
      setTotalPages(nextTotalPages);
      setPage(nextPage);

      await reload(nextPage);

      if (editingId === orderId) cancelEdit();
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? t("errors.deleteOrderFail"));
    } finally {
      setDeletingId(null);
    }
  }

  function startEdit(o: OrderDto) {
    setErr(null);
    setEditingId(o.id);

    const name = normStr(o.name);
    const currentAddr = normStr(pickAddressId(o as any)) || (addresses[0]?.id ?? "");

    setEditName(name);
    setEditAddressId(currentAddr);
    setInitialEdit({ name, addressId: currentAddr });

    setMenuOpenId(null);
    setMenu(null);
  }

  function cancelEdit() {
    setEditingId(null);
    setEditName("");
    setEditAddressId("");
    setInitialEdit(null);
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
    if (!isDirtyEdit) return;

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
      setPage(0);
      await reload(0);
    })().catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    reload(page).catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  function tStatus(status?: string) {
    if (!status) return "";
    const key = `orderStatus.${status}`;
    const tr = t(key);
    return tr === key ? status : tr;
  }

  function openMenuFor(orderId: string, btn: HTMLElement) {
    const rect = btn.getBoundingClientRect();
    const vw = window.innerWidth;
    const vh = window.innerHeight;

    const approxH = 96;

    const hasSpaceBelow = vh - rect.bottom >= approxH + MENU_GAP;
    const top = hasSpaceBelow ? rect.bottom + MENU_GAP : Math.max(MENU_GAP, rect.top - approxH - MENU_GAP);

    let left = rect.right - MENU_W;
    left = Math.max(MENU_GAP, Math.min(left, vw - MENU_W - MENU_GAP));

    setMenu({ id: orderId, top, left });
    setMenuOpenId(orderId);
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
        <div className="min-w-0">
          <h1 className="text-2xl font-semibold">{t("orders.title")}</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">{t("orders.subtitle")}</p>
        </div>
        <button
          onClick={() => reload(page)}
          className="w-full sm:w-auto px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
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
            <div className="text-sm font-medium text-slate-700 mb-1 dark:text-slate-200">{t("orders.name")}</div>
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
            <div className="text-sm font-medium text-slate-700 mb-1 dark:text-slate-200">{t("orders.address")}</div>
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
                  setPage(0);
                  await reload(0);
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

      <div className="rounded-2xl border bg-white dark:bg-slate-950 dark:border-slate-800">
        <div className="px-4 py-3 border-b flex items-center justify-between dark:border-slate-800 rounded-t-2xl gap-3">
          <div className="text-sm font-semibold">{t("orders.recent")}</div>
          <div className="text-xs text-slate-500 dark:text-slate-400 shrink-0">
            {loading ? (
              t("common.loading")
            ) : (
              <>
                {t("orders.total", { count: totalElements })}
                {totalPages > 1 ? ` • ${page + 1}/${totalPages}` : ""}
              </>
            )}
          </div>
        </div>

        {orders.length === 0 ? (
          <div className="p-6 text-sm text-slate-500 dark:text-slate-400">{t("orders.noOrders")}</div>
        ) : (
          <ul className="divide-y dark:divide-slate-800 rounded-b-2xl">
            {orders.map((o) => {
              const isDeleting = deletingId === o.id;
              const isEditing = editingId === o.id;
              const isUpdating = updatingId === o.id;

              return (
                <li key={o.id} className="p-4 hover:bg-slate-50 transition dark:hover:bg-slate-900/40">
                  <div className="flex flex-col gap-3">
                    <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
                      <div className="min-w-0 flex-1">
                        {!isEditing ? (
                          <div className="font-medium text-slate-900 break-all dark:text-slate-50">{o.name || o.id}</div>
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

                      <div className="w-full sm:w-auto sm:shrink-0 flex flex-col sm:flex-row sm:items-center gap-2">
                        <span className="inline-flex w-fit items-center rounded-full border px-2.5 py-1 text-xs font-semibold dark:border-slate-700 dark:text-slate-200">
                          {tStatus(o.status)}
                        </span>

                        {!isEditing ? (
                          <div className="flex items-center gap-2 w-full sm:w-auto">
                            <Link
                              to={`/orders/${o.id}`}
                              className={cn(
                                "inline-flex items-center justify-center rounded-full px-3 py-2 text-xs font-semibold",
                                "bg-slate-900 text-white hover:bg-slate-800 transition",
                                "dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200",
                                "flex-1 sm:flex-none",
                                isDeleting && "opacity-50 pointer-events-none"
                              )}
                            >
                              {t("orders.openOrder")}
                            </Link>

                            <div data-orders-menu-root={o.id} className="shrink-0">
                              <button
                                type="button"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  const btn = e.currentTarget as unknown as HTMLElement;

                                  if (menuOpenId === o.id) {
                                    setMenuOpenId(null);
                                    setMenu(null);
                                    return;
                                  }
                                  openMenuFor(o.id, btn);
                                }}
                                disabled={isDeleting}
                                className="inline-flex items-center justify-center rounded-full border w-10 h-10
                                           bg-white hover:bg-slate-50 disabled:opacity-50 transition
                                           dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
                                aria-label="menu"
                              >
                                ⋯
                              </button>
                            </div>

                            {menuOpenId === o.id && menu?.id === o.id && (
                              <div
                                data-orders-menu-popup={o.id}
                                className="fixed z-50 rounded-xl border bg-white shadow-lg
                                           dark:bg-slate-950 dark:border-slate-800"
                                style={{ top: menu.top, left: menu.left, width: MENU_W }}
                              >
                                <button
                                  type="button"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    startEdit(o);
                                  }}
                                  className="w-full text-left px-3 py-2 text-sm hover:bg-slate-50 transition
                                             dark:hover:bg-slate-900/60 rounded-t-xl"
                                >
                                  {t("common.edit")}
                                </button>

                                <button
                                  type="button"
                                  onClick={async (e) => {
                                    e.stopPropagation();
                                    setMenuOpenId(null);
                                    setMenu(null);
                                    await onDeleteOrder(o.id);
                                  }}
                                  className="w-full text-left px-3 py-2 text-sm text-red-700 hover:bg-red-50 transition
                                             dark:text-red-300 dark:hover:bg-red-950/40 rounded-b-xl"
                                >
                                  {isDeleting ? t("common.deleting") : t("common.delete")}
                                </button>
                              </div>
                            )}
                          </div>
                        ) : (
                          <div className="flex items-center gap-2 w-full sm:w-auto">
                            <button
                              type="button"
                              onClick={() => saveEdit(o.id)}
                              disabled={isUpdating || !editAddressId || !editNameOk || !isDirtyEdit}
                              className="flex-1 sm:flex-none inline-flex items-center justify-center rounded-full px-3 py-2 text-xs font-semibold
                                         bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50 transition
                                         dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
                            >
                              {isUpdating ? t("common.saving") : t("common.save")}
                            </button>

                            <button
                              type="button"
                              onClick={cancelEdit}
                              disabled={isUpdating}
                              className="flex-1 sm:flex-none inline-flex items-center justify-center rounded-full border px-3 py-2 text-xs font-semibold
                                         bg-white hover:bg-slate-50 disabled:opacity-50 transition
                                         dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
                            >
                              {t("common.cancel")}
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </li>
              );
            })}
          </ul>
        )}

        {/* pagination */}
        {totalPages > 1 && (
          <div className="border-t px-4 py-3 flex flex-col sm:flex-row gap-2 sm:items-center sm:justify-between dark:border-slate-800">
            <div className="text-xs text-slate-500 dark:text-slate-400">
              {page + 1}/{totalPages}
            </div>

            <div className="flex gap-2">
              <button
                disabled={page <= 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                className="w-full sm:w-auto px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                           disabled:opacity-50
                           dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
              >
                {t("common.back")}
              </button>

              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                className="w-full sm:w-auto px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                           disabled:opacity-50
                           dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
              >
                {t("common.next")}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default OrdersPage;
