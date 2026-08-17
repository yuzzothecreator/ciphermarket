"use client";

import { Badge, Button, Card, CardContent, CardHeader, CardTitle } from "@ciphermarket/ui";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Loader2, ShoppingCart } from "lucide-react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth-context";
import { addToCart, getCatalogueProduct } from "@/lib/buyer-api";

function formatPrice(cents: number, currency: string): string {
  return new Intl.NumberFormat("en-GB", {
    style: "currency",
    currency: currency || "GBP",
  }).format(cents / 100);
}

export function ProductDetailView() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const { accessToken, isAuthenticated } = useAuth();
  const queryClient = useQueryClient();

  const productQuery = useQuery({
    queryKey: ["catalogue-product", params.id],
    queryFn: () => getCatalogueProduct(params.id),
  });

  const addMutation = useMutation({
    mutationFn: () => addToCart(accessToken!, { productId: params.id, quantity: 1 }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["cart"] });
      router.push("/cart");
    },
  });

  if (productQuery.isLoading) {
    return <p className="text-sm text-muted-foreground">Loading product…</p>;
  }

  if (productQuery.isError || !productQuery.data) {
    return <p className="text-sm text-destructive">Product not found or unavailable.</p>;
  }

  const product = productQuery.data;

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
      <Link href="/catalogue" className="text-sm text-muted-foreground hover:text-foreground">
        ← Back to catalogue
      </Link>

      <div className="mt-6 grid gap-8 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <div className="flex items-center gap-3">
            <h1 className="text-3xl font-semibold tracking-tight">{product.name}</h1>
            <Badge variant="outline">{product.productType.replace("_", " ")}</Badge>
          </div>
          <p className="mt-4 text-muted-foreground">{product.fullDescription || product.shortDescription}</p>
          {product.usageTerms && (
            <section className="mt-8">
              <h2 className="font-medium">Usage terms</h2>
              <p className="mt-2 text-sm text-muted-foreground">{product.usageTerms}</p>
            </section>
          )}
        </div>

        <Card>
          <CardHeader>
            <CardTitle>
              {product.priceCents === 0 ? "Free" : formatPrice(product.priceCents, product.currency)}
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {product.licenceType && (
              <p className="text-sm text-muted-foreground">Licence: {product.licenceType}</p>
            )}
            {!isAuthenticated || !accessToken ? (
              <Button asChild className="w-full">
                <Link href="/auth/sign-in">Sign in to purchase</Link>
              </Button>
            ) : (
              <Button
                className="w-full"
                disabled={addMutation.isPending}
                onClick={() => addMutation.mutate()}
              >
                {addMutation.isPending ? (
                  <Loader2 className="mr-2 size-4 animate-spin" />
                ) : (
                  <ShoppingCart className="mr-2 size-4" />
                )}
                Add to cart
              </Button>
            )}
            {addMutation.isError && (
              <p className="text-sm text-destructive">
                {addMutation.error instanceof Error ? addMutation.error.message : "Failed to add to cart."}
              </p>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
