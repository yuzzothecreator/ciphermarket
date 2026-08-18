"use client";

import type { Entitlement } from "@ciphermarket/contracts";
import { Badge, Button, Card, CardContent, CardHeader, CardTitle } from "@ciphermarket/ui";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Download, Loader2 } from "lucide-react";
import Link from "next/link";
import { useState } from "react";
import { useAuth } from "@/lib/auth-context";
import {
  createAccessGrant,
  getDownloadUrl,
  issueLicence,
  listEntitlements,
  listOrders,
} from "@/lib/buyer-api";

function formatPrice(cents: number, currency: string): string {
  return new Intl.NumberFormat("en-GB", {
    style: "currency",
    currency: currency || "GBP",
  }).format(cents / 100);
}

function EntitlementCard({
  entitlement,
  token,
}: {
  entitlement: Entitlement;
  token: string;
}) {
  const [error, setError] = useState<string | null>(null);

  const downloadMutation = useMutation({
    mutationFn: async () => {
      if (!entitlement.hasLicence) {
        await issueLicence(token, entitlement.id);
      }
      const grant = await createAccessGrant(token, entitlement.id);
      return grant;
    },
    onSuccess: (grant) => {
      setError(null);
      window.open(getDownloadUrl(grant.accessToken), "_blank", "noopener,noreferrer");
    },
    onError: (err: unknown) => {
      setError(err instanceof Error ? err.message : "Download failed.");
    },
  });

  return (
    <Card>
      <CardContent className="py-5">
        <div className="flex items-start justify-between gap-2">
          <div>
            <p className="font-medium">{entitlement.productName}</p>
            <p className="text-xs text-muted-foreground">{entitlement.productType.replace("_", " ")}</p>
          </div>
          <Badge variant={entitlement.status === "ACTIVE" ? "accent" : "outline"}>
            {entitlement.status}
          </Badge>
        </div>
        <p className="mt-2 text-xs text-muted-foreground">
          Granted {new Date(entitlement.grantedAt).toLocaleString()}
        </p>
        {entitlement.status === "ACTIVE" && (
          <Button
            className="mt-4 w-full"
            size="sm"
            disabled={downloadMutation.isPending}
            onClick={() => downloadMutation.mutate()}
          >
            {downloadMutation.isPending ? (
              <Loader2 className="mr-2 size-4 animate-spin" />
            ) : (
              <Download className="mr-2 size-4" />
            )}
            Secure download
          </Button>
        )}
        {error && <p className="mt-2 text-xs text-destructive">{error}</p>}
      </CardContent>
    </Card>
  );
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
        Phase 4
      </Badge>
      <h1 className="text-3xl font-semibold tracking-tight">Buyer portal</h1>
      <p className="mt-3 max-w-2xl text-muted-foreground">
        View orders and download purchased products via short-lived access grants. PDFs are
        watermarked; source archives include signed manifests.
      </p>

      <div className="mt-8 flex gap-2">
        <Button variant={tab === "orders" ? "default" : "secondary"} onClick={() => setTab("orders")}>
          Orders
        </Button>
        <Button
          variant={tab === "entitlements" ? "default" : "secondary"}
          onClick={() => setTab("entitlements")}
        >
          Downloads
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
                  <EntitlementCard entitlement={entitlement} token={accessToken} />
                </li>
              ))}
            </ul>
          </>
        )}
      </div>
    </div>
  );
}
