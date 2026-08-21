"use client";

import type { CatalogueProduct } from "@ciphermarket/contracts";
import { Badge } from "@ciphermarket/ui";
import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { useParams } from "next/navigation";
import { getCreatorStorefront } from "@/lib/buyer-api";

function formatPrice(cents: number, currency: string): string {
  return new Intl.NumberFormat("en-GB", {
    style: "currency",
    currency: currency || "GBP",
  }).format(cents / 100);
}

export function CreatorStorefrontView() {
  const params = useParams<{ slug: string }>();
  const storefrontQuery = useQuery({
    queryKey: ["creator-storefront", params.slug],
    queryFn: () => getCreatorStorefront(params.slug),
  });

  if (storefrontQuery.isLoading) {
    return <p className="text-sm text-muted-foreground">Loading storefront…</p>;
  }

  if (storefrontQuery.isError || !storefrontQuery.data) {
    return <p className="text-sm text-destructive">Creator storefront not found.</p>;
  }

  const { organisation, products } = storefrontQuery.data;

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
      <Link href="/catalogue" className="text-sm text-muted-foreground hover:text-foreground">
        ← Back to catalogue
      </Link>
      <Badge variant="outline" className="mb-4 mt-6">
        Creator storefront
      </Badge>
      <h1 className="text-3xl font-semibold tracking-tight">{organisation.name}</h1>
      <p className="mt-3 max-w-2xl text-muted-foreground">
        {organisation.description || `Published products from @${organisation.slug}`}
      </p>

      <div className="mt-10">
        {!products.length ? (
          <p className="text-sm text-muted-foreground">No published products yet.</p>
        ) : (
          <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {products.map((product: CatalogueProduct) => (
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
                    {product.priceCents === 0
                      ? "Free"
                      : formatPrice(product.priceCents, product.currency)}
                  </p>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
