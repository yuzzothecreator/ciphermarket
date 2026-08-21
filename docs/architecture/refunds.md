# Refunds (Phase 8)

Buyer-initiated refunds with dual-control execution.

## Flow

1. Buyer requests a refund on a **PAID** order (`POST /api/v1/orders/{id}/refund-requests`).
2. Marketplace admin reviews the queue (`GET /api/v1/admin/refunds`).
3. Admin either rejects, or submits for maker-checker (`REFUND_APPROVE`).
4. A **different** admin approves via the existing approvals API.
5. On approval the platform:
   - Marks the order `REFUNDED` and payment `REFUNDED`
   - Revokes entitlements and linked licences
   - Records audit + security events
   - Emails the buyer (Mailpit locally)

Frontend redirects are never treated as proof of refund. Execution happens only after maker-checker approval.

## Honest limits

- The mock provider records a `mock-refund-{id}` reference; wire a real PSP adapter before production settlement.
- Product `refund_policy` text is informational; operators still decide on each request.
