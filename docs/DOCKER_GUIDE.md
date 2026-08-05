# Docker Compose Guide

Docker Compose provides the quickest reproducible reviewer setup. It starts a
PostgreSQL container and the Spring Boot application without requiring a local
PostgreSQL installation.

## What is started

```text
Browser, Swagger, Postman, or curl
                |
                v
      Spring Boot application
        localhost:8081
                |
                v
       PostgreSQL container
     internal host postgres:5432
```

The PostgreSQL port is also exposed as `localhost:5433` for optional database
inspection. Port `5433` avoids conflicting with a PostgreSQL installation that
may already use host port `5432`.

Flyway runs automatically when the application starts and creates or validates
the schema.

## Prerequisite

Install and start Docker Desktop, or another Docker Engine installation that
supports Docker Compose.

Verify:

```powershell
docker version
docker compose version
```

## Quick start on Windows

From the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\docker-up.ps1
```

The script:

1. creates `.env.docker.local` with a generated local-only database password;
2. leaves that file untracked because it is covered by `.gitignore`;
3. builds the application image;
4. starts PostgreSQL;
5. waits until PostgreSQL is healthy;
6. starts the application;
7. waits until Actuator reports `UP`;
8. prints the testing URLs.

Open:

```text
http://localhost:8081/swagger-ui.html
```

## Manual Compose start

Copy the example environment file:

```powershell
Copy-Item .env.docker.example .env.docker.local
```

Edit `.env.docker.local` and replace the placeholder password.

Validate the resolved Compose model:

```powershell
docker compose --env-file .env.docker.local config
```

Start the services:

```powershell
docker compose --env-file .env.docker.local up --build --detach
```

Check status:

```powershell
docker compose --env-file .env.docker.local ps
```

View application logs:

```powershell
docker compose --env-file .env.docker.local logs app --follow
```

## Test the running application

Health:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8081/actuator/health"
```

Swagger:

```text
http://localhost:8081/swagger-ui.html
```

Repeatable smoke test:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
```

Postman and curl can also target `http://localhost:8081`.

## Inspect PostgreSQL

The container maps PostgreSQL to host port `5433`:

```powershell
psql -h localhost -p 5433 -U url_shortener_app -d url_shortener
```

Use the password from `.env.docker.local`.

Alternatively, run `psql` inside the container:

```powershell
docker compose --env-file .env.docker.local exec postgres psql -U url_shortener_app -d url_shortener
```

Example query:

```sql
SELECT short_code, original_url, click_count, last_accessed_at
FROM url_mapping
ORDER BY created_at DESC;
```

## Stop containers and retain data

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\docker-down.ps1
```

The named PostgreSQL volume is retained. The next start reuses the same data.

## Stop and delete all local Docker data

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\docker-down.ps1 -RemoveData
```

This removes the PostgreSQL volume. The next start creates an empty database and
Flyway recreates the schema.

## Direct Compose shutdown

Retain data:

```powershell
docker compose --env-file .env.docker.local down
```

Remove data:

```powershell
docker compose --env-file .env.docker.local down --volumes
```

## Troubleshooting

### Port 8081 is already in use

Stop the locally running Spring Boot application before starting Compose.

Check:

```powershell
Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue
```

### Docker engine is not running

Start Docker Desktop and wait for it to report that the engine is ready.

### Application cannot connect to PostgreSQL

Inspect status and logs:

```powershell
docker compose --env-file .env.docker.local ps
docker compose --env-file .env.docker.local logs postgres --tail 100
docker compose --env-file .env.docker.local logs app --tail 100
```

The application intentionally connects to `postgres:5432`, which is the
service name and internal container port. It must not use `localhost:5433`
from inside the application container.

### A previous database password was changed

A PostgreSQL named volume retains the credentials created during its first
initialization. Remove the volume and start again:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\docker-down.ps1 -RemoveData
powershell -ExecutionPolicy Bypass -File .\scripts\docker-up.ps1
```

## Security boundary

The Docker setup is intended for local evaluation only.

- No production password is stored in the repository.
- `.env.docker.local` is ignored by Git.
- The application runs as a non-root user.
- The runtime image contains a Java 21 JRE rather than the Maven build toolchain.
- Production deployment would use a managed secret store and tighter network
  exposure.
