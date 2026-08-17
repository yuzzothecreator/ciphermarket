"use client";

import { Button, Card, CardContent, CardHeader, CardTitle } from "@ciphermarket/ui";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Loader2, Trash2 } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth-context";
import { checkout, getCart, removeCartItem, updateCartItem } from "@/lib/buyer-api";

function formatPrice(cents: number, currency: string): string {
  return new Intl.NumberFormat("en-GB", {
    style: "currency",
    currency: currency || "GBP",
  }).format(cents / 100);
}

export function CartView() {
  const { accessToken, isAuthenticated } = useAuth();
  const router = useRouter();
  const queryClient = useQueryClient();

  const cartQuery = useQuery({
    queryKey: ["cart"],
    queryFn: () => getCart(accessToken!),
    enabled: Boolean(accessToken),
  });

  const checkoutMutation = useMutation({
    mutationFn: () => checkout(accessToken!),
    onSuccess: (result) => {
      queryClient.invalidateQueries({ queryKey: ["cart"] });
      if (result.requiresPayment) {
        router.push(`/checkout/pay?paymentId=${result.paymentId}&orderId=${result.orderId}`);
      } else {
        router.push(`/checkout/complete?orderId=${result.orderId}`);
      }
    },
  });

  if (!isAuthenticated || !accessToken) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-20 text-center">
        <p className="text-muted-foreground">Sign in to view your cart.</p>
        <Button className="mt-4" asChild>
          <Link href="/auth/sign-in">Sign in</Link>
        </Button>
      </div>
    );
  }

  if (cartQuery.isLoading) {
    return <p className="mx-auto max-w-3xl px-4 py-12 text-sm text-muted-foreground">Loading cart…</p>;
  }

  const cart = cartQuery.data;

  return (
    <div className="mx-auto max-w-3xl px-4 py-12 sm:px-6">
      <h1 className="text-3xl font-semibold tracking-tight">Cart</h1>

      {!cart?.items.length ? (
        <Card className="mt-8">
          <CardContent className="py-10 text-center">
            <p className="text-sm text-muted-foreground">Your cart is empty.</p>
            <Button variant="secondary" className="mt-4" asChild>
              <Link href="/catalogue">Browse catalogue</Link>
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="mt-8 space-y-4">
          {cart.items.map((item) => (
            <Card key={item.id}>
              <CardContent className="flex flex-wrap items-center justify-between gap-4 py-4">
                <div>
                  <p className="font-medium">{item.productName}</p>
                  <p className="text-sm text-muted-foreground">
                    {formatPrice(item.unitPriceCents, item.currency)} each
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <input
                    type="number"
                    min={1}
                    max={99}
                    defaultValue={item.quantity}
                    className="h-9 w-16 rounded-lg border border-border bg-background px-2 text-sm"
                    onBlur={(e) => {
                      const qty = Number(e.target.value);
                      if (qty >= 1 && qty <= 99 && qty !== item.quantity) {
                        updateCartItem(accessToken, item.id, qty).then(() =>
                          queryClient.invalidateQueries({ queryKey: ["cart"] }),
                        );
                      }
                    }}
                  />
                  <Button
                    variant="ghost"
                    size="icon"
                    aria-label="Remove item"
                    onClick={() =>
                      removeCartItem(accessToken, item.id).then(() =>
                        queryClient.invalidateQueries({ queryKey: ["cart"] }),
                      )
                    }
                  >
                    <Trash2 className="size-4" />
                  </Button>
                </div>
                <p className="font-medium">{formatPrice(item.lineTotalCents, item.currency)}</p>
              </CardContent>
            </Card>
          ))}

          <Card>
            <CardHeader>
              <CardTitle>Total: {formatPrice(cart.subtotalCents, cart.currency)}</CardTitle>
            </CardHeader>
            <CardContent>
              <Button
                className="w-full"
                disabled={checkoutMutation.isPending}
                onClick={() => checkoutMutation.mutate()}
              >
                {checkoutMutation.isPending && <Loader2 className="mr-2 size-4 animate-spin" />}
                Proceed to checkout
              </Button>
              {checkoutMutation.isError && (
                <p className="mt-3 text-sm text-destructive">
                  {checkoutMutation.error instanceof Error
                    ? checkoutMutation.error.message
                    : "Checkout failed."}
                </p>
              )}
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
