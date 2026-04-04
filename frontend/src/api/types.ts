export type UserDto = {
  id: string;
  email: string;
};

export type JwtAuthDto = {
  token: string;
  refreshToken: string;
};

export type AddressCreateDto = {
  firstname: string;
  lastname: string;
  patronymic: string;
  phone: string;
  city: string;
  street: string;
  building: string;
  postalCode: string;
};

export type AddressUpdateDto = Partial<AddressCreateDto>;

export type AddressDto = AddressCreateDto & {
  id: string;
  user: UserDto;
};

export type OrderStatus = "CREATED" | "IN_CHECK" | "PENDING" | "CANCELED" | "ORDERED" | "PAID";

export type OrderCreateDto = { name: string; addressId: string };
export type OrderUpdateDto = { name: string; addressId: string };

export type OrderItem = {
  id: string;
  link: string;
  size: string;
  configuration: string;
  price?: number;
};

export type OrderDto = {
  id: string;
  name: string;
  userId: string;
  addressId: string;
  status: OrderStatus;
  createdDate: string;
  trackingNumber?: string;
  items?: OrderItem[];
};
