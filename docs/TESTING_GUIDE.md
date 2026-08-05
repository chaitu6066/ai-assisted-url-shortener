# Testing Guide

This guide explains how to verify the URL shortener through Maven tests,
Swagger UI, Postman, PowerShell, and PostgreSQL.

## 1. Prerequisites

Before testing:

- PostgreSQL is running.
- The `url_shortener` database exists.
- The application database user can connect.
- Java 21 and Maven are available.
- The application environment variables are configured.

Example PowerShell configuration:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/url_shortener"
$env:DB_USERNAME="url_shortener_app"
$env:DB_PASSWORD="<your-local-password>"
$env:SERVER_PORT="8081"
$env:PUBLIC_BASE_URL="http://localhost:8081"
```

Do not commit a real password.

## 2. Automated Maven tests

Run the complete automated test suite from the repository root:

```powershell
mvn clean verify
```

Expected result:

```text
Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The suite covers:

- URL validation;
- Base62 short-code generation;
- generated-code collision retry;
- custom-alias policy;
- duplicate alias behavior;
- redirect behavior;
- analytics behavior;
- HTTP response contracts;
- structured error responses.

## 3. Start the application

```powershell
mvn spring-boot:run
```

Expected local base URL:

```text
http://localhost:8081
```

Verify application health:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8081/actuator/health"
```

Expected:

```json
{
  "status": "UP"
}
```

## 4. Testing with Swagger UI

Open:

```text
http://localhost:8081/swagger-ui.html
```

Swagger is suitable for testing URL creation and analytics.

### 4.1 Create a generated short URL

Open:

```text
POST /api/v1/urls
```

Select **Try it out** and use:

```json
{
  "originalUrl": "https://www.example.com"
}
```

Expected:

- HTTP `201 Created`;
- a generated 10-character `shortCode`;
- a `shortUrl`;
- the original destination;
- a creation timestamp.

Example:

```json
{
  "shortCode": "LvcAuAlGCS",
  "shortUrl": "http://localhost:8081/LvcAuAlGCS",
  "originalUrl": "https://www.example.com",
  "createdAt": "2026-08-05T04:10:11Z"
}
```

Copy the returned `shortCode`.

### 4.2 Create a custom alias

Use:

```json
{
  "originalUrl": "https://www.example.com/travel",
  "customAlias": "travel-2026"
}
```

Expected:

- HTTP `201 Created`;
- `shortCode` equal to `travel-2026`.

### 4.3 Read analytics

Open:

```text
GET /api/v1/urls/{shortCode}/analytics
```

Use:

```text
travel-2026
```

Expected:

```json
{
  "shortCode": "travel-2026",
  "originalUrl": "https://www.example.com/travel",
  "createdAt": "...",
  "clickCount": 0,
  "lastAccessedAt": null
}
```

After performing a redirect, call the analytics endpoint again. The click count
should increase and `lastAccessedAt` should be populated.

### Swagger redirect note

Swagger UI may automatically follow external redirects or show a browser/CORS
error instead of clearly displaying the original HTTP `302` response. Use
PowerShell or Postman with automatic redirects disabled when verifying the
`Location` and `Cache-Control` headers.

## 5. Testing with Postman

Create a Postman environment with:

```text
baseUrl = http://localhost:8081
shortCode = travel-2026
```

### 5.1 Health request

```text
Method: GET
URL: {{baseUrl}}/actuator/health
```

Expected: HTTP `200`.

### 5.2 Generated URL request

```text
Method: POST
URL: {{baseUrl}}/api/v1/urls
Header: Content-Type: application/json
```

Body:

```json
{
  "originalUrl": "https://www.example.com"
}
```

Expected: HTTP `201`.

Copy the returned `shortCode` into the Postman environment variable.

### 5.3 Custom alias request

```text
Method: POST
URL: {{baseUrl}}/api/v1/urls
Header: Content-Type: application/json
```

Body:

```json
{
  "originalUrl": "https://www.example.com/travel",
  "customAlias": "travel-2026"
}
```

Expected: HTTP `201`.

### 5.4 Redirect request

Disable automatic redirect following in Postman before sending this request.

```text
Method: GET
URL: {{baseUrl}}/{{shortCode}}
```

Expected:

```text
HTTP 302
Location: https://www.example.com/travel
Cache-Control: no-store
```

### 5.5 Analytics request

```text
Method: GET
URL: {{baseUrl}}/api/v1/urls/{{shortCode}}/analytics
```

Expected: HTTP `200`.

Confirm:

- `clickCount` increased after redirect;
- `lastAccessedAt` is populated;
- calling analytics repeatedly does not increase `clickCount`.

## 6. Negative test cases

### 6.1 Duplicate custom alias

Submit the same custom-alias creation request twice.

Expected on the second request:

```text
HTTP 409 Conflict
```

Response body should include:

```json
{
  "code": "CUSTOM_ALIAS_ALREADY_EXISTS"
}
```

### 6.2 Reserved alias

Request:

```json
{
  "originalUrl": "https://www.example.com",
  "customAlias": "api"
}
```

Expected:

```text
HTTP 400 Bad Request
```

Response body should include:

```json
{
  "code": "RESERVED_CUSTOM_ALIAS"
}
```

### 6.3 Invalid destination URL

Request:

```json
{
  "originalUrl": "javascript:alert(1)"
}
```

Expected:

```text
HTTP 400 Bad Request
```

Response body should include:

```json
{
  "code": "VALIDATION_FAILED"
}
```

### 6.4 Unknown short code

```text
GET /does-not-exist
```

Expected:

```text
HTTP 404 Not Found
```

Response body should include:

```json
{
  "code": "SHORT_URL_NOT_FOUND"
}
```

### 6.5 Unknown analytics code

```text
GET /api/v1/urls/does-not-exist/analytics
```

Expected:

```text
HTTP 404 Not Found
```

## 7. Repeatable PowerShell smoke test

With the application running:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
```

The script:

1. creates a unique custom alias;
2. verifies an initial click count of zero;
3. performs three redirects;
4. verifies a final click count of three;
5. verifies that `lastAccessedAt` is populated.

Expected:

```text
Smoke test passed.
Short code: smoke-...
Click count: 3
Last accessed: ...
```

## 8. Optional PostgreSQL verification

Connect to PostgreSQL and run:

```sql
SELECT
    short_code,
    original_url,
    click_count,
    created_at,
    last_accessed_at
FROM url_mapping
ORDER BY created_at DESC;
```

For a specific alias:

```sql
SELECT
    short_code,
    click_count,
    last_accessed_at
FROM url_mapping
WHERE short_code = 'travel-2026';
```

The database values should match the analytics API.

## 9. GitHub Actions verification

After pushing the quality-gate commit:

1. Open the repository on GitHub.
2. Open **Actions**.
3. Select **Build and test**.
4. Confirm the latest workflow completed successfully.

The workflow runs:

```text
mvn --batch-mode clean verify
```

## 10. Final test evidence

Record the final result in `docs/QUALITY_GATES_AND_EVIDENCE.md`:

```text
mvn clean verify: PASS
Smoke test: PASS
Swagger verification: PASS
Postman or PowerShell redirect verification: PASS
GitHub Actions: PASS
Secret scan reviewed: YES
Public repository checked: YES
```

Swagger and Postman are complementary to the automated tests. Maven tests and
CI remain the deterministic quality gates; Swagger, Postman, PowerShell, and
database queries provide manual end-to-end evidence.
