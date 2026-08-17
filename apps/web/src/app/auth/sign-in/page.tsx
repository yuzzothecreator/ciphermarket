"use client";

import { useEffect } from "react";
import { buildSignInUrl } from "@/lib/auth";

export default function SignInPage() {
  useEffect(() => {
    buildSignInUrl("/creator").then((url) => {
      window.location.href = url;
    });
  }, []);

  return (
    <div className="mx-auto flex max-w-md flex-col gap-6 px-4 py-20 sm:px-6">
      <h1 className="text-2xl font-semibold tracking-tight">Sign in</h1>
      <p className="text-sm text-muted-foreground">
        Redirecting to Keycloak for secure authentication…
      </p>
    </div>
  );
}
