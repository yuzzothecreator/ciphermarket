"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { buildSignOutUrl, getAccessToken, parseJwtRoles } from "@/lib/auth";

interface AuthContextValue {
  accessToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  roles: string[];
  isSecurityOps: boolean;
  isMarketplaceAdmin: boolean;
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

  const roles = useMemo(() => parseJwtRoles(accessToken), [accessToken]);
  const isMarketplaceAdmin = roles.includes("marketplace_admin");
  const isSecurityOps = isMarketplaceAdmin || roles.includes("security_auditor");

  const value = useMemo(
    () => ({
      accessToken,
      isAuthenticated: Boolean(accessToken),
      isLoading,
      roles,
      isSecurityOps,
      isMarketplaceAdmin,
      refresh,
      signOut,
    }),
    [accessToken, isLoading, roles, isSecurityOps, isMarketplaceAdmin, refresh, signOut],
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
