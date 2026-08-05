# Engineering Scenario Traceability

## Scenario 1: Greenfield core capability

**Commit:** `feat: implement generated URL creation and redirect`

The initial slice established:

- generated 10-character Base62 short codes;
- atomic insert with a database uniqueness constraint;
- bounded collision retries;
- HTTP 201 creation response;
- HTTP 302 redirect with no-store caching;
- URL validation, structured errors, and correlation identifiers;
- unit tests and local verification.

## Scenario 2: Brownfield enhancement

**Commit:** `feat: add optional custom aliases`

The existing creation flow was extended rather than replaced:

- `customAlias` remained optional;
- generated creation continued to work;
- alias syntax and reserved routes were validated;
- duplicate aliases returned HTTP 409;
- the existing database uniqueness constraint remained the final concurrency authority;
- regression testing demonstrated that the greenfield behavior was preserved.

Build and runtime testing exposed incomplete AI-generated integration changes.
Those issues were corrected before the commit was accepted.

## Scenario 3: Ambiguous analytics requirement

**Commit:** `feat: add atomic redirect analytics`

"Basic analytics" was clarified as aggregate, privacy-conscious information:

- total redirect count;
- creation time;
- most recent redirect time;
- destination URL.

The implementation deliberately excludes visitor identity and event-level
personal data.

Redirect counting uses one PostgreSQL statement:

```sql
UPDATE url_mapping
SET click_count = click_count + 1,
    last_accessed_at = CURRENT_TIMESTAMP
WHERE short_code = :shortCode
RETURNING original_url;
```

This makes the increment and destination lookup one atomic database operation
and avoids lost updates from application-level read-modify-write logic.

## Human control points

At every scenario boundary, the engineer:

1. reviewed the proposed change;
2. built the project;
3. ran tests;
4. started the application;
5. exercised the affected API;
6. corrected defects;
7. inspected the Git diff;
8. committed and pushed the accepted increment.
