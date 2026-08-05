# API Design

## Project

AI-Assisted Production-Grade URL Shortener

## 1. Purpose

This document defines the external HTTP contract for the URL Shortener prototype.

The API design aims to be:

- simple to evaluate through Swagger, Postman, or cURL;
- resource-oriented;
- versioned for management APIs;
- compact for public redirect URLs;
- explicit about validation and error behavior;
- extensible without premature complexity.

---

## 2. API Design Principles

1. **Management APIs are versioned**
   - Management operations use `/api/v1/...`.
   - This allows future contract evolution without changing public redirect links.

2. **Redirect URLs remain compact**
   - Public redirection uses `/{shortCode}`.
   - Example: `http://localhost:8080/aB91xK2LmQ`.

3. **HTTP semantics are used consistently**
   - `201 Created` for successful URL creation.
   - `302 Found` for redirection.
   - `400 Bad Request` for invalid input.
   - `404 Not Found` for an unknown short code.
   - `409 Conflict` for duplicate custom aliases.
   - `503 Service Unavailable` when a short code cannot be allocated or a required dependency is unavailable.

4. **External API models are separated from persistence entities**
   - Controllers expose request and response DTOs.
   - Database entities are not serialized directly.

5. **Errors use one stable response structure**
   - Clients should not parse arbitrary framework exception messages.

6. **The configured application base URL is used to construct the public short URL**
   - Only the short code is persisted.

---

## 3. Endpoint Summary

| Method | Endpoint | Purpose | Success Status |
|---|---|---|---|
| `POST` | `/api/v1/urls` | Create a short URL | `201 Created` |
| `GET` | `/{shortCode}` | Redirect to the original URL | `302 Found` |
| `GET` | `/api/v1/urls/{shortCode}/analytics` | Retrieve URL analytics | `200 OK` |

The prototype intentionally excludes update, delete, bulk-list, authentication, and administrative APIs because they are outside the stated core scope.

---

# 4. Create Short URL

## 4.1 Endpoint

```http
POST /api/v1/urls
Content-Type: application/json
Accept: application/json
```

## 4.2 Request body

```json
{
  "originalUrl": "https://www.irctc.co.in",
  "customAlias": "irctc"
}
```

`customAlias` is optional.

### Request schema

| Field | Type | Required | Rules |
|---|---|---:|---|
| `originalUrl` | string | Yes | Absolute URL, `http` or `https`, maximum 2048 characters, host required |
| `customAlias` | string | No | 3–32 characters, lowercase letters, digits, and hyphens only; must start and end with a letter or digit |

### Custom-alias examples

Valid:

```text
irctc
travel-2026
campaign42
```

Invalid:

```text
ab
IRCTC
-my-link
my-link-
my_link
api
swagger-ui
actuator
```

### Reserved aliases

The application shall reject aliases that may conflict with application routes or operational endpoints.

Initial reserved values:

```text
api
swagger-ui
swagger-ui.html
v3
actuator
error
favicon.ico
```

Reserved-alias matching is case-insensitive.

## 4.3 Automatic-code behavior

When `customAlias` is omitted or blank:

1. Generate a 10-character Base62 code using `SecureRandom`.
2. Attempt an atomic database insert.
3. If the code conflicts with an existing row, immediately generate another value.
4. Retry up to the configured maximum.
5. Return `503 Service Unavailable` only if allocation fails after all attempts.

Example generated code:

```text
aB91xK2LmQ
```

## 4.4 Duplicate original URL behavior

Each create request without a custom alias creates a new mapping, even when the original URL already exists.

Rationale:

- different campaigns may need different short links;
- click analytics should remain independent;
- future expiry or ownership rules may differ;
- the assignment does not require URL deduplication.

Idempotent creation through an `Idempotency-Key` header is documented as a future enhancement rather than implemented in the prototype.

## 4.5 Success response

```http
HTTP/1.1 201 Created
Content-Type: application/json
Location: http://localhost:8080/irctc
```

```json
{
  "shortCode": "irctc",
  "shortUrl": "http://localhost:8080/irctc",
  "originalUrl": "https://www.irctc.co.in",
  "createdAt": "2026-08-05T00:30:00Z"
}
```

### Response schema

| Field | Type | Description |
|---|---|---|
| `shortCode` | string | Persisted generated code or custom alias |
| `shortUrl` | string | Public URL constructed from configured base URL and short code |
| `originalUrl` | string | Validated destination URL |
| `createdAt` | ISO-8601 timestamp | Creation time in UTC |

