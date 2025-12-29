// src/api/orders.ts
import { orderApi } from "./http";
import type { OrderCreateDto, OrderDto, OrderItem } from "./types";

export async function listOrders(page = 0, size = 10): Promise<any> {
  const resp = await orderApi.get("", { params: { page, size } });
  return resp.data;
}

export async function createOrder(dto: OrderCreateDto): Promise<OrderDto> {
  const resp = await orderApi.post("", dto);
  return resp.data as OrderDto;
}

export async function getOrderById(orderId: string): Promise<OrderDto> {
  const resp = await orderApi.get(`/${orderId}`);
  return resp.data as OrderDto;
}

export async function deleteOrder(orderId: string): Promise<void> {
  await orderApi.delete(`/${orderId}`);
}

export type OrderUpdateDto = Partial<OrderCreateDto>;

export async function updateOrder(orderId: string, dto: OrderUpdateDto): Promise<OrderDto> {
  const resp = await orderApi.patch(`/${orderId}`, dto);
  return resp.data as OrderDto;
}

export type OrderItemCreateDto = {
  link: string;
  size: string;
  configuration: string;
};

export type OrderItemUpdateDto = Partial<OrderItemCreateDto>;

export async function addOrderItem(orderId: string, dto: OrderItemCreateDto): Promise<OrderItem> {
  const resp = await orderApi.post(`/${orderId}/items`, dto);
  return resp.data as OrderItem;
}

export async function getOrderItemById(orderId: string, itemId: string): Promise<OrderItem> {
  const resp = await orderApi.get(`/${orderId}/items/${itemId}`);
  return resp.data as OrderItem;
}

export async function deleteOrderItem(orderId: string, itemId: string): Promise<void> {
  await orderApi.delete(`/${orderId}/items/${itemId}`);
}

export async function updateOrderItem(
  orderId: string,
  itemId: string,
  dto: OrderItemUpdateDto
): Promise<OrderItem> {
  const resp = await orderApi.patch(`/${orderId}/items/${itemId}`, dto);
  return resp.data as OrderItem;
}
