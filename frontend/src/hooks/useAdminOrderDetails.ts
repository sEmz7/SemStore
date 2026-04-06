import { useCallback, useEffect, useState } from "react";
import type { TFunction } from "i18next";
import { getAdminOrder, setItemPrice, submitOrder } from "../api/adminOrders";
import type { OrderFullDto } from "../api/types";

export function useAdminOrderDetails(
  orderId: string | undefined,
  t: TFunction
) {
  const [order, setOrder] = useState<OrderFullDto | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // per-item price input values (itemId → string)
  const [priceInputs, setPriceInputs] = useState<Record<string, string>>({});
  const [savingPriceId, setSavingPriceId] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const reload = useCallback(async () => {
    if (!orderId) return;
    setLoading(true);
    try {
      setError(null);
      const data = await getAdminOrder(orderId);
      setOrder(data);
      // initialise price inputs from loaded data (show existing prices)
      const inputs: Record<string, string> = {};
      data.items.forEach((item) => {
        if (item.price != null) inputs[item.id] = String(item.price);
      });
      setPriceInputs((prev) => ({ ...inputs, ...prev }));
    } catch (e: any) {
      setError(e?.response?.data?.message ?? t("errors.loadAdminOrderFail"));
    } finally {
      setLoading(false);
    }
  }, [orderId, t]);

  useEffect(() => {
    reload();
  }, [reload]);

  function setPriceInput(itemId: string, value: string) {
    setPriceInputs((prev) => ({ ...prev, [itemId]: value }));
  }

  async function saveItemPrice(itemId: string): Promise<void> {
    if (!orderId) return;
    const raw = priceInputs[itemId] ?? "";
    const price = parseFloat(raw);
    if (!raw.trim() || isNaN(price) || price <= 0) return;

    setSavingPriceId(itemId);
    try {
      setError(null);
      const updated = await setItemPrice(orderId, itemId, price);
      // update item price in local order state without full reload
      setOrder((prev) => {
        if (!prev) return prev;
        return {
          ...prev,
          items: prev.items.map((item) =>
            item.id === itemId ? { ...item, price: updated.price } : item
          ),
        };
      });
    } catch (e: any) {
      setError(e?.response?.data?.message ?? t("errors.setPriceFail"));
    } finally {
      setSavingPriceId(null);
    }
  }

  async function handleSubmit(): Promise<void> {
    if (!orderId) return;
    setSubmitting(true);
    try {
      setError(null);
      const updated = await submitOrder(orderId);
      setOrder(updated);
    } catch (e: any) {
      setError(e?.response?.data?.message ?? t("errors.submitOrderFail"));
    } finally {
      setSubmitting(false);
    }
  }

  const allPricesSet =
    (order?.items.length ?? 0) > 0 &&
    (order?.items.every((item) => item.price != null && item.price > 0) ??
      false);

  const canSubmit =
    order?.status === "IN_CHECK" && allPricesSet && !submitting;

  return {
    order,
    loading,
    error,
    setError,
    priceInputs,
    setPriceInput,
    savingPriceId,
    saveItemPrice,
    submitting,
    handleSubmit,
    canSubmit,
    allPricesSet,
    reload,
  };
}
