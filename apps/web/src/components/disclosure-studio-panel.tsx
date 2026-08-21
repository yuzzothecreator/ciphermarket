"use client";

import type { DisclosureDocument, DisclosureRequestRecord } from "@ciphermarket/contracts";
import { Badge, Button, Card, CardContent, CardDescription, CardHeader, CardTitle } from "@ciphermarket/ui";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { FileLock2, Loader2, Send } from "lucide-react";
import { useState } from "react";
import {
  createDisclosureRequest,
  listDisclosureDocuments,
  listOrganisationDisclosureRequests,
  revokeDisclosureRequest,
  uploadDisclosureDocument,
} from "@/lib/disclosure-api";

const inputClass =
  "flex h-10 w-full rounded-lg border border-border bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring";

const textareaClass =
  "flex min-h-24 w-full rounded-lg border border-border bg-background px-3 py-2 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring";

export function DisclosureStudioPanel({
  token,
  organisationId,
}: {
  token: string;
  organisationId: string;
}) {
  const queryClient = useQueryClient();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [selectedDocId, setSelectedDocId] = useState<string>("");
  const [recipientEmail, setRecipientEmail] = useState("");
  const [terms, setTerms] = useState(
    "The recipient agrees to keep this document confidential, not redistribute it, and use it only for the stated evaluation purpose.",
  );
  const [error, setError] = useState<string | null>(null);

  const documentsQuery = useQuery({
    queryKey: ["disclosure-documents", organisationId],
    queryFn: () => listDisclosureDocuments(token, organisationId),
    enabled: Boolean(token && organisationId),
  });

  const requestsQuery = useQuery({
    queryKey: ["disclosure-requests", organisationId],
    queryFn: () => listOrganisationDisclosureRequests(token, organisationId),
    enabled: Boolean(token && organisationId),
  });

  const uploadMutation = useMutation({
    mutationFn: () => {
      if (!file) throw new Error("Choose a file");
      return uploadDisclosureDocument(token, organisationId, title, description, file);
    },
    onSuccess: (doc) => {
      setError(null);
      setTitle("");
      setDescription("");
      setFile(null);
      setSelectedDocId(doc.id);
      queryClient.invalidateQueries({ queryKey: ["disclosure-documents", organisationId] });
    },
    onError: (err: unknown) => {
      setError(err instanceof Error ? err.message : "Upload failed");
    },
  });

  const requestMutation = useMutation({
    mutationFn: () =>
      createDisclosureRequest(token, organisationId, selectedDocId, {
        recipientEmail,
        confidentialityTerms: terms,
      }),
    onSuccess: () => {
      setError(null);
      setRecipientEmail("");
      queryClient.invalidateQueries({ queryKey: ["disclosure-requests", organisationId] });
    },
    onError: (err: unknown) => {
      setError(err instanceof Error ? err.message : "Request failed");
    },
  });

  const revokeMutation = useMutation({
    mutationFn: (requestId: string) => revokeDisclosureRequest(token, organisationId, requestId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["disclosure-requests", organisationId] });
    },
    onError: (err: unknown) => {
      setError(err instanceof Error ? err.message : "Revoke failed");
    },
  });

  const readyDocs = (documentsQuery.data ?? []).filter((d) => d.status === "READY");

  return (
    <div className="mt-10 space-y-6">
      <div>
        <h2 className="flex items-center gap-2 text-xl font-semibold tracking-tight">
          <FileLock2 className="size-5 text-accent" aria-hidden />
          Confidential disclosures
        </h2>
        <p className="mt-2 max-w-2xl text-sm text-muted-foreground">
          Upload an encrypted document, record its SHA-256 hash, and invite a recipient who must
          accept confidentiality terms before download. This creates evidence of disclosure — not an
          automatic NDA or copyright.
        </p>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Upload document</CardTitle>
            <CardDescription>Quarantine scan, encrypt, and store the hash.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <input
              className={inputClass}
              placeholder="Title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
            <textarea
              className={textareaClass}
              placeholder="Description (optional)"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
            <input
              type="file"
              className="block w-full text-sm"
              onChange={(e) => setFile(e.target.files?.[0] ?? null)}
            />
            <Button
              disabled={!title || !file || uploadMutation.isPending}
              onClick={() => uploadMutation.mutate()}
            >
              {uploadMutation.isPending ? (
                <Loader2 className="mr-2 size-4 animate-spin" />
              ) : null}
              Encrypt & store
            </Button>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Invite recipient</CardTitle>
            <CardDescription>Recipient must already have a CipherMarket account.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <select
              className={inputClass}
              value={selectedDocId}
              onChange={(e) => setSelectedDocId(e.target.value)}
            >
              <option value="">Select READY document</option>
              {readyDocs.map((doc) => (
                <option key={doc.id} value={doc.id}>
                  {doc.title}
                </option>
              ))}
            </select>
            <input
              className={inputClass}
              placeholder="Recipient email"
              type="email"
              value={recipientEmail}
              onChange={(e) => setRecipientEmail(e.target.value)}
            />
            <textarea
              className={textareaClass}
              value={terms}
              onChange={(e) => setTerms(e.target.value)}
            />
            <Button
              disabled={!selectedDocId || !recipientEmail || !terms || requestMutation.isPending}
              onClick={() => requestMutation.mutate()}
            >
              {requestMutation.isPending ? (
                <Loader2 className="mr-2 size-4 animate-spin" />
              ) : (
                <Send className="mr-2 size-4" />
              )}
              Send disclosure request
            </Button>
          </CardContent>
        </Card>
      </div>

      {error && <p className="text-sm text-destructive">{error}</p>}

      <DocumentsList documents={documentsQuery.data ?? []} loading={documentsQuery.isLoading} />
      <RequestsList
        requests={requestsQuery.data ?? []}
        loading={requestsQuery.isLoading}
        onRevoke={(id) => revokeMutation.mutate(id)}
        revoking={revokeMutation.isPending}
      />
    </div>
  );
}

