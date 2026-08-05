# URL Shortener

AI-assisted, production-oriented URL shortener prototype using Java 21, Spring Boot, PostgreSQL, Flyway, and OpenAPI.

## Run locally

```bat
set DB_URL=jdbc:postgresql://localhost:5432/url_shortener
set DB_USERNAME=url_shortener_app
set DB_PASSWORD=<your-local-password>
set PUBLIC_BASE_URL=http://localhost:8080
mvn clean verify
mvn spring-boot:run
```

Verify:

- `http://localhost:8080/actuator/health`
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

Detailed design documents are maintained under `docs/`.
