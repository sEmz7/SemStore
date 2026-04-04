import { orderApi, getUserIdFromAccessToken } from "./http";
import type { OrderCreateDto, OrderDto, OrderItem } from "./types";

export type Paged<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
};

export type ListOrdersResponse = OrderDto[] | Paged<OrderDto>;

export async function listOrders(page = 0, size = 10): Promise<ListOrdersResponse> {
  const { data } = await orderApi.get<ListOrdersResponse>("", { params: { page, size } });
  return data;
}

export type OrderUpdateDto = Partial<OrderCreateDto>;

export async function createOrder(dto: OrderCreateDto): Promise<OrderDto> {
  const { data } = await orderApi.post<OrderDto>("", dto);
  return data;
}

export async function getOrderById(orderId: string): Promise<OrderDto> {
  const { data } = await orderApi.get<OrderDto>(`/${orderId}`);
  return data;
}

export async function deleteOrder(orderId: string): Promise<void> {
  await orderApi.delete(`/${orderId}`);
}

export async function updateOrder(orderId: string, dto: OrderUpdateDto): Promise<OrderDto> {
  const { data } = await orderApi.patch<OrderDto>(`/${orderId}`, dto);
  return data;
}

export type OrderItemCreateDto = {
  link: string;
  size: string;
  configuration: string;
};

export type OrderItemUpdateDto = Partial<OrderItemCreateDto>;

export async function addOrderItem(orderId: string, dto: OrderItemCreateDto): Promise<OrderItem> {
  const { data } = await orderApi.post<OrderItem>(`/${orderId}/items`, dto);
  return data;
}

export async function getOrderItemById(orderId: string, itemId: string): Promise<OrderItem> {
  const { data } = await orderApi.get<OrderItem>(`/${orderId}/items/${itemId}`);
  return data;
}

export async function deleteOrderItem(orderId: string, itemId: string): Promise<void> {
  await orderApi.delete(`/${orderId}/items/${itemId}`);
}

export async function updateOrderItem(
  orderId: string,
  itemId: string,
  dto: OrderItemUpdateDto
): Promise<OrderItem> {
  const { data } = await orderApi.patch<OrderItem>(`/${orderId}/items/${itemId}`, dto);
  return data;
}

export async function confirmOrder(orderId: string): Promise<OrderDto> {
  const userId = getUserIdFromAccessToken();
  const headers: Record<string, string> = {};
  if (userId) headers["X-User-Id"] = userId;
  const { data } = await orderApi.patch<OrderDto>(
    `/orders/${orderId}/confirm`,
    undefined,
    { baseURL: "", headers }
  );
  return data;
}
