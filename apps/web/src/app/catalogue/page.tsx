import { Badge } from "@ciphermarket/ui";
import { CategoryList } from "@/components/category-list";
import { ProductCatalogue } from "@/components/product-catalogue";

export const metadata = { title: "Catalogue" };

export default function CataloguePage() {
  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
      <Badge variant="outline" className="mb-4">
        Phase 3
      </Badge>
      <h1 className="text-3xl font-semibold tracking-tight">Product catalogue</h1>
      <p className="mt-3 max-w-2xl text-muted-foreground">
        Browse published digital products from verified creators. Purchases grant entitlements after
        signed payment webhooks confirm on the server.
      </p>
      <div className="mt-10">
        <ProductCatalogue />
      </div>
      <section className="mt-16">
        <h2 className="text-lg font-medium">Categories</h2>
        <div className="mt-4">
          <CategoryList />
        </div>
      </section>
    </div>
  );
}