## 4.6 Error responses

### Invalid request

```http
HTTP/1.1 400 Bad Request
```

```json
{
  "timestamp": "2026-08-05T00:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_FAILED",
  "message": "Request validation failed.",
  "path": "/api/v1/urls",
  "correlationId": "4c22cc0b-55a9-4ff0-8a94-ea5db873d9ae",
  "violations": [
    {
      "field": "originalUrl",
      "message": "must be an absolute HTTP or HTTPS URL"
    }
  ]
}
```

### Duplicate custom alias

```http
HTTP/1.1 409 Conflict
```

```json
{
  "timestamp": "2026-08-05T00:30:00Z",
  "status": 409,
  "error": "Conflict",
  "code": "CUSTOM_ALIAS_ALREADY_EXISTS",
  "message": "The requested custom alias is already in use.",
  "path": "/api/v1/urls",
  "correlationId": "4c22cc0b-55a9-4ff0-8a94-ea5db873d9ae",
  "violations": []
}
```

### Reserved custom alias

```http
HTTP/1.1 400 Bad Request
```

```json
{
  "timestamp": "2026-08-05T00:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "RESERVED_CUSTOM_ALIAS",
  "message": "The requested custom alias is reserved.",
  "path": "/api/v1/urls",
  "correlationId": "4c22cc0b-55a9-4ff0-8a94-ea5db873d9ae",
  "violations": []
}
```

### Generated-code allocation failure

```http
HTTP/1.1 503 Service Unavailable
Retry-After: 1
```

```json
{
  "timestamp": "2026-08-05T00:30:00Z",
  "status": 503,
  "error": "Service Unavailable",
  "code": "SHORT_CODE_ALLOCATION_FAILED",
  "message": "A short code could not be allocated. Please retry.",
  "path": "/api/v1/urls",
  "correlationId": "4c22cc0b-55a9-4ff0-8a94-ea5db873d9ae",
  "violations": []
}
```

---

# 5. Redirect Short URL

## 5.1 Endpoint

```http
GET /{shortCode}
```

Example:

```http
GET /irctc
```

## 5.2 Path parameter

| Parameter | Type | Rules |
|---|---|---|
| `shortCode` | string | Generated Base62 code or valid custom alias |

## 5.3 Success response

```http
HTTP/1.1 302 Found
Location: https://www.irctc.co.in
Cache-Control: no-store
```

The response body is empty.

## 5.4 Why `302 Found`?

`302 Found` is selected instead of `301 Moved Permanently`.

Reasons:

- `301` may be cached aggressively by browsers and intermediaries;
- destination URLs may need to change in a future enhancement;
- cached redirects could bypass the service and cause analytics undercounting;
- `302` better matches a dynamic redirect service.

`Cache-Control: no-store` is added so redirect requests continue reaching the service, allowing click analytics to be recorded.

## 5.5 Analytics behavior

For a valid mapping:

1. Resolve the destination by indexed short-code lookup.
2. Update `click_count` atomically.
3. Update `last_accessed_at`.
4. Return the redirect response.

The redirect is the primary business action. Analytics should be implemented so that an analytics-only failure does not intentionally create a redirect loop or expose an internal error after the destination has already been resolved.

## 5.6 Unknown code

```http
HTTP/1.1 404 Not Found
Content-Type: application/json
```

```json
{
  "timestamp": "2026-08-05T00:30:00Z",
  "status": 404,
  "error": "Not Found",
  "code": "SHORT_URL_NOT_FOUND",
  "message": "No URL mapping exists for the supplied short code.",
  "path": "/unknown-code",
  "correlationId": "4c22cc0b-55a9-4ff0-8a94-ea5db873d9ae",
  "violations": []
}
```

## 5.7 HTTP method decision

Only `GET` is supported for redirect behavior in the prototype.

`POST`, `PUT`, `PATCH`, and `DELETE` requests to a short URL are not redirected because forwarding non-idempotent methods can create unsafe or surprising behavior.

---

# 6. Retrieve Analytics

## 6.1 Endpoint

```http
GET /api/v1/urls/{shortCode}/analytics
Accept: application/json
```

## 6.2 Success response

```http
HTTP/1.1 200 OK
Content-Type: application/json
Cache-Control: no-store
```

