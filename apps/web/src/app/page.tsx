import { Badge, Button, SecurityFeatureGrid } from "@ciphermarket/ui";
import { ArrowRight, ShieldCheck } from "lucide-react";
import Link from "next/link";
import { ApiHealthBadge } from "@/components/api-health-badge";
import { CategoryList } from "@/components/category-list";

export default function HomePage() {
  return (
    <div>
      <section className="border-b border-border">
        <div className="mx-auto grid max-w-7xl gap-10 px-4 py-20 sm:px-6 lg:grid-cols-2 lg:items-center">
          <div className="space-y-6">
            <Badge variant="accent" className="gap-1">
              <ShieldCheck className="size-3.5" aria-hidden />
              Zero-trust marketplace
            </Badge>
            <h1 className="text-4xl font-semibold tracking-tight sm:text-5xl">
              Secure digital product distribution, licensing and leak tracing
            </h1>
            <p className="max-w-xl text-lg text-muted-foreground">
              Sell PDFs, source code, and design assets with envelope encryption, verified payments,
              buyer-specific delivery, and complete security auditing.
            </p>
            <div className="flex flex-wrap items-center gap-3">
              <Button asChild>
                <Link href="/catalogue">
                  Browse catalogue
                  <ArrowRight className="size-4" aria-hidden />
                </Link>
              </Button>
              <Button variant="secondary" asChild>
                <Link href="/creator">Open Creator Studio</Link>
              </Button>
              <ApiHealthBadge />
            </div>
          </div>
          <SecurityFeatureGrid />
        </div>
      </section>

      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6">
        <div className="mb-8 flex items-end justify-between gap-4">
          <div>
            <h2 className="text-2xl font-semibold tracking-tight">Categories</h2>
            <p className="mt-2 text-muted-foreground">Live data from the CipherMarket API.</p>
          </div>
          <Link href="/catalogue" className="text-sm text-accent hover:underline">
            View all
          </Link>
        </div>
        <CategoryList />
      </section>
    </div>
  );
}
