"use client";

import type { SalesAnalytics } from "@ciphermarket/contracts";
import { Badge, Card, CardContent, CardHeader, CardTitle } from "@ciphermarket/ui";
import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { getSalesAnalytics } from "@/lib/creator-api";

function formatPrice(cents: number, currency: string): string {
  return new Intl.NumberFormat("en-GB", {
    style: "currency",
    currency: currency || "GBP",
  }).format(cents / 100);
}

export function SalesAnalyticsPanel({
  token,
  organisationId,
  organisationSlug,
}: {
  token: string;
  organisationId: string;
  organisationSlug?: string;
}) {
  const analyticsQuery = useQuery({
    queryKey: ["sales-analytics", organisationId],
    queryFn: () => getSalesAnalytics(token, organisationId),
    enabled: Boolean(token && organisationId),
  });

  const data: SalesAnalytics | undefined = analyticsQuery.data;

  return (
    <section className="mt-10 space-y-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <Badge variant="outline" className="mb-2">
            Sales
          </Badge>
          <h2 className="text-xl font-semibold tracking-tight">Sales analytics</h2>
          <p className="mt-1 text-sm text-muted-foreground">
            Paid orders only. Refunded sales are excluded from these totals.
          </p>
        </div>
        {organisationSlug && (
          <Link
            href={`/creators/${organisationSlug}`}
            className="text-sm text-muted-foreground hover:text-foreground"
          >
            View public storefront →
          </Link>
        )}
      </div>

      {analyticsQuery.isLoading && (
        <p className="text-sm text-muted-foreground">Loading sales…</p>
      )}
      {analyticsQuery.isError && (
        <p className="text-sm text-destructive">Unable to load sales analytics.</p>
      )}

      {data && (
        <>
          <div className="grid gap-3 sm:grid-cols-3">
            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Paid orders
                </CardTitle>
              </CardHeader>
              <CardContent className="text-2xl font-semibold">{data.paidOrderCount}</CardContent>
            </Card>
            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">
                  Units sold
                </CardTitle>
              </CardHeader>
              <CardContent className="text-2xl font-semibold">{data.unitsSold}</CardContent>
            </Card>
            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium text-muted-foreground">Revenue</CardTitle>
              </CardHeader>
              <CardContent className="text-2xl font-semibold">
                {formatPrice(data.revenueCents, data.currency)}
              </CardContent>
            </Card>
          </div>

          {data.products.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle>By product</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-3">
                  {data.products.map((row) => (
                    <li
                      key={row.productId}
                      className="flex flex-wrap items-center justify-between gap-2 text-sm"
                    >
                      <span className="font-medium">{row.productName}</span>
                      <span className="text-muted-foreground">
                        {row.unitsSold} units · {formatPrice(row.revenueCents, row.currency)}
                      </span>
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          )}
        </>
      )}
    </section>
  );
}
