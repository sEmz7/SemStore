export type Tokens = {
  accessToken: string;
  refreshToken: string;
};

const KEY = "semstore_tokens";

export const tokenStorage = {
  get(): Tokens | null {
    const raw = localStorage.getItem(KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as Tokens;
    } catch {
      return null;
    }
  },
  set(tokens: Tokens) {
    localStorage.setItem(KEY, JSON.stringify(tokens));
  },
  clear() {
    localStorage.removeItem(KEY);
  },
};
