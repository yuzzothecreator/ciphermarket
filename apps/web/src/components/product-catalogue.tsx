"use client";

import type { CatalogueProduct, CatalogueSort, ProductType } from "@ciphermarket/contracts";
import { Badge, Button } from "@ciphermarket/ui";
import { useQuery } from "@tanstack/react-query";
import Link from "next/link";
import { useMemo, useState } from "react";
import { listCatalogueProducts } from "@/lib/buyer-api";

const inputClass =
  "flex h-10 w-full rounded-lg border border-border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring";

const productTypes: Array<ProductType | ""> = ["", "PDF", "SOURCE_CODE", "DESIGN", "GENERAL"];
const sorts: CatalogueSort[] = ["NEWEST", "PRICE_ASC", "PRICE_DESC", "NAME_ASC"];

function formatPrice(cents: number, currency: string): string {
  return new Intl.NumberFormat("en-GB", {
    style: "currency",
    currency: currency || "GBP",
  }).format(cents / 100);
}

export function ProductCatalogue() {
  const [q, setQ] = useState("");
  const [productType, setProductType] = useState<ProductType | "">("");
  const [sort, setSort] = useState<CatalogueSort>("NEWEST");
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [appliedQ, setAppliedQ] = useState("");

  const params = useMemo(
    () => ({
      q: appliedQ || undefined,
      productType: productType || undefined,
      sort,
      minPriceCents: minPrice ? Math.round(Number(minPrice) * 100) : undefined,
      maxPriceCents: maxPrice ? Math.round(Number(maxPrice) * 100) : undefined,
    }),
    [appliedQ, productType, sort, minPrice, maxPrice],
  );

  const { data, isLoading, isError } = useQuery({
    queryKey: ["catalogue-products", params],
    queryFn: () => listCatalogueProducts(params),
  });

  return (
    <div className="space-y-6">
      <form
        className="grid gap-3 sm:grid-cols-2 lg:grid-cols-6"
        onSubmit={(e) => {
          e.preventDefault();
          setAppliedQ(q.trim());
        }}
      >
        <input
          className={`${inputClass} lg:col-span-2`}
          placeholder="Search products"
          value={q}
          onChange={(e) => setQ(e.target.value)}
        />
        <select
          className={inputClass}
          value={productType}
          onChange={(e) => setProductType(e.target.value as ProductType | "")}
        >
          {productTypes.map((type) => (
            <option key={type || "all"} value={type}>
              {type ? type.replace("_", " ") : "All types"}
            </option>
          ))}
        </select>
        <select
          className={inputClass}
          value={sort}
          onChange={(e) => setSort(e.target.value as CatalogueSort)}
        >
          {sorts.map((value) => (
            <option key={value} value={value}>
              {value.replace("_", " ")}
            </option>
          ))}
        </select>
        <input
          className={inputClass}
          type="number"
          min="0"
          step="0.01"
          placeholder="Min price"
          value={minPrice}
          onChange={(e) => setMinPrice(e.target.value)}
        />
        <input
          className={inputClass}
          type="number"
          min="0"
          step="0.01"
          placeholder="Max price"
          value={maxPrice}
          onChange={(e) => setMaxPrice(e.target.value)}
        />
        <Button type="submit" className="sm:col-span-2 lg:col-span-6 lg:w-fit">
          Apply filters
        </Button>
      </form>

      {isLoading && <p className="text-sm text-muted-foreground">Loading products…</p>}
      {isError && (
        <p className="text-sm text-destructive">
          Unable to load catalogue. Ensure the API is running and products are published.
        </p>
      )}
      {!isLoading && !isError && data?.length === 0 && (
        <p className="text-sm text-muted-foreground">No products match these filters.</p>
      )}
      {!!data?.length && (
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
  );
}
