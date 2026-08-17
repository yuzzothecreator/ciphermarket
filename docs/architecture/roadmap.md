# Implementation Roadmap

## Phase 1 — Foundation ✅ (current)

- Monorepo, Docker Compose, Next.js, Spring Boot
- PostgreSQL + Flyway foundation schema
- Keycloak realm with platform roles
- Organisation model + tenant isolation
- RBAC (platform + organisation roles)
- Design system (ion-indigo accent, light/dark)
- Health checks, OpenAPI, audit hash chain
- CI workflow, documentation, threat model

## Phase 2 — Product management

- Creator studio (full)
- Products, versions, upload sessions
- Quarantine storage, MIME validation, ClamAV
- Envelope encryption (`EncryptionProvider`)
- Publishing workflow

## Phase 3 — Commerce

- Catalogue, cart, checkout
- Mock payment provider + webhook pipeline
- Orders, entitlements, receipts, notifications

## Phase 4 — Secure delivery

- Licences, access grants, download limits
- PDF watermarking (PDFBox)
- Source-code signed manifests
- Device registration, revocation

## Phase 5 — Security operations

- Security event engine
- Tamper-evident audit batches
- Maker-checker approvals
- Admin investigation UI

## Phase 6 — Production readiness

- Full test suite (Playwright E2E)
- Observability stack
- Performance testing
- Deployment hardening
- `@ciphermarket/sdk-node` (after API stabilises)
