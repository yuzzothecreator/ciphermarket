import { SecurityFeatureGrid } from "@ciphermarket/ui";

export const metadata = { title: "Security & Buyer Protection" };

export default function SecurityPage() {
  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
      <h1 className="text-3xl font-semibold tracking-tight">Security & buyer protection</h1>
      <p className="mt-3 max-w-3xl text-muted-foreground">
        CipherMarket uses established cryptographic standards, verified payment webhooks, and
        tenant-isolated access controls. We document security limitations honestly: once a buyer
        receives readable content, absolute copying prevention is impossible.
      </p>
      <div className="mt-10">
        <SecurityFeatureGrid />
      </div>
      <section className="mt-12 rounded-xl border border-border bg-muted/30 p-6">
        <h2 className="text-lg font-semibold">Documented limitations</h2>
        <ul className="mt-4 list-disc space-y-2 pl-5 text-sm text-muted-foreground">
          <li>Browser-based PDF viewing cannot fully prevent screenshots or printing.</li>
          <li>Downloaded readable source code can be copied; manifests help verify integrity.</li>
          <li>Document hashing provides evidence of existence, not automatic legal protection.</li>
          <li>Vault development mode is not suitable for production key management.</li>
        </ul>
      </section>
    </div>
  );
}
