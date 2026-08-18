.PHONY: help up down logs ps migrate api-dev web-dev build test lint format infra-wait e2e perf monitoring

help:
	@echo "CipherMarket — local development commands"
	@echo ""
	@echo "  make up          Start infrastructure (Docker Compose)"
	@echo "  make down        Stop infrastructure"
	@echo "  make logs        Tail Docker Compose logs"
	@echo "  make ps          Show running services"
	@echo "  make migrate     Run Flyway migrations via API module"
	@echo "  make api-dev     Run Spring Boot API (requires infra)"
	@echo "  make web-dev     Run Next.js web app"
	@echo "  make build       Build all packages"
	@echo "  make test        Run all tests"
	@echo "  make e2e         Playwright public-page smoke tests"
	@echo "  make perf        k6 catalogue load (API must be running)"
	@echo "  make monitoring  Start Prometheus and Grafana"
	@echo "  make lint        Lint frontend packages"
	@echo "  make format      Format frontend files"
	@echo "  make infra-wait  Wait until core services are healthy"

up:
	docker compose up -d

down:
	docker compose down

logs:
	docker compose logs -f

ps:
	docker compose ps

infra-wait:
	@powershell -ExecutionPolicy Bypass -File infrastructure/scripts/wait-for-services.ps1

migrate:
	cd services/api && ./mvnw flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/ciphermarket -Dflyway.user=ciphermarket -Dflyway.password=$${POSTGRES_PASSWORD:-change_me_in_local_env}

api-dev:
	cd services/api && ./mvnw spring-boot:run

web-dev:
	pnpm --filter @ciphermarket/web dev

build:
	pnpm build
	cd services/api && ./mvnw -B package -DskipTests

test:
	cd services/api && ./mvnw -B test
	pnpm test

lint:
	pnpm lint

format:
	pnpm format

e2e:
	pnpm --filter @ciphermarket/web test:e2e

perf:
	k6 run infrastructure/perf/k6-catalogue.js

monitoring:
	docker compose --profile monitoring up -d
