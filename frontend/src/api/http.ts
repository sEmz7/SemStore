import axios, { AxiosError } from "axios";
import { tokenStorage } from "./tokenStorage";

const userBaseURL = import.meta.env.VITE_USER_API_URL ?? "/api/users";
const orderBaseURL = import.meta.env.VITE_ORDER_API_URL ?? "/api/orders";
const authBaseURL = import.meta.env.VITE_AUTH_API_URL ?? "/api";

export const userApi = axios.create({ baseURL: userBaseURL });
export const orderApi = axios.create({ baseURL: orderBaseURL });
export const authApi = axios.create({ baseURL: authBaseURL });

const refreshApi = axios.create({ baseURL: authBaseURL });

const NO_BEARER_PATHS = new Set(["/auth/login", "/auth/register", "/auth/refresh"]);

function normalizeUrl(u: string): string {
  const q = u.indexOf("?");
  return q === -1 ? u : u.slice(0, q);
}

function shouldSkipBearer(config: any): boolean {
  const url = normalizeUrl(String(config?.url ?? ""));
  return NO_BEARER_PATHS.has(url);
}

export function getUserIdFromAccessToken(): string | null {
  const token = tokenStorage.get()?.accessToken;
  if (!token) return null;

  try {
    const payloadPart = token.split(".")[1];
    let base64 = payloadPart.replace(/-/g, "+").replace(/_/g, "/");
    while (base64.length % 4 !== 0) base64 += "=";

    const json = atob(base64);
    const payload = JSON.parse(json);
    return payload?.id ?? null;
  } catch {
    return null;
  }
}

function attachAuth(config: any) {
  const tokens = tokenStorage.get();

  if (tokens?.accessToken && !shouldSkipBearer(config)) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${tokens.accessToken}`;
  }

  if (config.baseURL === orderBaseURL) {
    const userId = getUserIdFromAccessToken();
    if (userId) {
      config.headers = config.headers ?? {};
      config.headers["X-User-Id"] = userId;
    }
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
  const tokens = tokenStorage.get();
  if (!tokens?.refreshToken) throw new Error("No refresh token");

  const resp = await refreshApi.post("/auth/refresh", {
    refreshToken: tokens.refreshToken,
  });

  const { token, refreshToken } = resp.data as { token: string; refreshToken: string };
  tokenStorage.set({ accessToken: token, refreshToken });
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
  return err?.response?.status === 401;
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

        if (original.baseURL === orderBaseURL) {
          const userId = getUserIdFromAccessToken();
          if (userId) original.headers["X-User-Id"] = userId;
        }

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

    if (original.baseURL === orderBaseURL) {
      const userId = getUserIdFromAccessToken();
      if (userId) original.headers["X-User-Id"] = userId;
    }

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