```json
{
  "shortCode": "irctc",
  "shortUrl": "http://localhost:8080/irctc",
  "originalUrl": "https://www.irctc.co.in",
  "clickCount": 42,
  "createdAt": "2026-08-05T00:30:00Z",
  "lastAccessedAt": "2026-08-05T01:15:22Z"
}
```

Before the first redirect:

```json
{
  "shortCode": "irctc",
  "shortUrl": "http://localhost:8080/irctc",
  "originalUrl": "https://www.irctc.co.in",
  "clickCount": 0,
  "createdAt": "2026-08-05T00:30:00Z",
  "lastAccessedAt": null
}
```

### Response schema

| Field | Type | Description |
|---|---|---|
| `shortCode` | string | Stored short code |
| `shortUrl` | string | Public short URL |
| `originalUrl` | string | Destination URL |
| `clickCount` | integer | Number of successfully recorded redirects |
| `createdAt` | ISO-8601 timestamp | Creation time in UTC |
| `lastAccessedAt` | ISO-8601 timestamp or null | Last successfully recorded redirect |

## 6.3 Unknown code

Returns:

```http
HTTP/1.1 404 Not Found
```

with error code:

```text
SHORT_URL_NOT_FOUND
```

## 6.4 Access-control limitation

Analytics is unauthenticated in the prototype so the evaluator can exercise the API easily.

In a production SaaS model, analytics would require authentication and authorization so one user cannot inspect another user's link statistics.

---

# 7. Standard Error Contract

All JSON errors follow this structure:

```json
{
  "timestamp": "2026-08-05T00:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_FAILED",
  "message": "Request validation failed.",
  "path": "/api/v1/urls",
  "correlationId": "4c22cc0b-55a9-4ff0-8a94-ea5db873d9ae",
  "violations": [
    {
      "field": "originalUrl",
      "message": "must be an absolute HTTP or HTTPS URL"
    }
  ]
}
```

## Error fields

| Field | Type | Purpose |
|---|---|---|
| `timestamp` | timestamp | Failure time in UTC |
| `status` | integer | HTTP status code |
| `error` | string | Standard HTTP reason |
| `code` | string | Stable application error code |
| `message` | string | Human-readable explanation |
| `path` | string | Request path |
| `correlationId` | string | Identifier used to correlate client response and server logs |
| `violations` | array | Field-specific validation errors; empty for non-field errors |

## Initial application error codes

| Error Code | HTTP Status | Meaning |
|---|---:|---|
| `VALIDATION_FAILED` | 400 | Request fields failed validation |
| `INVALID_URL` | 400 | Destination URL is malformed or unsupported |
| `INVALID_CUSTOM_ALIAS` | 400 | Alias does not match the accepted format |
| `RESERVED_CUSTOM_ALIAS` | 400 | Alias conflicts with a reserved route |
| `SHORT_URL_NOT_FOUND` | 404 | No mapping exists |
| `CUSTOM_ALIAS_ALREADY_EXISTS` | 409 | Requested alias is already used |
| `SHORT_CODE_ALLOCATION_FAILED` | 503 | Generated code could not be allocated within retry limit |
| `DEPENDENCY_UNAVAILABLE` | 503 | Required dependency such as PostgreSQL is unavailable |
| `INTERNAL_ERROR` | 500 | Unexpected server failure |

---

# 8. URL Validation Rules

A destination is accepted only when all conditions are satisfied:

1. The value is present and non-blank.
2. Length is at most 2048 characters.
3. It parses as an absolute URI.
4. Scheme is exactly `http` or `https`, case-insensitively.
5. A host is present.
6. Credentials embedded in the URL are rejected.
7. Control characters are rejected.

Accepted:

```text
https://www.irctc.co.in
http://example.com/products?id=42
https://example.com/path#section
```

Rejected:

```text
irctc.co.in
www.irctc.co.in
javascript:alert(1)
file:///etc/passwd
ftp://example.com/file
https://user:password@example.com
```

## Security limitation

The prototype validates structure and scheme but does not call a domain-reputation service.

Production hardening should consider:

- malicious-domain screening;
- phishing controls;
- private-network and loopback restrictions where server-side URL fetching exists;
- administrative takedown support.

The redirect service does not fetch the destination server-side, so classic server-side request forgery is not part of the redirect flow. The destination is returned to the client through the `Location` header.

---

# 9. Content Negotiation and Encoding

