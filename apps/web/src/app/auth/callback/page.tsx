"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useEffect, useState } from "react";
import { consumeAuthReturnTo, exchangeAuthorizationCode } from "@/lib/auth";
import { useAuth } from "@/lib/auth-context";

function AuthCallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { refresh } = useAuth();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const code = searchParams.get("code");
    const authError = searchParams.get("error_description") ?? searchParams.get("error");

    if (authError) {
      setError(authError);
      return;
    }

    if (!code) {
      setError("Missing authorization code.");
      return;
    }

    exchangeAuthorizationCode(code)
      .then(() => {
        refresh();
        router.replace(consumeAuthReturnTo());
      })
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : "Authentication failed.");
      });
  }, [refresh, router, searchParams]);

  if (error) {
    return (
      <div className="mx-auto max-w-md px-4 py-20 text-center">
        <h1 className="text-xl font-semibold">Sign-in failed</h1>
        <p className="mt-3 text-sm text-destructive">{error}</p>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-md px-4 py-20 text-center">
      <p className="text-sm text-muted-foreground">Completing sign-in…</p>
    </div>
  );
}

export default function AuthCallbackPage() {
  return (
    <Suspense
      fallback={
        <div className="mx-auto max-w-md px-4 py-20 text-center">
          <p className="text-sm text-muted-foreground">Completing sign-in…</p>
        </div>
      }
    >
      <AuthCallbackContent />
    </Suspense>
  );
}
