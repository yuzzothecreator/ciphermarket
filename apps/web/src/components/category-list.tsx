"use client";

import type { Category } from "@ciphermarket/contracts";
import { useQuery } from "@tanstack/react-query";
import { clientFetch } from "@/lib/client-api";

export function CategoryList() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ["categories"],
    queryFn: () => clientFetch<Category[]>("/api/v1/categories"),
  });

  if (isLoading) {
    return <p className="text-sm text-muted-foreground">Loading categories…</p>;
  }

  if (isError) {
    return (
      <p className="text-sm text-destructive">
        Unable to load categories. Start Docker Compose and the API service.
      </p>
    );
  }

  if (!data?.length) {
    return <p className="text-sm text-muted-foreground">No categories published yet.</p>;
  }

  return (
    <ul className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
      {data.map((category) => (
        <li
          key={category.id}
          className="rounded-xl border border-border bg-card p-4 transition-colors hover:border-accent/40"
        >
          <h3 className="font-medium">{category.name}</h3>
          <p className="mt-1 text-sm text-muted-foreground">{category.description}</p>
        </li>
      ))}
    </ul>
  );
}
