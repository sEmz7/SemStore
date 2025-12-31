import { userApi } from "./http";
import type { AddressCreateDto, AddressDto, AddressUpdateDto } from "./types";

export async function listAddresses(): Promise<AddressDto[]> {
  const { data } = await userApi.get<AddressDto[]>("/address");
  return data;
}

export async function createAddress(dto: AddressCreateDto): Promise<AddressDto> {
  const { data } = await userApi.post<AddressDto>("/address", dto);
  return data;
}

export async function updateAddress(addressId: string, dto: AddressUpdateDto): Promise<AddressDto> {
  const { data } = await userApi.patch<AddressDto>(`/address/${addressId}`, dto);
  return data;
}

export async function deleteAddress(addressId: string): Promise<void> {
  await userApi.delete(`/address/${addressId}`);
}
