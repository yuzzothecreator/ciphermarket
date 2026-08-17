# CipherMarket

**A Zero-Trust Marketplace for Secure Digital Product Distribution, Licensing and Leak Tracing**

CipherMarket enables creators to sell and distribute digital products securely — PDFs, source-code archives, design files, and more — with envelope encryption, verified payments, buyer-specific delivery, and complete security auditing.

> **Important:** CipherMarket does not claim to make piracy impossible. Once legitimate users receive readable content, absolute copying prevention is impossible. The system makes unauthorised access difficult, reduces casual sharing, detects misuse, and helps identify leak sources.

## Stack

| Layer | Technology |
|-------|------------|
| Frontend | Next.js 15, React 19, TypeScript, Tailwind CSS 4, Shadcn-style UI, TanStack Query |
| Backend | Java 21, Spring Boot 3.4, Spring Security, JPA, Flyway |
| Identity | Keycloak (OpenID Connect) |
| Data | PostgreSQL, Redis, RabbitMQ |
| Storage | MinIO (local), S3-compatible abstraction (production) |
| Keys | HashiCorp Vault Transit (dev mode locally) |
| Observability | OpenTelemetry, Prometheus, Grafana, structured JSON logs |

## Repository structure

```text
ciphermarket/
├── apps/web/              # Next.js frontend
├── services/api/          # Spring Boot modular monolith
├── packages/
│   ├── ui/                # Shared design system
│   └── contracts/         # Shared TypeScript types
├── infrastructure/        # Docker, Keycloak, monitoring, scripts
└── docs/                  # Architecture, security, operations
```

## Quick start

### Prerequisites

- Docker Desktop
- Node.js 20+ and pnpm 9+
- Java 21 and Maven (or use the included wrapper)

### 1. Configure environment

```bash
cp .env.example .env
# Edit .env and replace change_me_in_local_env placeholders
```

### 2. Start infrastructure

```bash
make up
make infra-wait
```

Services:

| Service | URL |
|---------|-----|
| Web | http://localhost:3000 |
| API | http://localhost:8080 |
| Keycloak | http://localhost:8180 |
| Mailpit | http://localhost:8025 |
| MinIO Console | http://localhost:9001 |
| RabbitMQ | http://localhost:15672 |

### 3. Run the API

```bash
make api-dev
```

Flyway migrations run automatically on startup.

### 4. Run the web app

```bash
pnpm install
make web-dev
```

## Phase 1 status (current)

Completed in this phase:

- Monorepo with pnpm workspaces
- Docker Compose (PostgreSQL, Redis, RabbitMQ, MinIO, Vault, ClamAV, Keycloak, Mailpit)
- Spring Boot API with modular package boundaries
- Flyway foundation schema (users, organisations, memberships, categories, audit events)
- Keycloak realm with platform roles
- Organisation APIs with tenant isolation
- Append-only audit trail with hash chaining
- Next.js app with design system (ion-indigo accent, light/dark themes)
- Public pages, auth redirects, live API health and categories
- Unit tests for audit hashing and RBAC rules
- GitHub Actions CI workflow
- Architecture and threat-model documentation

Not yet implemented (Phases 2–6):

- Product upload pipeline, encryption, malware scanning
- Commerce, payments, entitlements, secure delivery
- Security operations, E2E tests, full observability stack

## API documentation

When the API is running:

- OpenAPI: http://localhost:8080/api/v1/openapi
- Swagger UI: http://localhost:8080/api/v1/docs
- Health: http://localhost:8080/actuator/health

## Security notes

- Never commit `.env` or production secrets
- Vault dev mode is **not** suitable for production
- All tenant operations verify organisation membership server-side
- Audit events are append-only at the database level

## Documentation

- [Architecture](docs/architecture/system-architecture.md)
- [Threat model](docs/security/threat-model.md)
- [Local setup](docs/operations/local-setup.md)
- [Security limitations](docs/security/limitations.md)

## License

Proprietary — all rights reserved unless otherwise specified.
