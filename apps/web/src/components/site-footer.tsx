import Link from "next/link";

export function SiteFooter() {
  return (
    <footer className="border-t border-border bg-muted/30">
      <div className="mx-auto flex max-w-7xl flex-col gap-4 px-4 py-10 sm:px-6 md:flex-row md:items-center md:justify-between">
        <p className="text-sm text-muted-foreground">
          CipherMarket — secure digital product distribution. Copying prevention is not absolute; we
          make unauthorised access difficult and help trace leaks.
        </p>
        <div className="flex flex-wrap gap-4 text-sm">
          <Link href="/terms" className="text-muted-foreground hover:text-foreground">
            Terms
          </Link>
          <Link href="/privacy" className="text-muted-foreground hover:text-foreground">
            Privacy
          </Link>
          <Link href="/security" className="text-muted-foreground hover:text-foreground">
            Security
          </Link>
        </div>
      </div>
    </footer>
  );
}
