import { orderApi } from "./http";
import type { OrderCreateDto, OrderDto, OrderItem } from "./types";

export async function listOrders(page = 0, size = 10): Promise<any> {
  const resp = await orderApi.get(`/orders?page=${page}&size=${size}`);
  return resp.data;
}

export async function createOrder(dto: OrderCreateDto): Promise<OrderDto> {
  const resp = await orderApi.post(`/orders`, dto);
  return resp.data as OrderDto;
}

export async function getOrderById(orderId: string): Promise<OrderDto> {
  const resp = await orderApi.get(`/orders/${orderId}`);
  return resp.data as OrderDto;
}

export type OrderItemCreateDto = {
  link: string;
  size: string;
  configuration: string;
};

export async function addOrderItem(
  orderId: string,
  dto: OrderItemCreateDto
): Promise<OrderItem> {
  const resp = await orderApi.post(`/orders/${orderId}/items`, dto);
  return resp.data as OrderItem;
}

export async function deleteOrderItem(orderId: string, itemId: string): Promise<void> {
  await orderApi.delete(`/orders/${orderId}/items/${itemId}`);
}
