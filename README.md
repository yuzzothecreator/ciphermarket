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
│   ├── contracts/         # Shared TypeScript types
│   └── sdk-node/          # Typed Node.js API client
├── infrastructure/        # Docker, Keycloak, monitoring, perf scripts
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
| Prometheus | http://localhost:9090 (`--profile monitoring`) |
| Grafana | http://localhost:3001 (`--profile monitoring`) |

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

## Status

Phases 1–6 are implemented in this repository:

- Foundation, Keycloak, tenant isolation, audit hash chain
- Creator Studio, quarantine upload, ClamAV, envelope encryption
- Catalogue, cart, checkout, HMAC payment webhooks, entitlements
- Ed25519 licences, access grants, watermarked/signed delivery
- Security events, sealed audit batches, maker-checker admin console
- Playwright smoke tests, Grafana dashboards, k6, prod Docker overlay, `@ciphermarket/sdk-node`

See [docs/architecture/roadmap.md](docs/architecture/roadmap.md) and [docs/operations/production-readiness.md](docs/operations/production-readiness.md).

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
- [Production readiness](docs/operations/production-readiness.md)
- [Security limitations](docs/security/limitations.md)

## License

Proprietary — all rights reserved unless otherwise specified.
