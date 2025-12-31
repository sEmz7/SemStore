import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import type { TFunction } from "i18next";
import { listAddresses } from "../api/addresses";
import { createOrder, deleteOrder, listOrders, updateOrder } from "../api/orders";
import type { AddressDto, OrderDto } from "../api/types";
import { isOrderNameValid, normStr, ORDER_NAME_MAX, ORDER_NAME_MIN } from "../utils/order";

function pickAddressId(o: unknown): string {
  if (!o || typeof o !== "object") return "";
  const r = o as Record<string, unknown>;

  const direct = r["addressId"];
  if (typeof direct === "string") return direct;

  const address = r["address"];
  if (address && typeof address === "object") {
    const a = address as Record<string, unknown>;
    const id = a["id"];
    if (typeof id === "string") return id;
  }

  return "";
}

type EditState = {
  id: string;
  name: string;
  addressId: string;
  initial: { name: string; addressId: string };
};

export function useOrders(t: TFunction, pageSize = 10) {
  const tRef = useRef(t);
  useEffect(() => {
    tRef.current = t;
  }, [t]);

  const [orders, setOrders] = useState<OrderDto[]>([]);
  const [addresses, setAddresses] = useState<AddressDto[]>([]);

  // create
  const [createAddressId, setCreateAddressId] = useState("");
  const [createName, setCreateName] = useState("");
  const [creating, setCreating] = useState(false);

  // edit
  const [edit, setEdit] = useState<EditState | null>(null);
  const [updatingId, setUpdatingId] = useState<string | null>(null);

  // delete
  const [deletingId, setDeletingId] = useState<string | null>(null);

  // page
  const [page, setPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  // ui
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const addressOptions = useMemo(
    () =>
      (addresses ?? []).map((a) => ({
        id: a.id,
        label: `${a.city}, ${a.street} ${a.building}`,
      })),
    [addresses]
  );

  const createNameOk = useMemo(() => isOrderNameValid(createName), [createName]);
  const editNameOk = useMemo(() => (edit ? isOrderNameValid(edit.name) : false), [edit]);

  const createNameError = useMemo(() => {
    if (!createName.trim()) return null;
    if (createNameOk) return null;
    return tRef.current("errors.orderNameLength", { min: ORDER_NAME_MIN, max: ORDER_NAME_MAX });
  }, [createName, createNameOk]);

  const editNameError = useMemo(() => {
    if (!edit) return null;
    if (!edit.name.trim()) return null;
    if (editNameOk) return null;
    return tRef.current("errors.orderNameLength", { min: ORDER_NAME_MIN, max: ORDER_NAME_MAX });
  }, [edit, editNameOk]);

  const isDirtyEdit = useMemo(() => {
    if (!edit) return false;
    return normStr(edit.name) !== edit.initial.name || normStr(edit.addressId) !== edit.initial.addressId;
  }, [edit]);

  const canCreate = useMemo(() => !!createAddressId && createNameOk && !creating && !loading, [
    createAddressId,
    createNameOk,
    creating,
    loading,
  ]);

  const canSaveEdit = useMemo(() => {
    if (!edit) return false;
    if (!edit.addressId) return false;
    if (!editNameOk) return false;
    if (!isDirtyEdit) return false;
    if (!!updatingId) return false;
    return true;
  }, [edit, editNameOk, isDirtyEdit, updatingId]);

  const reload = useCallback(
    async (p: number) => {
      setLoading(true);
      try {
        setError(null);
        const data: unknown = await listOrders(p, pageSize);

        if (Array.isArray(data)) {
          const arr = data as OrderDto[];
          setOrders(arr);
          setTotalElements(arr.length);
          setTotalPages(1);
          return;
        }

        const obj = (data ?? {}) as Record<string, unknown>;
        const contentRaw = obj["content"];
        const content = Array.isArray(contentRaw) ? (contentRaw as OrderDto[]) : [];

        const te = Number(obj["totalElements"] ?? content.length);
        const tp = Number(obj["totalPages"] ?? Math.max(1, Math.ceil(te / pageSize)));

        setOrders(content);
        setTotalElements(te);
        setTotalPages(tp);
      } catch (e: any) {
        setError(e?.response?.data?.message ?? tRef.current("errors.loadOrderFail"));
      } finally {
        setLoading(false);
      }
    },
    [pageSize]
  );

  const didInitRef = useRef(false);

  useEffect(() => {
    (async () => {
      try {
        const a = await listAddresses();
        setAddresses(a);
        if (a[0]) setCreateAddressId(a[0].id);

        didInitRef.current = true;
        setPage(0);
        await reload(0);
      } catch (e: any) {
        didInitRef.current = true;
        setError(e?.response?.data?.message ?? tRef.current("errors.loadAddressesFail"));
      }
    })().catch(() => {});
  }, [reload]);

  useEffect(() => {
    if (!didInitRef.current) return;
    reload(page).catch(() => {});
  }, [page, reload]);

  const submitCreate = useCallback(async () => {
    setError(null);

    const name = createName.trim();
    if (!createAddressId) {
      setError(tRef.current("errors.fillOrderAndAddress"));
      return;
    }
    if (!isOrderNameValid(name)) {
      setError(tRef.current("errors.orderNameLength", { min: ORDER_NAME_MIN, max: ORDER_NAME_MAX }));
      return;
    }

    setCreating(true);
    try {
      await createOrder({ addressId: createAddressId, name });
      setCreateName("");
      setPage(0);
      await reload(0);
    } catch (e: any) {
      setError(e?.response?.data?.message ?? tRef.current("errors.createOrderFail"));
    } finally {
      setCreating(false);
    }
  }, [createAddressId, createName, reload]);

  const startEdit = useCallback(
    (o: OrderDto) => {
      setError(null);

      const id = o.id;
      const name = normStr(o.name);
      const currentAddr = normStr(pickAddressId(o)) || (addresses[0]?.id ?? "");

      setEdit({
        id,
        name,
        addressId: currentAddr,
        initial: { name, addressId: currentAddr },
      });
    },
    [addresses]
  );

  const cancelEdit = useCallback(() => {
    setEdit(null);
    setUpdatingId(null);
  }, []);

  const saveEdit = useCallback(async () => {
    if (!edit) return;

    const orderId = edit.id;
    const name = edit.name.trim();
    const addr = edit.addressId;

    if (!addr) {
      setError(tRef.current("errors.fillOrderAndAddress"));
      return;
    }
    if (!isOrderNameValid(name)) {
      setError(tRef.current("errors.orderNameLength", { min: ORDER_NAME_MIN, max: ORDER_NAME_MAX }));
      return;
    }
    if (!isDirtyEdit) return;

    setError(null);
    setUpdatingId(orderId);
    try {
      const updated = await updateOrder(orderId, { name, addressId: addr });

      setOrders((prev) => prev.map((o) => (o.id === orderId ? ({ ...o, ...(updated as any) } as OrderDto) : o)));

      cancelEdit();
    } catch (e: any) {
      setError(e?.response?.data?.message ?? tRef.current("errors.updateOrderFail"));
    } finally {
      setUpdatingId(null);
    }
  }, [edit, isDirtyEdit, cancelEdit]);

  const removeOrder = useCallback(
    async (orderId: string): Promise<boolean> => {
      setError(null);
      setDeletingId(orderId);
      try {
        await deleteOrder(orderId);

        const nextTotal = Math.max(0, totalElements - 1);
        const nextTotalPages = Math.max(1, Math.ceil(nextTotal / pageSize));
        const nextPage = Math.min(page, nextTotalPages - 1);

        setTotalElements(nextTotal);
        setTotalPages(nextTotalPages);
        setPage(nextPage);

        await reload(nextPage);

        if (edit?.id === orderId) cancelEdit();
        return true;
      } catch (e: any) {
        setError(e?.response?.data?.message ?? tRef.current("errors.deleteOrderFail"));
        return false;
      } finally {
        setDeletingId(null);
      }
    },
    [cancelEdit, edit?.id, page, pageSize, reload, totalElements]
  );

  return {
    orders,
    addresses,
    addressOptions,

    createAddressId,
    setCreateAddressId,
    createName,
    setCreateName,
    creating,
    createNameOk,
    createNameError,
    canCreate,
    submitCreate,

    edit,
    setEdit,
    startEdit,
    cancelEdit,
    saveEdit,
    editNameOk,
    editNameError,
    isDirtyEdit,
    canSaveEdit,
    updatingId,

    deletingId,
    removeOrder,

    page,
    setPage,
    totalElements,
    totalPages,

    loading,
    error,
    setError,

    reload: (p = page) => reload(p),
  };
}
