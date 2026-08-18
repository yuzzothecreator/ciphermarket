# Production readiness (Phase 6)

This note covers how CipherMarket is observed, tested, and hardened for deployment. It does not claim the product is ready to accept real customer funds or production traffic without an operator review.

## Test suite

| Layer | How to run |
|-------|------------|
| API unit tests | `cd services/api && ./mvnw -B test` |
| Frontend typecheck | `pnpm typecheck` |
| Playwright smoke | Build the web app, then `pnpm --filter @ciphermarket/web test:e2e` |
| Optional API smoke | `CIPHERMARKET_API_URL=http://localhost:8080 pnpm --filter @ciphermarket/web test:e2e` |
| Catalogue load | `k6 run infrastructure/perf/k6-catalogue.js` |

Playwright covers public pages (home, catalogue, security, privacy, terms). It starts `next start` unless `PLAYWRIGHT_SKIP_WEBSERVER=1` and a server is already listening. Authenticated creator/admin journeys still require a live Keycloak realm and are not part of CI smoke.

## Observability

```bash
docker compose --profile monitoring up -d
```

| Service | URL |
|---------|-----|
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3001 (admin / `GRAFANA_ADMIN_PASSWORD`) |

Grafana provisions a Prometheus datasource and the **CipherMarket API** dashboard (HTTP rate, p95 latency, CPU, JVM heap). The API exposes `/actuator/prometheus` and writes `X-Correlation-Id` on every response. Production profile (`SPRING_PROFILES_ACTIVE=prod`) emits ECS JSON logs on stdout so a collector can ship them without scraping consoles.

OpenTelemetry remains optional: set `OTEL_EXPORTER_OTLP_ENDPOINT` and attach the OpenTelemetry Java agent at process start. The API depends on the OTel API so traces can be correlated later without a hard runtime exporter.

## Performance testing

The k6 script hits health, categories, and the public catalogue:

```bash
k6 run -e API_BASE_URL=http://localhost:8080 -e VUS=20 -e DURATION=1m infrastructure/perf/k6-catalogue.js
```

Thresholds fail the run if more than 5% of requests error or p95 exceeds 800 ms. Tune `RATE_LIMIT_RPM` if you are load-testing a local API with the in-process limiter enabled.

## Deployment hardening

- API and web images run as non-root users with liveness healthchecks.
- `application-prod.yml` disables Swagger, hides health details, turns on HSTS, and uses JSON logs.
- `ProductionSecretGuard` refuses to start in `prod` if datasource/Redis/RabbitMQ/Vault/webhook/storage secrets are blank or still contain local placeholders, or if licence signing keys are missing.
- Public and authenticated routes share a per-IP sliding window (`RATE_LIMIT_RPM`, default 120 locally / 60 in prod). Health probes are excluded. Multi-instance production should still terminate limits at a gateway or WAF; `X-Forwarded-For` is trusted only when `RATE_LIMIT_TRUST_FORWARDED_FOR=true`.
- Next.js sends `X-Frame-Options`, `nosniff`, referrer, and permissions-policy headers and omits `X-Powered-By`.

Build and run the application containers against existing Compose infrastructure:

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

The web image sets `DOCKER_BUILD=1` so Next.js emits the standalone server used by the non-root runtime stage. Local Windows `next build` skips standalone output because tracing needs Linux-style symlinks.

Replace Vault dev mode with a production Vault or cloud KMS before handling real assets. Do not expose Keycloak admin, MinIO console, RabbitMQ management, or Grafana to the public internet.

## Node SDK

`@ciphermarket/sdk-node` is a thin fetch client over the stable public and buyer APIs:

```ts
import { CipherMarketClient } from "@ciphermarket/sdk-node";

const publicApi = new CipherMarketClient({ baseUrl: "http://localhost:8080" });
const products = await publicApi.listCatalogue();

const buyerApi = publicApi.withAccessToken(accessToken);
const entitlements = await buyerApi.listEntitlements();
```

Creator studio, upload, and admin investigation endpoints stay in the web app until those contracts freeze further.
