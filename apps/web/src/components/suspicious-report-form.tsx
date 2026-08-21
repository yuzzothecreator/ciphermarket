"use client";

import type { SuspiciousReportCategory } from "@ciphermarket/contracts";
import { Button, Card, CardContent, CardHeader, CardTitle } from "@ciphermarket/ui";
import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { reportSuspiciousActivity } from "@/lib/buyer-api";

const categories: SuspiciousReportCategory[] = ["LEAK", "FRAUD", "ABUSE", "MALWARE", "OTHER"];

const inputClass =
  "flex h-10 w-full rounded-lg border border-border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring";

const textareaClass =
  "flex min-h-24 w-full rounded-lg border border-border bg-background px-3 py-2 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring";

export function SuspiciousReportForm({
  token,
  resourceType,
  resourceId,
}: {
  token: string;
  resourceType?: string;
  resourceId?: string;
}) {
  const [category, setCategory] = useState<SuspiciousReportCategory>("ABUSE");
  const [summary, setSummary] = useState("");
  const [details, setDetails] = useState("");

  const mutation = useMutation({
    mutationFn: () =>
      reportSuspiciousActivity(token, {
        category,
        summary,
        details: details || undefined,
        resourceType,
        resourceId,
      }),
    onSuccess: () => {
      setSummary("");
      setDetails("");
    },
  });

  return (
    <Card>
      <CardHeader>
        <CardTitle>Report suspicious activity</CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        <p className="text-sm text-muted-foreground">
          Reports create a high-severity security event for marketplace operators. Do not include
          secrets or payment credentials.
        </p>
        <select
          className={inputClass}
          value={category}
          onChange={(e) => setCategory(e.target.value as SuspiciousReportCategory)}
        >
          {categories.map((value) => (
            <option key={value} value={value}>
              {value}
            </option>
          ))}
        </select>
        <input
          className={inputClass}
          placeholder="Short summary"
          value={summary}
          onChange={(e) => setSummary(e.target.value)}
        />
        <textarea
          className={textareaClass}
          placeholder="Optional details"
          value={details}
          onChange={(e) => setDetails(e.target.value)}
        />
        <Button
          disabled={!summary.trim() || mutation.isPending}
          onClick={() => mutation.mutate()}
        >
          Submit report
        </Button>
        {mutation.isSuccess && (
          <p className="text-sm text-muted-foreground">Report submitted for security review.</p>
        )}
        {mutation.isError && (
          <p className="text-sm text-destructive">
            {mutation.error instanceof Error ? mutation.error.message : "Failed to submit report."}
          </p>
        )}
      </CardContent>
    </Card>
  );
}