function DocumentsList({
  documents,
  loading,
}: {
  documents: DisclosureDocument[];
  loading: boolean;
}) {
  if (loading) {
    return <p className="text-sm text-muted-foreground">Loading documents…</p>;
  }
  if (!documents.length) {
    return <p className="text-sm text-muted-foreground">No disclosure documents yet.</p>;
  }
  return (
    <div className="space-y-3">
      <h3 className="text-sm font-medium">Documents</h3>
      {documents.map((doc) => (
        <Card key={doc.id}>
          <CardContent className="flex flex-wrap items-start justify-between gap-3 py-4">
            <div>
              <p className="font-medium">{doc.title}</p>
              <p className="text-xs text-muted-foreground">{doc.originalFileName}</p>
              {doc.sha256Checksum && (
                <p className="mt-1 break-all font-mono text-[11px] text-muted-foreground">
                  SHA-256 {doc.sha256Checksum}
                </p>
              )}
            </div>
            <Badge variant={doc.status === "READY" ? "accent" : "outline"}>{doc.status}</Badge>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

function RequestsList({
  requests,
  loading,
  onRevoke,
  revoking,
}: {
  requests: DisclosureRequestRecord[];
  loading: boolean;
  onRevoke: (id: string) => void;
  revoking: boolean;
}) {
  if (loading) {
    return <p className="text-sm text-muted-foreground">Loading requests…</p>;
  }
  if (!requests.length) {
    return <p className="text-sm text-muted-foreground">No disclosure requests yet.</p>;
  }
  return (
    <div className="space-y-3">
      <h3 className="text-sm font-medium">Outgoing requests</h3>
      {requests.map((request) => (
        <Card key={request.id}>
          <CardContent className="flex flex-wrap items-start justify-between gap-3 py-4">
            <div>
              <p className="font-medium">{request.documentTitle ?? request.documentId}</p>
              <p className="text-xs text-muted-foreground">To {request.recipientEmail}</p>
              <p className="mt-1 text-xs text-muted-foreground">
                Disclosed {new Date(request.disclosedAt).toLocaleString()}
              </p>
            </div>
            <div className="flex items-center gap-2">
              <Badge variant={request.status === "ACCEPTED" ? "accent" : "outline"}>
                {request.status}
              </Badge>
              {(request.status === "PENDING" || request.status === "ACCEPTED") && (
                <Button
                  size="sm"
                  variant="secondary"
                  disabled={revoking}
                  onClick={() => onRevoke(request.id)}
                >
                  Revoke
                </Button>
              )}
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
