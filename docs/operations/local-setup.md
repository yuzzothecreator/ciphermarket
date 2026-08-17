# Local Development Setup

## 1. Clone and configure

```bash
cp .env.example .env
```

Update passwords in `.env`. Do not use default values outside local development.

## 2. Start Docker services

```bash
docker compose up -d
```

Wait for health:

```bash
make infra-wait
```

Or on Windows PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File infrastructure/scripts/wait-for-services.ps1
```

## 3. Backend

```bash
cd services/api
./mvnw spring-boot:run
```

Verify:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/v1/categories
```

## 4. Frontend

```bash
pnpm install
pnpm --filter @ciphermarket/web dev
```

Open http://localhost:3000

## 5. Keycloak

- Admin console: http://localhost:8180 (admin / value from `.env`)
- Realm: `ciphermarket`
- Web client: `ciphermarket-web` (public, PKCE)
- API client: `ciphermarket-api` (confidential)

Assign the `creator` role to test organisation creation.

## 6. Email (Mailpit)

All Keycloak emails appear at http://localhost:8025

## 7. Monitoring (optional)

```bash
docker compose --profile monitoring up -d
```

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3001

## Troubleshooting

| Issue | Action |
|-------|--------|
| API cannot connect to Postgres | Ensure `docker compose ps` shows postgres healthy |
| Keycloak slow start | Wait up to 60s; check logs with `docker compose logs keycloak` |
| JWT validation fails | Confirm `KEYCLOAK_URL` matches running instance |
| Categories empty | Flyway migration seeds categories; check API logs |
