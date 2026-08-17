import { Button } from "@ciphermarket/ui";
import Link from "next/link";

export const metadata = { title: "Creator Studio" };

export default function CreatorPage() {
  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
      <h1 className="text-3xl font-semibold tracking-tight">Creator Studio</h1>
      <p className="mt-3 max-w-2xl text-muted-foreground">
        Manage products, secure uploads, and organisation members. Authentication via Keycloak is
        required — sign in to access creator features in Phase 2.
      </p>
      <div className="mt-8 rounded-xl border border-dashed border-border p-10 text-center">
        <p className="text-sm text-muted-foreground">No products yet. Phase 2 will enable the full studio.</p>
        <Button className="mt-4" asChild>
          <Link href="/auth/sign-in">Sign in with Keycloak</Link>
        </Button>
      </div>
    </div>
  );
}
