import { Badge } from "@ciphermarket/ui";
import { CategoryList } from "@/components/category-list";

export const metadata = { title: "Catalogue" };

export default function CataloguePage() {
  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
      <Badge variant="outline" className="mb-4">
        Phase 3
      </Badge>
      <h1 className="text-3xl font-semibold tracking-tight">Product catalogue</h1>
      <p className="mt-3 max-w-2xl text-muted-foreground">
        Published products will appear here after Phase 3 commerce implementation. Categories below
        are loaded from the live API.
      </p>
      <div className="mt-10">
        <CategoryList />
      </div>
    </div>
  );
}
