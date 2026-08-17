"use client";

import { Button, Card, CardContent, CardHeader, CardTitle } from "@ciphermarket/ui";
import { useMutation } from "@tanstack/react-query";
import { Loader2 } from "lucide-react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense } from "react";
import { useAuth } from "@/lib/auth-context";
import { simulatePayment } from "@/lib/buyer-api";

function PayContent() {
  const searchParams = useSearchParams();
  const paymentId = searchParams.get("paymentId");
  const orderId = searchParams.get("orderId");
  const { accessToken, isAuthenticated } = useAuth();
  const router = useRouter();

  const payMutation = useMutation({
    mutationFn: () => simulatePayment(accessToken!, paymentId!),
    onSuccess: () => router.push(`/checkout/complete?orderId=${orderId}`),
  });

  if (!isAuthenticated || !accessToken) {
    return (
      <div className="mx-auto max-w-md px-4 py-20 text-center">
        <p className="text-muted-foreground">Sign in to complete payment.</p>
        <Button className="mt-4" asChild>
          <Link href="/auth/sign-in">Sign in</Link>
        </Button>
      </div>
    );
  }

  if (!paymentId || !orderId) {
    return <p className="mx-auto max-w-md px-4 py-20 text-destructive">Invalid checkout session.</p>;
  }

  return (
    <div className="mx-auto max-w-md px-4 py-20 sm:px-6">
      <Card>
        <CardHeader>
          <CardTitle>Mock payment</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-sm text-muted-foreground">
            This simulates a payment provider redirect. Entitlements are granted only after the
            signed server webhook confirms payment — not from this page alone.
          </p>
          <Button
            className="w-full"
            disabled={payMutation.isPending}
            onClick={() => payMutation.mutate()}
          >
            {payMutation.isPending && <Loader2 className="mr-2 size-4 animate-spin" />}
            Complete mock payment
          </Button>
          {payMutation.isError && (
            <p className="text-sm text-destructive">
              {payMutation.error instanceof Error ? payMutation.error.message : "Payment failed."}
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

export function CheckoutPayView() {
  return (
    <Suspense fallback={<p className="mx-auto max-w-md px-4 py-20 text-sm text-muted-foreground">Loading…</p>}>
      <PayContent />
    </Suspense>
  );
}
