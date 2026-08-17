"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { buildSignOutUrl, getAccessToken } from "@/lib/auth";

interface AuthContextValue {
  accessToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  refresh: () => void;
  signOut: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const refresh = useCallback(() => {
    setAccessToken(getAccessToken());
  }, []);

  useEffect(() => {
    refresh();
    setIsLoading(false);
  }, [refresh]);

  const signOut = useCallback(() => {
    window.location.href = buildSignOutUrl();
  }, []);

  const value = useMemo(
    () => ({
      accessToken,
      isAuthenticated: Boolean(accessToken),
      isLoading,
      refresh,
      signOut,
    }),
    [accessToken, isLoading, refresh, signOut],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
