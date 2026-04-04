// src/api/http.ts
import axios, { AxiosError } from "axios";
import { tokenStorage } from "./tokenStorage";

const userBaseURL = import.meta.env.VITE_USER_API_URL ?? "/api/users";
const orderBaseURL = import.meta.env.VITE_ORDER_API_URL ?? "/api/orders";
const authBaseURL = import.meta.env.VITE_AUTH_API_URL ?? "/api";

export const userApi = axios.create({ baseURL: userBaseURL, withCredentials: true });
export const orderApi = axios.create({ baseURL: orderBaseURL, withCredentials: true });
export const authApi = axios.create({ baseURL: authBaseURL, withCredentials: true });

const refreshApi = axios.create({ baseURL: authBaseURL, withCredentials: true });

const NO_BEARER_PATHS = new Set([
  "/auth/login",
  "/auth/register",
  "/auth/refresh",
  "/auth/verify-email",
  "/auth/resend-verification-code",
]);

function normalizeUrl(u: string): string {
  const q = u.indexOf("?");
  return q === -1 ? u : u.slice(0, q);
}

function shouldSkipBearer(config: any): boolean {
  const url = normalizeUrl(String(config?.url ?? ""));
  return NO_BEARER_PATHS.has(url);
}


function attachAuth(config: any) {
  const tokens = tokenStorage.get();

  if (tokens?.accessToken && !shouldSkipBearer(config)) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${tokens.accessToken}`;
  }

  return config;
}

[userApi, orderApi, authApi].forEach((api) => api.interceptors.request.use(attachAuth));

let isRefreshing = false;
let pending: Array<(token: string | null) => void> = [];

function notify(token: string | null) {
  pending.forEach((cb) => cb(token));
  pending = [];
}

async function refreshAccessToken(): Promise<string> {
  const resp = await refreshApi.post("/auth/refresh");

  const bodyToken = (resp.data as any)?.token;
  const headerAuth = (resp.headers as any)?.authorization ?? (resp.headers as any)?.Authorization;

  const headerToken =
    typeof headerAuth === "string" && headerAuth.toLowerCase().startsWith("bearer ")
      ? headerAuth.slice(7)
      : null;

  const token: string | null = bodyToken ?? headerToken;

  if (!token) throw new Error("No access token in refresh response");

  tokenStorage.set({ accessToken: token });
  return token;
}

function pickInstance(cfg: any) {
  if (cfg?.baseURL === orderBaseURL) return orderApi;
  if (cfg?.baseURL === authBaseURL) return authApi;
  return userApi;
}

function shouldHandle401(err: any): boolean {
  const cfg = err?.config;
  if (!cfg) return false;
  if (shouldSkipBearer(cfg)) return false;
  if (err?.response?.status !== 401) return false;

  const h = cfg.headers ?? {};
  const authHeader = h.Authorization ?? h.authorization;
  if (!authHeader) return false;

  return true;
}

async function handle401(error: AxiosError) {
  const original: any = error.config;
  if (!original || original._retry) throw error;
  if (shouldSkipBearer(original)) throw error;

  original._retry = true;

  if (isRefreshing) {
    return new Promise((resolve, reject) => {
      pending.push((token) => {
        if (!token) return reject(error);

        original.headers = original.headers ?? {};
        original.headers.Authorization = `Bearer ${token}`;

        resolve(pickInstance(original)(original));
      });
    });
  }

  isRefreshing = true;
  try {
    const newToken = await refreshAccessToken();
    notify(newToken);

    original.headers = original.headers ?? {};
    original.headers.Authorization = `Bearer ${newToken}`;

    return pickInstance(original)(original);
  } catch {
    tokenStorage.clear();
    notify(null);
    throw error;
  } finally {
    isRefreshing = false;
  }
}

[userApi, orderApi, authApi].forEach((api) => {
  api.interceptors.response.use(
    (r) => r,
    async (err) => {
      if (shouldHandle401(err)) return handle401(err);
      throw err;
    }
  );
});
