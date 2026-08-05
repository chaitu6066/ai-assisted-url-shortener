# AI-Assisted URL Shortener

A production-oriented URL-shortener prototype demonstrating engineer-led,
AI-assisted delivery with Java 21, Spring Boot, PostgreSQL, Flyway, Maven, and
OpenAPI.

## Capabilities

- Generated 10-character Base62 short codes
- Optional lowercase custom aliases
- Atomic conflict-safe PostgreSQL inserts
- HTTP 302 redirects with `Cache-Control: no-store`
- Atomic click-count and last-accessed analytics
- Structured error responses with correlation identifiers
- HTTP/HTTPS destination validation
- Flyway-managed schema and Hibernate validation
- Unit and HTTP-contract tests
- Repeatable PowerShell smoke test
- GitHub Actions build gate

## Architecture

```text
API controllers
    ↓
Application services
    ↓
Domain policies and models
    ↓
JPA query repository + JDBC command repository
    ↓
PostgreSQL
```

JPA is used for straightforward reads. Targeted JDBC is used where PostgreSQL
atomic statements express the required concurrency behavior more safely.

## API

### Create a short URL

```http
POST /api/v1/urls
Content-Type: application/json
```

Generated code:

```json
{
  "originalUrl": "https://www.example.com"
}
```

Custom alias:

```json
{
  "originalUrl": "https://www.example.com/travel",
  "customAlias": "travel-2026"
}
```

### Redirect

```http
GET /{shortCode}
```

Returns HTTP 302 with the destination in the `Location` header.

### Read analytics

```http
GET /api/v1/urls/{shortCode}/analytics
```

## Local prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL
- PowerShell for the supplied smoke script

## Configuration

Set configuration through environment variables. Do not commit real
credentials.

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/url_shortener"
$env:DB_USERNAME="url_shortener_app"
$env:DB_PASSWORD="<your-local-password>"
$env:SERVER_PORT="8081"
$env:PUBLIC_BASE_URL="http://localhost:8081"
```

## Build and run

```powershell
mvn clean verify
mvn spring-boot:run
```

## Smoke test

With the application running:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
```

## Operational endpoints

- Health: `http://localhost:8081/actuator/health`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

## Key design decisions

- PostgreSQL's unique constraint is the authority for code collisions.
- Generated-code collisions are retried a bounded number of times.
- User-selected alias conflicts return HTTP 409 rather than being silently changed.
- Click counting and destination retrieval occur in one atomic update.
- Analytics are aggregate-only and do not collect visitor personal data.
- Redirects use HTTP 302 because destinations may be mutable in this prototype.
- Secrets are externalized through environment variables.

## Documentation

- `docs/ARCHITECTURE.md`
- `docs/API_DESIGN.md`
- `docs/DATABASE_DESIGN.md`
- `docs/ANALYTICS_REQUIREMENT_DECISION.md`
- `docs/SCENARIO_TRACEABILITY.md`
- `docs/AI_USAGE_AND_TRACEABILITY.md`
- `docs/QUALITY_GATES_AND_EVIDENCE.md`

## Current limitations and production evolution

This is a prototype, not a claimed internet-scale service.

Not implemented:

- authentication and authorization;
- tenant isolation;
- rate limiting and abuse prevention;
- expiration, disablement, or deletion;
- Redis caching;
- asynchronous click-event ingestion;
- multi-region routing;
- database partitioning or sharding;
- browser UI;
- personally identifiable analytics.

A production rollout would add controls based on measured load, threat models,
availability objectives, data-governance requirements, and operational
evidence rather than unsupported scale assumptions.
