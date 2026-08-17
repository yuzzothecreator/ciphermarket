"use client";

import type { CatalogueProduct } from "@ciphermarket/contracts";
import { Badge } from "@ciphermarket/ui";
import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { listCatalogueProducts } from "@/lib/buyer-api";

function formatPrice(cents: number, currency: string): string {
  return new Intl.NumberFormat("en-GB", {
    style: "currency",
    currency: currency || "GBP",
  }).format(cents / 100);
}

export function ProductCatalogue() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ["catalogue-products"],
    queryFn: () => listCatalogueProducts(),
  });

  if (isLoading) {
    return <p className="text-sm text-muted-foreground">Loading products…</p>;
  }

  if (isError) {
    return (
      <p className="text-sm text-destructive">
        Unable to load catalogue. Ensure the API is running and products are published.
      </p>
    );
  }

  if (!data?.length) {
    return (
      <p className="text-sm text-muted-foreground">
        No published products yet. Creators can publish listings from Creator Studio.
      </p>
    );
  }

  return (
    <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {data.map((product: CatalogueProduct) => (
        <li key={product.id}>
          <Link
            href={`/catalogue/${product.id}`}
            className="block rounded-xl border border-border bg-card p-5 transition-colors hover:border-accent/40"
          >
            <div className="flex items-start justify-between gap-2">
              <h3 className="font-medium">{product.name}</h3>
              <Badge variant="outline">{product.productType.replace("_", " ")}</Badge>
            </div>
            <p className="mt-2 text-sm text-muted-foreground">
              {product.shortDescription || product.slug}
            </p>
            <p className="mt-3 text-sm font-medium">
              {product.priceCents === 0 ? "Free" : formatPrice(product.priceCents, product.currency)}
            </p>
          </Link>
        </li>
      ))}
    </ul>
  );
}
