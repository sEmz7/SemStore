import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import ConfirmDialog from "../components/ConfirmDialog";
import FormField from "../components/FormField";
import OrderRow, { type OrderRowLabels } from "../components/orders/OrderRow";
import { usePopupMenu } from "../hooks/usePopupMenu";
import { useOrders } from "../hooks/useOrders";
import { ORDER_NAME_MAX } from "../utils/order";

const PAGE_SIZE = 10;
const MENU_W = 176;

export function OrdersPage() {
  const { t } = useTranslation();
  const o = useOrders(t, PAGE_SIZE);

  const menu = usePopupMenu({
    width: MENU_W,
    rootAttr: "data-orders-menu-root",
    popupAttr: "data-orders-menu-popup",
  });

  const [confirmDelete, setConfirmDelete] = useState<{ id: string; name: string } | null>(null);

  function tStatus(status?: string) {
    if (!status) return "";
    const key = `orderStatus.${status}`;
    const tr = t(key);
    return tr === key ? status : tr;
  }

  const rowLabels: OrderRowLabels = useMemo(
    () => ({
      openOrder: t("orders.openOrder"),
      edit: t("common.edit"),
      delete: t("common.delete"),
      save: t("common.save"),
      saving: t("common.saving"),
      cancel: t("common.cancel"),
      name: t("orders.name"),
      address: t("orders.address"),
      namePlaceholder: t("orders.orderNameEditPlaceholder"),
      menuAria: "menu",
    }),
    [t]
  );

  return (
    <div className="space-y-6">
      {/* header */}
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
        <div className="min-w-0">
          <h1 className="text-2xl font-semibold">{t("orders.title")}</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">{t("orders.subtitle")}</p>
        </div>

        <button
          onClick={() => o.reload(o.page)}
          className="w-full sm:w-auto px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                     dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
        >
          {t("common.refresh")}
        </button>
      </div>

      {/* error */}
      {o.error && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700
                        dark:border-red-900/50 dark:bg-red-950/40 dark:text-red-300 break-words">
          {o.error}
        </div>
      )}

      {/* create */}
      <div className="rounded-2xl border bg-white p-4 dark:bg-slate-950 dark:border-slate-800">
        <div className="grid gap-3 sm:grid-cols-3 sm:items-end">
          <FormField
            label={t("orders.name")}
            value={o.createName}
            maxLength={ORDER_NAME_MAX}
            onChange={(v) => {
              o.setCreateName(v);
              o.setError(null);
            }}
            placeholder={t("orders.orderNamePlaceholder")}
            error={o.createNameError} 
          />

          <label className="grid gap-1">
            <span className="text-xs font-medium text-slate-600 dark:text-slate-300">{t("orders.address")}</span>
            <select
              className="w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                         bg-white dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
              value={o.createAddressId}
              onChange={(e) => o.setCreateAddressId(e.target.value)}
            >
              {o.addresses.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.city}, {a.street} {a.building}
                </option>
              ))}
            </select>
          </label>

          <div className="sm:col-span-1 flex justify-end">
            <button
              disabled={!o.canCreate}
              onClick={o.submitCreate}
              className="w-full sm:w-auto px-4 py-2 rounded-xl text-sm font-semibold
                         bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50 transition
                         dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
            >
              {o.creating ? t("common.saving") : t("orders.createOrder")}
            </button>
          </div>
        </div>
      </div>

      {/* list */}
      <div className="rounded-2xl border bg-white dark:bg-slate-950 dark:border-slate-800">
        <div className="px-4 py-3 border-b flex items-center justify-between dark:border-slate-800 rounded-t-2xl gap-3">
          <div className="text-sm font-semibold">{t("orders.recent")}</div>

          <div className="text-xs text-slate-500 dark:text-slate-400 shrink-0">
            {o.loading ? (
              t("common.loading")
            ) : (
              <>
                {t("orders.total", { count: o.totalElements })}
                {o.totalPages > 1 ? ` • ${o.page + 1}/${o.totalPages}` : ""}
              </>
            )}
          </div>
        </div>

        {o.orders.length === 0 ? (
          <div className="p-6 text-sm text-slate-500 dark:text-slate-400">{t("orders.noOrders")}</div>
        ) : (
          <ul className="divide-y dark:divide-slate-800 rounded-b-2xl">
            {o.orders.map((order) => {
              const isDeleting = o.deletingId === order.id;
              const isEditing = o.edit?.id === order.id;
              const isUpdating = o.updatingId === order.id;

              const editName = isEditing ? o.edit!.name : "";
              const editAddressId = isEditing ? o.edit!.addressId : "";

              const editNameError = isEditing ? o.editNameError : null; 

              const menuOpen = menu.openId === order.id && menu.pos?.id === order.id;

              return (
                <OrderRow
                  key={order.id}
                  order={order}
                  statusText={tStatus(order.status)}
                  addressOptions={o.addressOptions}
                  labels={rowLabels}
                  state={{
                    isDeleting,
                    isEditing,
                    isUpdating,
                    editName,
                    editAddressId,
                    editNameError,
                    nameMaxLength: ORDER_NAME_MAX,
                    canSaveEdit: o.canSaveEdit, 
                  }}
                  menu={{
                    open: menuOpen,
                    pos: menuOpen && menu.pos ? { top: menu.pos.top, left: menu.pos.left } : null,
                    onToggle: (btn) => menu.toggleFor(order.id, btn),
                    onClose: menu.close,
                    width: MENU_W,
                  }}
                  actions={{
                    startEdit: () => o.startEdit(order),
                    askDelete: () => {
                      menu.close();
                      setConfirmDelete({ id: order.id, name: order.name || order.id });
                    },
                    editNameChange: (v) => o.setEdit((p) => (p ? { ...p, name: v } : p)),
                    editAddressChange: (id) => o.setEdit((p) => (p ? { ...p, addressId: id } : p)),
                    saveEdit: o.saveEdit,
                    cancelEdit: o.cancelEdit,
                  }}
                />
              );
            })}
          </ul>
        )}

        {/* pagination */}
        {o.totalPages > 1 && (
          <div className="border-t px-4 py-3 flex flex-col sm:flex-row gap-2 sm:items-center sm:justify-between dark:border-slate-800">
            <div className="text-xs text-slate-500 dark:text-slate-400">
              {o.page + 1}/{o.totalPages}
            </div>

            <div className="flex gap-2">
              <button
                disabled={o.page <= 0}
                onClick={() => o.setPage((p) => Math.max(0, p - 1))}
                className="w-full sm:w-auto px-3 py-2 rounded-xl text-sm font-medium border bg-white hover:bg-slate-50 transition
                           disabled:opacity-50
                           dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
              >
                {t("common.back")}
              </button>

              <button
                disabled={o.page >= o.totalPages - 1}
                onClick={() => o.setPage((p) => Math.min(o.totalPages - 1, p + 1))}
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

      {/* confirm delete */}
      <ConfirmDialog
        open={!!confirmDelete}
        title={t("orders.confirmDeleteTitle")}
        description={
          confirmDelete ? t("orders.confirmDeleteText", { name: confirmDelete.name, id: confirmDelete.id }) : undefined
        }
        confirmText={t("common.delete")}
        cancelText={t("common.cancel")}
        loading={!!confirmDelete && o.deletingId === confirmDelete.id}
        onClose={() => setConfirmDelete(null)}
        onConfirm={async () => {
          if (!confirmDelete) return;
          const ok = await o.removeOrder(confirmDelete.id);
          if (ok) setConfirmDelete(null);
        }}
      />
    </div>
  );
}

export default OrdersPage;
