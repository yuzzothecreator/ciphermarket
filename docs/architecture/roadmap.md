# Implementation Roadmap

## Phase 1 — Foundation ✅

- Monorepo, Docker Compose, Next.js, Spring Boot
- PostgreSQL + Flyway foundation schema
- Keycloak realm with platform roles
- Organisation model + tenant isolation
- RBAC (platform + organisation roles)
- Design system (ion-indigo accent, light/dark)
- Health checks, OpenAPI, audit hash chain
- CI workflow, documentation, threat model

## Phase 2 — Product management ✅

- Creator studio (organisation, product CRUD, secure upload UI)
- Products, versions, upload sessions (Flyway V2)
- Quarantine storage, MIME validation, ClamAV
- Envelope encryption (`EncryptionProvider`)
- Publishing workflow
- Keycloak PKCE auth in web app

## Phase 3 — Commerce ✅ (current)

- Public product catalogue API + UI
- Shopping cart and checkout
- Mock payment provider with HMAC-signed webhooks
- Orders, entitlements (webhook-granted only), receipt emails
- Buyer portal (orders + entitlements)

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
