import { Button } from "@ciphermarket/ui";
import Link from "next/link";

export const metadata = { title: "Buyer Portal" };

export default function BuyerPage() {
  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
      <h1 className="text-3xl font-semibold tracking-tight">Buyer portal</h1>
      <p className="mt-3 max-w-2xl text-muted-foreground">
        View purchases, licences, download history, and registered devices after signing in.
      </p>
      <div className="mt-8 rounded-xl border border-dashed border-border p-10 text-center">
        <p className="text-sm text-muted-foreground">No purchases yet.</p>
        <Button variant="secondary" className="mt-4" asChild>
          <Link href="/catalogue">Browse catalogue</Link>
        </Button>
      </div>
    </div>
  );
}
