# AI-Assisted URL Shortener

Production-oriented URL shortener prototype built with Java 21, Spring Boot,
PostgreSQL, Flyway, and OpenAPI.

## Implemented capabilities

- Generate a 10-character Base62 short code
- Optionally create a human-readable custom alias
- Persist through PostgreSQL `INSERT ... ON CONFLICT DO NOTHING`
- Return `409 Conflict` for duplicate custom aliases
- Reject reserved aliases such as `api` and `actuator`
- Redirect valid short codes using HTTP 302
- Atomically increment redirect analytics in PostgreSQL
- Expose aggregate analytics without collecting personal data
- Validate HTTP/HTTPS destination URLs
- Return structured errors with correlation identifiers
- Expose Actuator health and Swagger/OpenAPI

## Run locally

```bat
set DB_URL=jdbc:postgresql://localhost:5432/url_shortener
set DB_USERNAME=url_shortener_app
set DB_PASSWORD=<your-local-password>
set SERVER_PORT=8081
set PUBLIC_BASE_URL=http://localhost:8081

mvn clean verify
mvn spring-boot:run
```

## Create a generated short URL

PowerShell:

```powershell
$body=@{originalUrl="https://www.example.com"}|ConvertTo-Json; Invoke-RestMethod -Method Post -Uri "http://localhost:8081/api/v1/urls" -ContentType "application/json" -Body $body
```

## Create a custom alias

```powershell
$body=@{originalUrl="https://www.example.com/travel";customAlias="travel-2026"}|ConvertTo-Json; Invoke-RestMethod -Method Post -Uri "http://localhost:8081/api/v1/urls" -ContentType "application/json" -Body $body
```

## Redirect and record one click

```powershell
curl.exe -i --max-redirs 0 "http://localhost:8081/travel-2026"
```

## Read analytics

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8081/api/v1/urls/travel-2026/analytics"
```

Example:

```json
{
  "shortCode": "travel-2026",
  "originalUrl": "https://www.example.com/travel",
  "createdAt": "2026-08-05T04:45:00Z",
  "clickCount": 3,
  "lastAccessedAt": "2026-08-05T05:10:00Z"
}
```

## Operational endpoints

- Health: `http://localhost:8081/actuator/health`
- Swagger: `http://localhost:8081/swagger-ui.html`
- OpenAPI: `http://localhost:8081/v3/api-docs`

Detailed engineering documents are under `docs/`.
