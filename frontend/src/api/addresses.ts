import { userApi } from "./http";
import type { AddressCreateDto, AddressDto, AddressUpdateDto } from "./types";

export async function listAddresses(): Promise<AddressDto[]> {
  const resp = await userApi.get("/users/address");
  return resp.data as AddressDto[];
}

export async function createAddress(dto: AddressCreateDto): Promise<AddressDto> {
  const resp = await userApi.post("/users/address", dto);
  return resp.data as AddressDto;
}

export async function updateAddress(
  addressId: string,
  dto: AddressUpdateDto
): Promise<AddressDto> {
  const resp = await userApi.patch(`/users/address/${addressId}`, dto);
  return resp.data as AddressDto;
}

export async function deleteAddress(addressId: string): Promise<void> {
  await userApi.delete(`/users/address/${addressId}`);
}
