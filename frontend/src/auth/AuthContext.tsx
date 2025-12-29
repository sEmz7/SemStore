import React, { createContext, useContext, useEffect, useMemo, useState } from "react";
import { getMe, login as apiLogin } from "../api/auth";
import { tokenStorage } from "../api/tokenStorage";
import type { UserDto } from "../api/types";

type AuthState = {
  user: UserDto | null;
  isLoading: boolean;
  login(email: string, password: string): Promise<void>;
  logout(): void;
  reloadMe(): Promise<void>;
};

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<UserDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  async function reloadMe() {
    const tokens = tokenStorage.get();
    if (!tokens?.accessToken) {
      setUser(null);
      return;
    }
    const me = await getMe();
    setUser(me);
  }

  async function login(email: string, password: string) {
    const tokens = await apiLogin(email, password);

    tokenStorage.set({ accessToken: tokens.token });

    await reloadMe();
  }

  function logout() {
    tokenStorage.clear();
    setUser(null);
  }

  useEffect(() => {
    (async () => {
      try {
        await reloadMe();
      } catch {
        tokenStorage.clear();
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    })();
  }, []);

  const value = useMemo(
    () => ({ user, isLoading, login, logout, reloadMe }),
    [user, isLoading]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