- JSON APIs use `application/json`.
- All text is UTF-8.
- Timestamps use ISO-8601 UTC format.
- Redirect responses have an empty body.
- Unsupported content types return `415 Unsupported Media Type`.
- Unsupported response media types may return `406 Not Acceptable`.

---

# 10. API Versioning Strategy

Management endpoints are versioned through the URL:

```text
/api/v1/urls
```

Reasons:

- visible and easy to understand;
- easy to test through a browser or Swagger;
- avoids custom version headers;
- suitable for a public prototype.

The redirect endpoint is not versioned:

```text
/{shortCode}
```

A short link must remain stable and compact. API implementation changes should not invalidate previously issued short URLs.

---

# 11. OpenAPI and Swagger

The application will expose interactive API documentation.

Expected local locations:

```text
http://localhost:8080/swagger-ui/index.html
http://localhost:8080/v3/api-docs
```

OpenAPI documentation shall include:

- endpoint summaries;
- request and response schemas;
- validation descriptions;
- status codes;
- example payloads;
- error responses.

The redirect endpoint may be tested directly through the browser because Swagger clients may automatically follow redirects.

---

# 12. Correlation Identifier

The application should support a request correlation identifier.

Behavior:

1. Accept an optional `X-Correlation-ID` request header.
2. Generate one when absent.
3. Add it to application logs.
4. Return it through the `X-Correlation-ID` response header.
5. Include it in JSON error responses.

This is optional if time becomes constrained, but it is included in the API design because it materially improves production troubleshooting.

---

# 13. API Security and Abuse Considerations

## Prototype

- No authentication.
- No rate limiting.
- Public redirect.
- Public create and analytics endpoints for evaluator convenience.
- Strict structural validation.
- No secrets in payloads or logs.

## Production evolution

- Authentication for create and analytics APIs.
- Ownership checks.
- Per-user quotas.
- Rate limiting.
- Custom aliases restricted by subscription or policy.
- Audit logging.
- Malicious-domain controls.
- Administrative disable/takedown APIs.

---

# 14. Explicitly Deferred Endpoints

The following are not implemented in the prototype:

```http
PATCH /api/v1/urls/{shortCode}
DELETE /api/v1/urls/{shortCode}
GET /api/v1/urls
POST /api/v1/urls/bulk
```

Reasons:

- not required by the core assignment;
- would introduce ownership and authorization questions;
- list APIs require pagination and filtering design;
- deletion raises link-reuse and historical analytics decisions;
- the 24-hour scope prioritizes complete core flows and validation.

---

# 15. API Test Matrix

| Scenario | Expected Result |
|---|---|
| Valid URL without alias | `201`, generated 10-character code |
| Valid URL with available alias | `201`, requested alias returned |
| Same URL submitted twice without alias | Two independent mappings |
| Duplicate custom alias | `409` |
| Blank URL | `400` |
| Relative URL | `400` |
| `javascript:` URL | `400` |
| Alias shorter than 3 characters | `400` |
| Alias with uppercase or underscore | `400` |
| Reserved alias | `400` |
| Generated-code collision | Immediate retry |
| Retry limit exhausted | `503` |
| Existing short code opened | `302` with `Location` header |
| Redirect response | `Cache-Control: no-store` |
| Unknown short code opened | `404` |
| Analytics before first click | `200`, count `0`, last access `null` |
| Analytics after redirect | Count incremented and last access populated |
| Unknown analytics code | `404` |
| Unsupported request content type | `415` |

---

# 16. Key API Decisions

1. Only three endpoints are implemented.
2. Management APIs use `/api/v1`.
3. Redirect route remains `/{shortCode}`.
4. Creation returns `201 Created`.
5. Redirect uses `302 Found`.
6. Redirect includes `Cache-Control: no-store`.
7. Duplicate custom aliases return `409 Conflict`.
8. Generated collisions are retried rather than exposed to the client.
9. Same original URL may have multiple generated mappings.
10. Error responses use stable application error codes.
11. Custom aliases are lowercase and human-readable.
12. Analytics is public only for prototype evaluation.
13. No update or delete API is included.
14. Timestamps are returned in UTC.
15. Persistence entities are never exposed directly.

---

# 17. Future API Enhancements

Potential future capabilities:

- idempotent create requests through `Idempotency-Key`;
- authenticated ownership;
- expiration controls;
- destination updates;
- disable/delete operations;
- paginated link listing;
- richer time-series analytics;
- QR-code generation;
- custom domains;
- bulk URL creation;
- premium alias policies;
- administrative abuse controls.
