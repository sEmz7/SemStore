// src/api/auth.ts
import { userApi } from "./http";
import type { JwtAuthDto, UserDto } from "./types";

export async function login(email: string, password: string): Promise<JwtAuthDto> {
  const resp = await userApi.post<JwtAuthDto>("/auth/login", { email, password });
  return resp.data;
}

export async function register(email: string, password: string): Promise<UserDto> {
  const resp = await userApi.post<UserDto>("/auth/register", { email, password });
  return resp.data;
}

export async function getMe(): Promise<UserDto> {
  const resp = await userApi.get<UserDto>("/users");
  return resp.data;
}
