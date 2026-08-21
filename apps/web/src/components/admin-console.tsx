"use client";

import type { ApprovalActionType } from "@ciphermarket/contracts";
import { Badge, Button, Card, CardContent, CardHeader, CardTitle } from "@ciphermarket/ui";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Loader2 } from "lucide-react";
import Link from "next/link";
import { useState } from "react";
import { useAuth } from "@/lib/auth-context";
import {
  acknowledgeSecurityEvent,
  createApproval,
  decideApproval,
  listAdminRefunds,
  listApprovals,
  listAuditBatches,
  listAuditEvents,
  listSecurityEvents,
  rejectAdminRefund,
  sealAuditBatch,
  submitRefundForApproval,
  verifyAuditChain,
} from "@/lib/ops-api";

const actionTypes: ApprovalActionType[] = [
  "PRODUCT_SUSPEND",
  "ENTITLEMENT_REVOKE",
  "LICENCE_REVOKE",
  "REFUND_APPROVE",
];

export function AdminConsole() {
  const { accessToken, isAuthenticated, isSecurityOps, isMarketplaceAdmin } = useAuth();
  const [tab, setTab] = useState<"events" | "audit" | "approvals" | "refunds">("events");
  const queryClient = useQueryClient();

  const eventsQuery = useQuery({
    queryKey: ["security-events"],
    queryFn: () => listSecurityEvents(accessToken!),
    enabled: Boolean(accessToken && isSecurityOps),
  });

  const verifyQuery = useQuery({
    queryKey: ["audit-verify"],
    queryFn: () => verifyAuditChain(accessToken!),
    enabled: Boolean(accessToken && isSecurityOps && tab === "audit"),
  });

  const auditQuery = useQuery({
    queryKey: ["audit-events"],
    queryFn: () => listAuditEvents(accessToken!),
    enabled: Boolean(accessToken && isSecurityOps && tab === "audit"),
  });

  const batchesQuery = useQuery({
    queryKey: ["audit-batches"],
    queryFn: () => listAuditBatches(accessToken!),
    enabled: Boolean(accessToken && isSecurityOps && tab === "audit"),
  });

  const approvalsQuery = useQuery({
    queryKey: ["approvals"],
    queryFn: () => listApprovals(accessToken!),
    enabled: Boolean(accessToken && isSecurityOps && tab === "approvals"),
  });

  const refundsQuery = useQuery({
    queryKey: ["admin-refunds"],
    queryFn: () => listAdminRefunds(accessToken!),
    enabled: Boolean(accessToken && isMarketplaceAdmin && tab === "refunds"),
  });

  if (!isAuthenticated || !accessToken) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
        <h1 className="text-3xl font-semibold tracking-tight">Security operations</h1>
        <p className="mt-3 text-muted-foreground">Sign in with an operations role to continue.</p>
        <Button className="mt-6" asChild>
          <Link href="/auth/sign-in">Sign in</Link>
        </Button>
      </div>
    );
  }

  if (!isSecurityOps) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
        <h1 className="text-3xl font-semibold tracking-tight">Security operations</h1>
        <p className="mt-3 text-sm text-destructive">
          This console is limited to marketplace administrators and security auditors.
        </p>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
      <Badge variant="outline" className="mb-4">
        Operations
      </Badge>
      <h1 className="text-3xl font-semibold tracking-tight">Security operations</h1>
      <p className="mt-3 max-w-2xl text-muted-foreground">
        Investigate security events, verify the tamper-evident audit chain, process maker-checker
        approvals, and review refund escalations.
      </p>

      <div className="mt-8 flex flex-wrap gap-2">
        <Button variant={tab === "events" ? "default" : "secondary"} onClick={() => setTab("events")}>
          Security events
        </Button>
        <Button variant={tab === "audit" ? "default" : "secondary"} onClick={() => setTab("audit")}>
          Audit chain
        </Button>
        <Button
          variant={tab === "approvals" ? "default" : "secondary"}
          onClick={() => setTab("approvals")}
        >
          Approvals
        </Button>
        {isMarketplaceAdmin && (
          <Button
            variant={tab === "refunds" ? "default" : "secondary"}
            onClick={() => setTab("refunds")}
          >
            Refunds
          </Button>
        )}
      </div>

      <div className="mt-8">
        {tab === "events" && (
          <ul className="space-y-3">
            {eventsQuery.isLoading && <p className="text-sm text-muted-foreground">Loading events…</p>}
            {eventsQuery.data?.length === 0 && (
              <p className="text-sm text-muted-foreground">No security events recorded yet.</p>
            )}
            {eventsQuery.data?.map((event) => (
              <li key={event.id}>
                <Card>
                  <CardContent className="flex flex-wrap items-start justify-between gap-3 py-4">
                    <div>
                      <p className="font-medium">{event.eventType}</p>
                      <p className="mt-1 text-sm text-muted-foreground">{event.summary}</p>
                      <p className="mt-1 text-xs text-muted-foreground">
                        {new Date(event.createdAt).toLocaleString()}
                      </p>
                    </div>
                    <div className="flex items-center gap-2">
                      <Badge variant={event.severity === "HIGH" || event.severity === "CRITICAL" ? "accent" : "outline"}>
                        {event.severity}
                      </Badge>
                      <Badge variant="outline">{event.status}</Badge>
                      {isMarketplaceAdmin && event.status === "OPEN" && (
                        <Button
                          size="sm"
                          variant="secondary"
                          onClick={() =>
                            acknowledgeSecurityEvent(accessToken, event.id).then(() =>
                              queryClient.invalidateQueries({ queryKey: ["security-events"] }),
                            )
                          }
                        >
                          Acknowledge
                        </Button>
                      )}
                    </div>
                  </CardContent>
                </Card>
              </li>
            ))}
          </ul>
        )}

        {tab === "audit" && (
          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Chain verification</CardTitle>
              </CardHeader>
              <CardContent className="flex flex-wrap items-center justify-between gap-3">
                {verifyQuery.data ? (
                  <p className="text-sm">
                    {verifyQuery.data.intact ? "Intact" : "Broken"} — {verifyQuery.data.eventCount}{" "}
                    events. {verifyQuery.data.detail}
                  </p>
                ) : (
                  <p className="text-sm text-muted-foreground">Checking chain…</p>
                )}
                {isMarketplaceAdmin && (
                  <SealBatchButton
                    token={accessToken}
                    onSealed={() => {
                      queryClient.invalidateQueries({ queryKey: ["audit-batches"] });
                      queryClient.invalidateQueries({ queryKey: ["audit-verify"] });
                    }}
                  />
                )}
              </CardContent>
            </Card>

            {batchesQuery.data && batchesQuery.data.length > 0 && (
              <div>
                <h2 className="mb-3 text-lg font-medium">Sealed batches</h2>
                <ul className="space-y-2">
                  {batchesQuery.data.map((batch) => (
                    <li key={batch.id} className="rounded-lg border border-border p-3 text-sm">
                      {batch.eventCount} events · {new Date(batch.sealedAt).toLocaleString()}
                      <span className="mt-1 block font-mono text-xs text-muted-foreground">
                        {batch.rootHash.slice(0, 24)}…
                      </span>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            <div>
              <h2 className="mb-3 text-lg font-medium">Recent audit events</h2>
              <ul className="space-y-2">
                {auditQuery.data?.map((event) => (
                  <li key={event.id} className="rounded-lg border border-border p-3 text-sm">
                    <span className="font-medium">{event.action}</span> · {event.resourceType}
                    <span className="mt-1 block text-xs text-muted-foreground">
                      {new Date(event.createdAt).toLocaleString()}
                    </span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        )}

        {tab === "approvals" && (
          <ApprovalsPanel
            token={accessToken}
            isAdmin={isMarketplaceAdmin}
            approvals={approvalsQuery.data ?? []}
            loading={approvalsQuery.isLoading}
            onChanged={() => queryClient.invalidateQueries({ queryKey: ["approvals"] })}
          />
        )}

        {tab === "refunds" && isMarketplaceAdmin && (
          <RefundsPanel
            token={accessToken}
            refunds={refundsQuery.data ?? []}
            loading={refundsQuery.isLoading}
            onChanged={() => {
              queryClient.invalidateQueries({ queryKey: ["admin-refunds"] });
              queryClient.invalidateQueries({ queryKey: ["approvals"] });
            }}
          />
        )}
      </div>
    </div>
  );
}

function SealBatchButton({ token, onSealed }: { token: string; onSealed: () => void }) {
  const mutation = useMutation({
    mutationFn: () => sealAuditBatch(token),
    onSuccess: onSealed,
  });
  return (
    <Button size="sm" disabled={mutation.isPending} onClick={() => mutation.mutate()}>
      {mutation.isPending && <Loader2 className="mr-2 size-4 animate-spin" />}
      Seal batch
    </Button>
  );
}

function RefundsPanel({
  token,
  refunds,
  loading,
  onChanged,
}: {
  token: string;
  refunds: Awaited<ReturnType<typeof listAdminRefunds>>;
  loading: boolean;
  onChanged: () => void;
}) {
  const [rejectReason, setRejectReason] = useState("Does not meet refund policy");
  const [error, setError] = useState<string | null>(null);

  return (
    <div className="space-y-4">
      <p className="text-sm text-muted-foreground">
        Reject immediately, or submit for maker-checker approval. A different admin must approve
        REFUND_APPROVE before entitlements are revoked.
      </p>
      {error && <p className="text-sm text-destructive">{error}</p>}
      {loading && <p className="text-sm text-muted-foreground">Loading refunds…</p>}
      <ul className="space-y-3">
        {refunds.map((refund) => (
          <li key={refund.id}>
            <Card>
              <CardContent className="space-y-3 py-4">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <p className="font-medium">
                      {new Intl.NumberFormat("en-GB", {
                        style: "currency",
                        currency: refund.currency || "GBP",
                      }).format(refund.amountCents / 100)}
                    </p>
                    <p className="mt-1 text-sm text-muted-foreground">{refund.reason}</p>
                    <p className="mt-1 font-mono text-xs text-muted-foreground">
                      Order {refund.orderId}
                    </p>
                  </div>
                  <Badge variant={refund.status === "REQUESTED" ? "accent" : "outline"}>
                    {refund.status}
                  </Badge>
                </div>
                {refund.status === "REQUESTED" && (
                  <div className="flex flex-wrap gap-2">
                    <Button
                      size="sm"
                      onClick={() =>
                        submitRefundForApproval(token, refund.id)
                          .then(() => {
                            setError(null);
                            onChanged();
                          })
                          .catch((err: unknown) =>
                            setError(err instanceof Error ? err.message : "Submit failed"),
                          )
                      }
                    >
                      Submit for approval
                    </Button>
                    <Button
                      size="sm"
                      variant="secondary"
                      onClick={() =>
                        rejectAdminRefund(token, refund.id, rejectReason)
                          .then(() => {
                            setError(null);
                            onChanged();
                          })
                          .catch((err: unknown) =>
                            setError(err instanceof Error ? err.message : "Reject failed"),
                          )
                      }
                    >
                      Reject
                    </Button>
                  </div>
                )}
              </CardContent>
            </Card>
          </li>
        ))}
      </ul>
      <input
        className="flex h-10 w-full max-w-md rounded-lg border border-border bg-background px-3 text-sm"
        value={rejectReason}
        onChange={(e) => setRejectReason(e.target.value)}
        placeholder="Rejection reason"
      />
    </div>
  );
}

function ApprovalsPanel({
  token,
  isAdmin,
  approvals,
  loading,
  onChanged,
}: {
  token: string;
  isAdmin: boolean;
  approvals: Awaited<ReturnType<typeof listApprovals>>;
  loading: boolean;
  onChanged: () => void;
}) {
  const [actionType, setActionType] = useState<ApprovalActionType>("PRODUCT_SUSPEND");
  const [resourceId, setResourceId] = useState("");
  const [reason, setReason] = useState("");

  const createMutation = useMutation({
    mutationFn: () => createApproval(token, { actionType, resourceId, reason }),
    onSuccess: () => {
      setResourceId("");
      setReason("");
      onChanged();
    },
  });

  return (
    <div className="space-y-6">
      {isAdmin && (
        <Card>
          <CardHeader>
            <CardTitle>New approval request</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <select
              className="flex h-10 w-full rounded-lg border border-border bg-background px-3 text-sm"
              value={actionType}
              onChange={(e) => setActionType(e.target.value as ApprovalActionType)}
            >
              {actionTypes.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
            <input
              className="flex h-10 w-full rounded-lg border border-border bg-background px-3 text-sm"
              placeholder="Resource UUID"
              value={resourceId}
              onChange={(e) => setResourceId(e.target.value)}
            />
            <textarea
              className="min-h-20 w-full rounded-lg border border-border bg-background px-3 py-2 text-sm"
              placeholder="Reason"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
            />
            <Button
              disabled={!resourceId || !reason || createMutation.isPending}
              onClick={() => createMutation.mutate()}
            >
              Submit for checker
            </Button>
            {createMutation.isError && (
              <p className="text-sm text-destructive">
                {createMutation.error instanceof Error
                  ? createMutation.error.message
                  : "Failed to create request."}
              </p>
            )}
          </CardContent>
        </Card>
      )}

      {loading && <p className="text-sm text-muted-foreground">Loading approvals…</p>}
      <ul className="space-y-3">
        {approvals.map((approval) => (
          <li key={approval.id}>
            <Card>
              <CardContent className="py-4">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <p className="font-medium">{approval.actionType}</p>
                    <p className="mt-1 text-sm text-muted-foreground">{approval.reason}</p>
                    <p className="mt-1 font-mono text-xs text-muted-foreground">{approval.resourceId}</p>
                  </div>
                  <Badge variant={approval.status === "PENDING" ? "accent" : "outline"}>
                    {approval.status}
                  </Badge>
                </div>
                {isAdmin && approval.status === "PENDING" && (
                  <div className="mt-3 flex gap-2">
                    <Button
                      size="sm"
                      onClick={() =>
                        decideApproval(token, approval.id, "APPROVED").then(onChanged)
                      }
                    >
                      Approve
                    </Button>
                    <Button
                      size="sm"
                      variant="secondary"
                      onClick={() =>
                        decideApproval(token, approval.id, "REJECTED", "Rejected by checker").then(
                          onChanged,
                        )
                      }
                    >
                      Reject
                    </Button>
                  </div>
                )}
              </CardContent>
            </Card>
          </li>
        ))}
      </ul>
    </div>
  );
}
