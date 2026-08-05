# AI-Assisted URL Shortener

Production-oriented URL shortener prototype built with Java 21, Spring Boot, PostgreSQL, Flyway, and OpenAPI.

## Greenfield slice implemented

- Generate a 10-character Base62 short code
- Persist through PostgreSQL `INSERT ... ON CONFLICT DO NOTHING`
- Redirect a valid short code using HTTP 302
- Validate HTTP/HTTPS destination URLs
- Return structured errors
- Expose Actuator health and Swagger/OpenAPI
- Unit-test generation, validation, collision retry, and retry exhaustion

Custom aliases and analytics are added in subsequent engineering scenarios.

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

## Create a short URL

```bat
curl.exe -i -X POST http://localhost:8081/api/v1/urls ^
  -H "Content-Type: application/json" ^
  -d "{\"originalUrl\":\"https://www.example.com\"}"
```

## Test redirect

```bat
curl.exe -i --max-redirs 0 http://localhost:8081/<shortCode>
```

## Health and API documentation

- Health: `http://localhost:8081/actuator/health`
- Swagger: `http://localhost:8081/swagger-ui.html`
- OpenAPI: `http://localhost:8081/v3/api-docs`

Detailed engineering documents are under `docs/`.
