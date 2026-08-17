"use client";

import { Button, Card, CardContent } from "@ciphermarket/ui";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Suspense } from "react";

function CompleteContent() {
  const searchParams = useSearchParams();
  const orderId = searchParams.get("orderId");

  return (
    <div className="mx-auto max-w-md px-4 py-20 sm:px-6">
      <Card>
        <CardContent className="py-10 text-center">
          <h1 className="text-2xl font-semibold">Order complete</h1>
          <p className="mt-3 text-sm text-muted-foreground">
            {orderId
              ? `Order ${orderId} has been processed. Your entitlements are active once payment is verified.`
              : "Your order has been processed."}
          </p>
          <div className="mt-6 flex flex-col gap-2">
            <Button asChild>
              <Link href="/buyer">View purchases</Link>
            </Button>
            <Button variant="secondary" asChild>
              <Link href="/catalogue">Continue shopping</Link>
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

export function CheckoutCompleteView() {
  return (
    <Suspense fallback={<p className="mx-auto max-w-md px-4 py-20 text-sm text-muted-foreground">Loading…</p>}>
      <CompleteContent />
    </Suspense>
  );
}
