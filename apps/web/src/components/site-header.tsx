"use client";

import { Button } from "@ciphermarket/ui";
import { Moon, Shield, Sun } from "lucide-react";
import Link from "next/link";
import { useTheme } from "next-themes";
import { useEffect, useState } from "react";
import { useAuth } from "@/lib/auth-context";

const nav = [
  { href: "/catalogue" as const, label: "Catalogue" },
  { href: "/cart" as const, label: "Cart" },
  { href: "/security" as const, label: "Security" },
  { href: "/creator" as const, label: "Creator Studio" },
  { href: "/buyer" as const, label: "Buyer Portal" },
];

export function SiteHeader() {
  const { theme, setTheme } = useTheme();
  const { isAuthenticated, isSecurityOps, signOut } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => setMounted(true), []);

  return (
    <header className="sticky top-0 z-50 border-b border-border bg-background/80 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6">
        <Link href="/" className="flex items-center gap-2 font-semibold tracking-tight">
          <Shield className="size-5 text-accent" aria-hidden />
          CipherMarket
        </Link>

        <nav className="hidden items-center gap-6 md:flex" aria-label="Main">
          {nav.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className="text-sm text-muted-foreground transition-colors hover:text-foreground"
            >
              {item.label}
            </Link>
          ))}
          {isSecurityOps && (
            <Link
              href="/admin"
              className="text-sm text-muted-foreground transition-colors hover:text-foreground"
            >
              Security ops
            </Link>
          )}
        </nav>

        <div className="flex items-center gap-2">
          {mounted && (
            <Button
              variant="ghost"
              size="icon"
              aria-label="Toggle theme"
              onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
            >
              {theme === "dark" ? <Sun className="size-4" /> : <Moon className="size-4" />}
            </Button>
          )}
          {isAuthenticated ? (
            <Button variant="secondary" size="sm" onClick={signOut}>
              Sign out
            </Button>
          ) : (
            <>
              <Button variant="secondary" size="sm" asChild>
                <Link href="/auth/sign-in">Sign in</Link>
              </Button>
              <Button size="sm" asChild>
                <Link href="/auth/sign-up">Get started</Link>
              </Button>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
