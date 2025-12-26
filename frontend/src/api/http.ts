// src/api/http.ts
import axios, { AxiosError } from "axios";
import { tokenStorage } from "./tokenStorage";

const userBaseURL = import.meta.env.VITE_USER_API_URL;   // "/api/users"
const orderBaseURL = import.meta.env.VITE_ORDER_API_URL; // "/api/orders"

export const userApi = axios.create({ baseURL: userBaseURL });
export const orderApi = axios.create({ baseURL: orderBaseURL });


export function getUserIdFromAccessToken(): string | null {
  const token = tokenStorage.get()?.accessToken;
  if (!token) return null;

  try {
    const payloadPart = token.split(".")[1];

    const base64 = payloadPart.replace(/-/g, "+").replace(/_/g, "/");
    const json = atob(base64);
    const payload = JSON.parse(json);
    return payload?.id ?? null;
  } catch {
    return null;
  }
}

function attachAuth(config: any) {
  const tokens = tokenStorage.get();

  if (tokens?.accessToken) {
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

userApi.interceptors.request.use(attachAuth);
orderApi.interceptors.request.use(attachAuth);

let isRefreshing = false;
let pending: Array<(token: string | null) => void> = [];

function notify(token: string | null) {
  pending.forEach((cb) => cb(token));
  pending = [];
}

async function refreshAccessToken(): Promise<string> {
  const tokens = tokenStorage.get();
  if (!tokens?.refreshToken) throw new Error("No refresh token");

  const resp = await userApi.post("/auth/refresh", {
    refreshToken: tokens.refreshToken,
  });

  const { token, refreshToken } = resp.data as {
    token: string;
    refreshToken: string;
  };

  tokenStorage.set({ accessToken: token, refreshToken });
  return token;
}

async function handle401(error: AxiosError) {
  const original: any = error.config;
  if (!original || original._retry) throw error;
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

        const instance = original.baseURL === orderBaseURL ? orderApi : userApi;
        resolve(instance(original));
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

    const instance = original.baseURL === orderBaseURL ? orderApi : userApi;
    return instance(original);
  } catch {
    tokenStorage.clear();
    notify(null);
    throw error;
  } finally {
    isRefreshing = false;
  }
}

userApi.interceptors.response.use(
  (r) => r,
  async (err) => {
    if (err?.response?.status === 401) return handle401(err);
    throw err;
  }
);

orderApi.interceptors.response.use(
  (r) => r,
  async (err) => {
    if (err?.response?.status === 401) return handle401(err);
    throw err;
  }
);
