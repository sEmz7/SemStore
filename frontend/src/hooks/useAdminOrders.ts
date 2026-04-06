import { useCallback, useEffect, useState } from "react";
import type { TFunction } from "i18next";
import { listAdminOrders } from "../api/adminOrders";
import type { OrderShortDto, OrderStatus } from "../api/types";

const PAGE_SIZE = 10;

export function useAdminOrders(t: TFunction) {
  const [orders, setOrders] = useState<OrderShortDto[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [statusFilter, setStatusFilter] = useState<OrderStatus | "">(
    "IN_CHECK"
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const reload = useCallback(
    async (p: number, status: OrderStatus | "") => {
      setLoading(true);
      try {
        setError(null);
        const data = await listAdminOrders(p, PAGE_SIZE, status || undefined);
        setOrders(data.content);
        setTotalPages(Math.max(1, data.totalPages));
      } catch (e: any) {
        setError(e?.response?.data?.message ?? t("errors.loadAdminOrdersFail"));
      } finally {
        setLoading(false);
      }
    },
    [t]
  );

  useEffect(() => {
    reload(page, statusFilter);
  }, [page, statusFilter, reload]);

  function changePage(p: number) {
    setPage(p);
  }

  function changeStatus(s: OrderStatus | "") {
    setPage(0);
    setStatusFilter(s);
  }

  return {
    orders,
    page,
    totalPages,
    statusFilter,
    loading,
    error,
    changePage,
    changeStatus,
    reload: () => reload(page, statusFilter),
  };
}
