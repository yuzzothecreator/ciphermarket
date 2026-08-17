"use client";

import type { HealthStatus } from "@ciphermarket/contracts";
import { Badge } from "@ciphermarket/ui";
import { useQuery } from "@tanstack/react-query";
import { Activity } from "lucide-react";
import { clientFetch } from "@/lib/client-api";

export function ApiHealthBadge() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ["health"],
    queryFn: () => clientFetch<HealthStatus>("/actuator/health"),
    refetchInterval: 60_000,
  });

  if (isLoading) {
    return (
      <Badge variant="outline" className="gap-1">
        <Activity className="size-3.5 animate-pulse" aria-hidden />
        Checking API…
      </Badge>
    );
  }

  if (isError || data?.status !== "UP") {
    return (
      <Badge variant="outline" className="gap-1 text-destructive">
        <Activity className="size-3.5" aria-hidden />
        API unavailable
      </Badge>
    );
  }

  return (
    <Badge variant="outline" className="gap-1">
      <Activity className="size-3.5 text-accent" aria-hidden />
      API healthy
    </Badge>
  );
}
