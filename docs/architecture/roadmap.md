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

## Phase 3 — Commerce ✅

- Public product catalogue API + UI
- Shopping cart and checkout
- Mock payment provider with HMAC-signed webhooks
- Orders, entitlements (webhook-granted only), receipt emails
- Buyer portal (orders + entitlements)

## Phase 4 — Secure delivery ✅

- Ed25519 licence tokens issued on entitlement grant
- Short-lived access grants with use limits
- Decrypt-and-transform delivery pipeline
- PDF watermarking (PDFBox) and source-code signed manifests
- Device registration and revocation
- Download audit trail

## Phase 5 — Security operations ✅

- Security event engine
- Tamper-evident audit batches
- Maker-checker approvals
- Admin investigation UI

## Phase 6 — Production readiness ✅

- Playwright smoke tests for public pages
- Grafana/Prometheus dashboard provisioning and ECS JSON logs in `prod`
- k6 catalogue load script
- Non-root web image, prod Compose overlay, security headers, in-process rate limit, secret guard
- `@ciphermarket/sdk-node` typed client

## Phase 7 — Confidential disclosure ✅ (current)

- Encrypted disclosure documents with SHA-256 evidence hashes
- Disclosure requests with confidentiality terms and expiration
- Recipient acceptance / rejection inbox
- Revocable access and append-only disclosure access events
- Creator Studio + buyer portal UI
