import { authApi, userApi } from "./http";
import type { JwtAuthDto, UserDto } from "./types";

export async function login(email: string, password: string): Promise<JwtAuthDto> {
  const resp = await authApi.post<JwtAuthDto>("/auth/login", { email, password });
  return resp.data;
}

export async function register(email: string, password: string): Promise<UserDto> {
  const resp = await authApi.post<UserDto>("/auth/register", { email, password });
  return resp.data;
}

export async function getMe(): Promise<UserDto> {
  const resp = await userApi.get<UserDto>("");
  return resp.data;
}
