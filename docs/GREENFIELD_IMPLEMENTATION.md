# Greenfield Core Implementation

## Objective

Implement the first working functional slice:

- generated short-code creation;
- atomic PostgreSQL persistence;
- HTTP 302 redirection;
- HTTP/HTTPS URL validation;
- structured errors;
- focused unit tests.

Custom aliases and analytics are intentionally left for later scenario commits.

## Apply the overlay

Extract `greenfield-implementation.zip` directly into:

```text
D:\url-shortener
```

Allow `README.md` to be replaced.

## Refresh Spring Tools

```text
Right-click project
→ Maven
→ Update Project
```

Then refresh the project.

## Build

```bat
cd /d D:\url-shortener

set SERVER_PORT=8081
set PUBLIC_BASE_URL=http://localhost:8081
set DB_URL=jdbc:postgresql://localhost:5432/url_shortener
set DB_USERNAME=url_shortener_app
set DB_PASSWORD=<your-local-password>

mvn clean verify
```

Expected result:

```text
BUILD SUCCESS
```

## Run

```bat
mvn spring-boot:run
```

## Verify URL creation

From another Command Prompt:

```bat
curl.exe -i -X POST http://localhost:8081/api/v1/urls ^
  -H "Content-Type: application/json" ^
  -d "{\"originalUrl\":\"https://www.example.com\"}"
```

Expected:

- HTTP `201 Created`;
- `Location` header;
- 10-character `shortCode`;
- `shortUrl` using port 8081.

## Verify redirect

Copy the generated code:

```bat
curl.exe -i --max-redirs 0 http://localhost:8081/<shortCode>
```

Expected:

- HTTP `302 Found`;
- `Location: https://www.example.com`;
- `Cache-Control: no-store`.

## Verify invalid URL

```bat
curl.exe -i -X POST http://localhost:8081/api/v1/urls ^
  -H "Content-Type: application/json" ^
  -d "{\"originalUrl\":\"javascript:alert(1)\"}"
```

Expected:

- HTTP `400`;
- application code `VALIDATION_FAILED`.

## Verify unknown code

```bat
curl.exe -i --max-redirs 0 http://localhost:8081/Unknown123
```

Expected:

- HTTP `404`;
- application code `SHORT_URL_NOT_FOUND`.

## Commit and push

```bat
git status
git diff

git add src README.md
git diff --cached

git commit -m "feat: implement generated URL creation and redirect"
git push
```
