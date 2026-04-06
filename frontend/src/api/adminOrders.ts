import { adminApi } from "./http";
import type { OrderFullDto, OrderItemDto, OrderShortDto, OrderStatus } from "./types";

export type Paged<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
};

export async function listAdminOrders(
  page = 0,
  size = 10,
  status?: OrderStatus
): Promise<Paged<OrderShortDto>> {
  const params: Record<string, unknown> = { page, size };
  if (status) params.status = status;
  const { data } = await adminApi.get<Paged<OrderShortDto>>("/orders", { params });
  return data;
}

export async function getAdminOrder(orderId: string): Promise<OrderFullDto> {
  const { data } = await adminApi.get<OrderFullDto>(`/orders/${orderId}`);
  return data;
}

export async function setItemPrice(
  orderId: string,
  itemId: string,
  price: number
): Promise<OrderItemDto> {
  const { data } = await adminApi.patch<OrderItemDto>(
    `/orders/${orderId}/items/${itemId}`,
    { price }
  );
  return data;
}

export async function submitOrder(orderId: string): Promise<OrderFullDto> {
  const { data } = await adminApi.patch<OrderFullDto>(`/orders/${orderId}/submit`);
  return data;
}
