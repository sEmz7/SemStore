// src/hooks/useOrderDetails.ts
import { useCallback, useEffect, useMemo, useState } from "react";
import type { TFunction } from "i18next";
import { addOrderItem, deleteOrderItem, getOrderById } from "../api/orders";
import type { OrderDto, OrderItem } from "../api/types";
import { localizeOrderItemLengthErrorFromBackend } from "../utils/validation";

type AddItemPayload = {
  link: string;
  size: string;
  configuration: string;
};

export function useOrderDetails(orderId: string | undefined, t: TFunction) {
  const [order, setOrder] = useState<OrderDto | null>(null);

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const [saving, setSaving] = useState(false);
  const [deletingItemId, setDeletingItemId] = useState<string | null>(null);

  const items = useMemo<OrderItem[]>(() => (order?.items ?? []) as OrderItem[], [order]);

  const reload = useCallback(async () => {
    if (!orderId) return;
    setLoading(true);
    try {
      setError(null);
      setOrder(await getOrderById(orderId));
    } catch (e: any) {
      setError(e?.response?.data?.message ?? t("errors.loadOrderFail"));
    } finally {
      setLoading(false);
    }
  }, [orderId, t]);

  useEffect(() => {
    reload();
  }, [reload]);

  function localizeAddItemError(e: any): string {
    const msg: string = e?.response?.data?.message ?? e?.message ?? "";
    if (!msg.trim()) return t("errors.addItemFail");

    const localized = localizeOrderItemLengthErrorFromBackend(msg, t);
    return localized ?? msg;
  }

  const addItem = useCallback(
    async (payload: AddItemPayload): Promise<boolean> => {
      if (!orderId) return false;
      setSaving(true);
      try {
        setError(null);
        await addOrderItem(orderId, payload);
        await reload();
        return true;
      } catch (e: any) {
        setError(localizeAddItemError(e));
        return false;
      } finally {
        setSaving(false);
      }
    },
    [orderId, reload] // eslint-disable-line react-hooks/exhaustive-deps
  );

  const deleteItem = useCallback(
    async (itemId: string): Promise<boolean> => {
      if (!orderId) return false;
      setDeletingItemId(itemId);
      try {
        setError(null);
        await deleteOrderItem(orderId, itemId);
        await reload();
        return true;
      } catch (e: any) {
        setError(e?.response?.data?.message ?? t("errors.deleteFail"));
        return false;
      } finally {
        setDeletingItemId(null);
      }
    },
    [orderId, reload, t]
  );

  return {
    order,
    items,

    error,
    setError,

    loading,
    saving,
    deletingItemId,

    reload,
    addItem,
    deleteItem,
  };
}
