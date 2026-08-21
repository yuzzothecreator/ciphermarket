# Security Operations (Phase 5)

Phase 5 adds an investigation console for marketplace administrators and security auditors: security events, tamper-evident audit batches, and maker-checker approvals.

## Roles

| Role | Access |
|------|--------|
| `marketplace_admin` | Read events/audit; acknowledge events; seal batches; create and decide approvals |
| `security_auditor` | Read-only: events, audit chain, approval history |

Maker and checker **must be different users**. Approving your own request is rejected.

## Security events

High-risk product flows emit operational events:

- `PRODUCT_PUBLISHED`
- `PAYMENT_CONFIRMED`
- `ENTITLEMENT_GRANTED`
- `DOWNLOAD_SUCCESS`
- `APPROVAL_REQUESTED` / `APPROVAL_DECIDED`
- `AUDIT_BATCH_SEALED`

Events start `OPEN` and can be acknowledged by an administrator.

## Audit batches

Each `audit_events` row is hash-chained (`previous_hash` → `event_hash`). Sealing a batch folds those hashes into a `root_hash` checkpoint so later mutation or truncation is detectable.

`GET /api/v1/audit/verify` recomputes the chain. `POST /api/v1/admin/audit/batches` seals unsealed events only if the chain is intact.

## Maker-checker actions

| Action | Effect after checker approval |
|--------|-------------------------------|
| `PRODUCT_SUSPEND` | Product status → `SUSPENDED` |
| `ENTITLEMENT_REVOKE` | Entitlement revoked; linked licence revoked |
| `LICENCE_REVOKE` | Licence revoked (downloads fail) |
| `REFUND_APPROVE` | Order/payment refunded; entitlements and licences revoked |

## API

| Method | Path | Role |
|--------|------|------|
| `GET` | `/api/v1/audit/events` | admin, auditor |
| `GET` | `/api/v1/audit/verify` | admin, auditor |
| `GET` | `/api/v1/audit/batches` | admin, auditor |
| `POST` | `/api/v1/admin/audit/batches` | admin |
| `GET` | `/api/v1/audit/security-events` | admin, auditor |
| `POST` | `/api/v1/admin/security-events/{id}/acknowledge` | admin |
| `GET` | `/api/v1/audit/approvals` | admin, auditor |
| `POST` | `/api/v1/admin/approvals` | admin |
| `POST` | `/api/v1/admin/approvals/{id}/decide` | admin (not the maker) |

## UI

Open `/admin` while signed in as `marketplace_admin` or `security_auditor`. Assign the Keycloak realm role, then sign in again so the access token includes it.
