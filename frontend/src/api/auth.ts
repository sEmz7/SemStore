// src/api/auth.ts
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

export async function logout(): Promise<void> {
  await authApi.post("/auth/logout");
}

export async function verifyEmail(email: string, code: string): Promise<void> {
  await authApi.post("/auth/verify-email", { email, code });
}

export async function resendVerificationCode(email: string): Promise<void> {
  await authApi.post("/auth/resend-verification-code", { email });
}