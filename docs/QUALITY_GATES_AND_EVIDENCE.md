# Quality Gates and Evidence

## Automated quality gate

Run from the repository root:

```powershell
mvn clean verify
```

The same command runs in GitHub Actions for pushes to `main` and pull requests.

The automated test suite covers:

- HTTP/HTTPS URL validation
- Base62 short-code generation
- bounded collision retry and retry exhaustion
- custom-alias policy
- duplicate custom-alias behavior
- generated-code regression behavior
- atomic redirect-service interaction
- analytics-service mapping
- creation API HTTP contract
- redirect HTTP contract and `Cache-Control: no-store`
- analytics API HTTP contract
- structured malformed-request handling

## Repeatable local smoke test

Start PostgreSQL and the application, then run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
```

The script creates a unique custom alias, verifies an initial count of zero,
performs three redirects, and verifies a final count of three with a populated
last-accessed timestamp.

## Development evidence observed on 2026-08-05

| Check | Observed result |
|---|---|
| Generated URL creation | Created `LvcAuAlGCS` for `https://www.example.com` |
| Redirect | HTTP 302 with the expected `Location` header and `Cache-Control: no-store` |
| Malformed request | HTTP 400 with `VALIDATION_FAILED` and a correlation identifier |
| Unknown code | HTTP 404 with `SHORT_URL_NOT_FOUND` |
| Duplicate custom alias | HTTP 409 with `CUSTOM_ALIAS_ALREADY_EXISTS` after exception mapping was corrected |
| Reserved alias | HTTP 400 with `RESERVED_CUSTOM_ALIAS` |
| Generated-code regression | A new 10-character code was created after the brownfield change |
| Analytics | Count changed from 0 to 1 after one redirect |
| Analytics read | A second analytics read did not increment the count |
| Last-accessed time | Populated after the redirect |

## Security and repository gate

Before the final push:

```powershell
git status
git diff --cached
git grep -n -I -E "DB_PASSWORD|password\s*=|BEGIN (RSA|OPENSSH|PRIVATE) KEY"
git ls-files | Select-String -Pattern "\.env$|INTERVIEW|target/|\.zip$"
```

Review every match. Placeholders such as `${DB_PASSWORD}` and documentation
that says not to commit passwords are acceptable; real credentials are not.

## Final sign-off record

Complete this after the final verification:

```text
Reviewer: chaitanya
Date:05/08/2026
Commit:71991e4
mvn clean verify: PASS
Local smoke test: PASS
Docker startup: PASS
Docker smoke test: PASS
Docker clean-clone test: PASS
Swagger verification: PASS
Redirect verification using curl/Postman: PASS
GitHub Actions: PASS
Secret scan reviewed: YES
Public repository checked in incognito: YES
```
