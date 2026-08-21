import type {
  CreateDisclosureRequest,
  DisclosureDocument,
  DisclosureRequestRecord,
} from "@ciphermarket/contracts";
import { clientFetch, getApiUrl } from "@/lib/client-api";

export function listDisclosureDocuments(token: string, organisationId: string) {
  return clientFetch<DisclosureDocument[]>(
    `/api/v1/organisations/${organisationId}/disclosures/documents`,
    { token },
  );
}

export async function uploadDisclosureDocument(
  token: string,
  organisationId: string,
  title: string,
  description: string,
  file: File,
): Promise<DisclosureDocument> {
  const form = new FormData();
  form.append("title", title);
  if (description) {
    form.append("description", description);
  }
  form.append("file", file);

  const response = await fetch(
    `${getApiUrl()}/api/v1/organisations/${organisationId}/disclosures/documents`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
      },
      body: form,
    },
  );

  if (!response.ok) {
    const problem = await response.json().catch(() => undefined);
    throw new Error(
      (problem as { detail?: string } | undefined)?.detail ??
        `Disclosure upload failed (${response.status})`,
    );
  }

  return response.json() as Promise<DisclosureDocument>;
}

export function createDisclosureRequest(
  token: string,
  organisationId: string,
  documentId: string,
  body: CreateDisclosureRequest,
) {
  return clientFetch<DisclosureRequestRecord>(
    `/api/v1/organisations/${organisationId}/disclosures/documents/${documentId}/requests`,
    { method: "POST", token, json: body },
  );
}

export function listOrganisationDisclosureRequests(token: string, organisationId: string) {
  return clientFetch<DisclosureRequestRecord[]>(
    `/api/v1/organisations/${organisationId}/disclosures/requests`,
    { token },
  );
}

export function revokeDisclosureRequest(token: string, organisationId: string, requestId: string) {
  return clientFetch<DisclosureRequestRecord>(
    `/api/v1/organisations/${organisationId}/disclosures/requests/${requestId}/revoke`,
    { method: "POST", token },
  );
}

export function listDisclosureInbox(token: string) {
  return clientFetch<DisclosureRequestRecord[]>("/api/v1/disclosures/inbox", { token });
}

export function acceptDisclosure(token: string, requestId: string) {
  return clientFetch<DisclosureRequestRecord>(`/api/v1/disclosures/inbox/${requestId}/accept`, {
    method: "POST",
    token,
  });
}

export function rejectDisclosure(token: string, requestId: string, note?: string) {
  return clientFetch<DisclosureRequestRecord>(`/api/v1/disclosures/inbox/${requestId}/reject`, {
    method: "POST",
    token,
    json: { note: note ?? "" },
  });
}

export function getDisclosureDownloadUrl(requestId: string): string {
  return `${getApiUrl()}/api/v1/disclosures/inbox/${requestId}/download`;
}
