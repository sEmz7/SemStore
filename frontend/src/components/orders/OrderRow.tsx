import { memo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import { copyToClipboard } from "../../utils/clipboard";
import type { OrderDto } from "../../api/types";
import { cn } from "../../utils/cn";
import FormField from "../FormField";

export type OrderRowLabels = {
  openOrder: string;
  edit: string;
  delete: string;
  save: string;
  saving: string;
  cancel: string;
  name: string;
  address: string;
  namePlaceholder: string;
  menuAria: string;
};

type AddressOption = { id: string; label: string };

export type OrderRowProps = {
  order: OrderDto;
  statusText: string;

  addressOptions: AddressOption[];

  state: {
    isDeleting: boolean;
    isEditing: boolean;
    isUpdating: boolean;

    editName: string;
    editAddressId: string;
    editNameError?: string | null;
    nameMaxLength: number;

    canSaveEdit: boolean; 
  };

  menu: {
    open: boolean;
    pos: { top: number; left: number } | null;
    onToggle: (btn: HTMLButtonElement) => void; 
    onClose: () => void;
    width?: number;
  };

  actions: {
    startEdit: () => void;
    askDelete: () => void;

    editNameChange: (v: string) => void;
    editAddressChange: (id: string) => void;

    saveEdit: () => void;
    cancelEdit: () => void;
  };

  labels: OrderRowLabels;
};

const MENU_W_FALLBACK = 176;

function OrderRow({ order, statusText, addressOptions, state, menu, actions, labels }: OrderRowProps) {
  const { isDeleting, isEditing, isUpdating, editName, editAddressId, editNameError, nameMaxLength, canSaveEdit } =
    state;
  const { i18n, t } = useTranslation();
  const [trackingCopied, setTrackingCopied] = useState(false);

  async function handleCopyTracking(trackingNumber: string) {
    const ok = await copyToClipboard(trackingNumber);
    if (ok) {
      setTrackingCopied(true);
      setTimeout(() => setTrackingCopied(false), 2000);
    }
  }

  function formatDate(dateStr: string): string {
    try {
      return new Intl.DateTimeFormat(i18n.language, { day: "numeric", month: "long", year: "numeric", hour: "2-digit", minute: "2-digit" }).format(new Date(dateStr));
    } catch {
      return dateStr;
    }
  }

  return (
    <li className="p-4 hover:bg-slate-50 transition dark:hover:bg-slate-900/40">
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
        {/* LEFT */}
        <div className="min-w-0 flex-1">
          {!isEditing ? (
            <>
              <div className="font-medium text-slate-900 break-all dark:text-slate-50">{order.name || order.id}</div>
              <div className="text-xs text-slate-500 mt-1 dark:text-slate-400 break-all">
                {order.trackingNumber && (
                  <>
                    <button
                      type="button"
                      onClick={() => handleCopyTracking(order.trackingNumber!)}
                      title={t("order.copyTracking")}
                      className="underline decoration-dotted underline-offset-2 cursor-pointer hover:text-slate-700 dark:hover:text-slate-200 transition-colors"
                    >
                      {trackingCopied ? t("order.trackingCopied") : order.trackingNumber}
                    </button>
                    {order.createdDate ? " • " : ""}
                  </>
                )}
                {order.createdDate ? formatDate(order.createdDate) : ""}
              </div>
            </>
          ) : (
            <div className="grid gap-2 sm:grid-cols-3">
              <div className="sm:col-span-2">
                <FormField
                  label={labels.name}
                  value={editName}
                  maxLength={nameMaxLength}
                  onChange={actions.editNameChange}
                  placeholder={labels.namePlaceholder}
                  error={editNameError ?? null}
                />
              </div>

              <label className="grid gap-1">
                <span className="text-xs font-medium text-slate-600 dark:text-slate-300">{labels.address}</span>

                <select
                  value={editAddressId}
                  onChange={(e) => actions.editAddressChange(e.target.value)}
                  className="w-full rounded-xl border px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-slate-200
                             bg-white dark:bg-slate-950 dark:border-slate-800 dark:focus:ring-slate-800"
                >
                  {addressOptions.map((x) => (
                    <option key={x.id} value={x.id}>
                      {x.label}
                    </option>
                  ))}
                </select>
              </label>
            </div>
          )}
        </div>

        {/* RIGHT */}
        <div className="w-full sm:w-auto sm:shrink-0 flex flex-col sm:flex-row sm:items-center gap-2">
          <span className="inline-flex w-fit items-center rounded-full border px-2.5 py-1 text-xs font-semibold dark:border-slate-700 dark:text-slate-200">
            {statusText}
          </span>

          {!isEditing ? (
            <div className="flex items-center gap-2 w-full sm:w-auto">
              <Link
                to={`/orders/${order.id}`}
                className={cn(
                  "inline-flex items-center justify-center rounded-full px-3 py-2 text-xs font-semibold",
                  "bg-slate-900 text-white hover:bg-slate-800 transition",
                  "dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200",
                  "flex-1 sm:flex-none",
                  isDeleting && "opacity-50 pointer-events-none"
                )}
              >
                {labels.openOrder}
              </Link>

              <div data-orders-menu-root={order.id} className="shrink-0">
                <button
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation();
                    menu.onToggle(e.currentTarget);
                  }}
                  disabled={isDeleting}
                  className="inline-flex items-center justify-center rounded-full border w-10 h-10
                             bg-white hover:bg-slate-50 disabled:opacity-50 transition
                             dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
                  aria-label={labels.menuAria}
                  aria-haspopup="menu"
                  aria-expanded={menu.open}
                >
                  <span aria-hidden>⋯</span>
                </button>
              </div>

              {menu.open && menu.pos && (
                <div
                  data-orders-menu-popup={order.id}
                  role="menu"
                  className="fixed z-50 rounded-xl border bg-white shadow-lg dark:bg-slate-950 dark:border-slate-800"
                  style={{ top: menu.pos.top, left: menu.pos.left, width: menu.width ?? MENU_W_FALLBACK }}
                >
                  <button
                    type="button"
                    role="menuitem"
                    onClick={(e) => {
                      e.stopPropagation();
                      menu.onClose();
                      actions.startEdit();
                    }}
                    className="w-full text-left px-3 py-2 text-sm hover:bg-slate-50 transition
                               dark:hover:bg-slate-900/60 rounded-t-xl"
                  >
                    {labels.edit}
                  </button>

                  <button
                    type="button"
                    role="menuitem"
                    onClick={(e) => {
                      e.stopPropagation();
                      menu.onClose();
                      actions.askDelete();
                    }}
                    className="w-full text-left px-3 py-2 text-sm text-red-700 hover:bg-red-50 transition
                               dark:text-red-300 dark:hover:bg-red-950/40 rounded-b-xl"
                  >
                    {labels.delete}
                  </button>
                </div>
              )}
            </div>
          ) : (
            <div className="flex items-center gap-2 w-full sm:w-auto">
              <button
                type="button"
                onClick={actions.saveEdit}
                disabled={isUpdating || !canSaveEdit} 
                className="flex-1 sm:flex-none inline-flex items-center justify-center rounded-full px-3 py-2 text-xs font-semibold
                           bg-slate-900 text-white hover:bg-slate-800 disabled:opacity-50 transition
                           dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
              >
                {isUpdating ? labels.saving : labels.save}
              </button>

              <button
                type="button"
                onClick={actions.cancelEdit}
                disabled={isUpdating}
                className="flex-1 sm:flex-none inline-flex items-center justify-center rounded-full border px-3 py-2 text-xs font-semibold
                           bg-white hover:bg-slate-50 disabled:opacity-50 transition
                           dark:bg-slate-950 dark:border-slate-800 dark:hover:bg-slate-900/60"
              >
                {labels.cancel}
              </button>
            </div>
          )}
        </div>
      </div>
    </li>
  );
}

export default memo(OrderRow);
