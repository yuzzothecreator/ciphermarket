"use client";

import { Badge, Button, Card, CardContent, CardHeader, CardTitle } from "@ciphermarket/ui";
import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { useState } from "react";
import { useAuth } from "@/lib/auth-context";
import { listEntitlements, listOrders } from "@/lib/buyer-api";

function formatPrice(cents: number, currency: string): string {
  return new Intl.NumberFormat("en-GB", {
    style: "currency",
    currency: currency || "GBP",
  }).format(cents / 100);
}

export function BuyerPortal() {
  const { accessToken, isAuthenticated } = useAuth();
  const [tab, setTab] = useState<"orders" | "entitlements">("orders");

  const ordersQuery = useQuery({
    queryKey: ["orders"],
    queryFn: () => listOrders(accessToken!),
    enabled: Boolean(accessToken),
  });

  const entitlementsQuery = useQuery({
    queryKey: ["entitlements"],
    queryFn: () => listEntitlements(accessToken!),
    enabled: Boolean(accessToken),
  });

  if (!isAuthenticated || !accessToken) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
        <h1 className="text-3xl font-semibold tracking-tight">Buyer portal</h1>
        <p className="mt-3 text-muted-foreground">Sign in to view purchases and entitlements.</p>
        <Button className="mt-6" asChild>
          <Link href="/auth/sign-in">Sign in</Link>
        </Button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
      <Badge variant="outline" className="mb-4">
        Phase 3
      </Badge>
      <h1 className="text-3xl font-semibold tracking-tight">Buyer portal</h1>
      <p className="mt-3 max-w-2xl text-muted-foreground">
        View order history and active entitlements granted after verified payment webhooks.
      </p>

      <div className="mt-8 flex gap-2">
        <Button variant={tab === "orders" ? "default" : "secondary"} onClick={() => setTab("orders")}>
          Orders
        </Button>
        <Button
          variant={tab === "entitlements" ? "default" : "secondary"}
          onClick={() => setTab("entitlements")}
        >
          Entitlements
        </Button>
      </div>

      <div className="mt-8">
        {tab === "orders" && (
          <>
            {ordersQuery.isLoading && <p className="text-sm text-muted-foreground">Loading orders…</p>}
            {ordersQuery.data?.length === 0 && (
              <Card>
                <CardContent className="py-10 text-center">
                  <p className="text-sm text-muted-foreground">No orders yet.</p>
                  <Button variant="secondary" className="mt-4" asChild>
                    <Link href="/catalogue">Browse catalogue</Link>
                  </Button>
                </CardContent>
              </Card>
            )}
            <ul className="space-y-4">
              {ordersQuery.data?.map((order) => (
                <li key={order.id}>
                  <Card>
                    <CardHeader className="flex flex-row items-center justify-between">
                      <CardTitle className="text-base">Order {order.id.slice(0, 8)}…</CardTitle>
                      <Badge variant={order.status === "PAID" ? "accent" : "outline"}>{order.status}</Badge>
                    </CardHeader>
                    <CardContent>
                      <p className="text-sm font-medium">
                        {formatPrice(order.subtotalCents, order.currency)}
                      </p>
                      <ul className="mt-3 space-y-1 text-sm text-muted-foreground">
                        {order.items.map((item) => (
                          <li key={item.id}>
                            {item.productName} × {item.quantity}
                          </li>
                        ))}
                      </ul>
                    </CardContent>
                  </Card>
                </li>
              ))}
            </ul>
          </>
        )}

        {tab === "entitlements" && (
          <>
            {entitlementsQuery.isLoading && (
              <p className="text-sm text-muted-foreground">Loading entitlements…</p>
            )}
            {entitlementsQuery.data?.length === 0 && (
              <Card>
                <CardContent className="py-10 text-center">
                  <p className="text-sm text-muted-foreground">No entitlements yet.</p>
                </CardContent>
              </Card>
            )}
            <ul className="grid gap-4 sm:grid-cols-2">
              {entitlementsQuery.data?.map((entitlement) => (
                <li key={entitlement.id}>
                  <Card>
                    <CardContent className="py-5">
                      <div className="flex items-center justify-between">
                        <p className="font-medium">Product {entitlement.productId.slice(0, 8)}…</p>
                        <Badge variant={entitlement.status === "ACTIVE" ? "accent" : "outline"}>
                          {entitlement.status}
                        </Badge>
                      </div>
                      <p className="mt-2 text-xs text-muted-foreground">
                        Granted {new Date(entitlement.grantedAt).toLocaleString()}
                      </p>
                    </CardContent>
                  </Card>
                </li>
              ))}
            </ul>
          </>
        )}
      </div>
    </div>
  );
}
