import type {
  ApprovalRequestRecord,
  ApprovalStatus,
  AuditBatch,
  AuditEventRecord,
  AuditVerifyResult,
  CreateApprovalRequest,
  RefundRequestRecord,
  SecurityEvent,
  SecurityEventStatus,
} from "@ciphermarket/contracts";
import { clientFetch } from "@/lib/client-api";

export function listAuditEvents(token: string) {
  return clientFetch<AuditEventRecord[]>("/api/v1/audit/events", { token });
}

export function verifyAuditChain(token: string) {
  return clientFetch<AuditVerifyResult>("/api/v1/audit/verify", { token });
}

export function listAuditBatches(token: string) {
  return clientFetch<AuditBatch[]>("/api/v1/audit/batches", { token });
}

export function sealAuditBatch(token: string) {
  return clientFetch<AuditBatch>("/api/v1/admin/audit/batches", { method: "POST", token });
}

export function listSecurityEvents(token: string, status?: SecurityEventStatus) {
  const query = status ? `?status=${status}` : "";
  return clientFetch<SecurityEvent[]>(`/api/v1/audit/security-events${query}`, { token });
}

export function acknowledgeSecurityEvent(token: string, eventId: string) {
  return clientFetch<SecurityEvent>(`/api/v1/admin/security-events/${eventId}/acknowledge`, {
    method: "POST",
    token,
  });
}

export function listApprovals(token: string, status?: ApprovalStatus) {
  const query = status ? `?status=${status}` : "";
  return clientFetch<ApprovalRequestRecord[]>(`/api/v1/audit/approvals${query}`, { token });
}

export function createApproval(token: string, body: CreateApprovalRequest) {
  return clientFetch<ApprovalRequestRecord>("/api/v1/admin/approvals", {
    method: "POST",
    token,
    json: body,
  });
}

export function decideApproval(
  token: string,
  approvalId: string,
  decision: "APPROVED" | "REJECTED",
  decisionReason?: string,
) {
  return clientFetch<ApprovalRequestRecord>(`/api/v1/admin/approvals/${approvalId}/decide`, {
    method: "POST",
    token,
    json: { decision, decisionReason },
  });
}

export function listAdminRefunds(token: string, status?: string) {
  const query = status ? `?status=${status}` : "";
  return clientFetch<RefundRequestRecord[]>(`/api/v1/admin/refunds${query}`, { token });
}

export function rejectAdminRefund(token: string, refundId: string, rejectionReason: string) {
  return clientFetch<RefundRequestRecord>(`/api/v1/admin/refunds/${refundId}/reject`, {
    method: "POST",
    token,
    json: { rejectionReason },
  });
}

export function submitRefundForApproval(token: string, refundId: string) {
  return clientFetch<RefundRequestRecord>(`/api/v1/admin/refunds/${refundId}/submit-for-approval`, {
    method: "POST",
    token,
  });
}
