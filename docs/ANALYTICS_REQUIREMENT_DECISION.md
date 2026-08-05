# Analytics Requirement Decision

## Original ambiguity

The assignment requests "basic analytics" but does not define whether that
means aggregate counters, event-level history, unique visitors, geographic
breakdowns, or personally identifiable tracking.

## Clarified prototype scope

This prototype implements privacy-conscious aggregate analytics:

- total redirect count;
- short URL creation time;
- most recent redirect time;
- destination URL.

It intentionally does not collect IP addresses, user agents, cookies,
geolocation, device identifiers, referrers, or per-click event history.

## Concurrency decision

A redirect records analytics and retrieves the destination in one PostgreSQL
statement:

```sql
UPDATE url_mapping
SET
    click_count = click_count + 1,
    last_accessed_at = CURRENT_TIMESTAMP
WHERE short_code = :shortCode
RETURNING original_url;
```

This avoids a read-modify-write race in application code and prevents lost
increments under concurrent requests.

## Failure semantics

- Existing short code: increment the count and return HTTP 302.
- Unknown short code: no row is updated and the API returns HTTP 404.
- Database unavailable: the existing dependency error handler returns HTTP 503.

The prototype chooses accurate synchronous counting. At much higher scale,
redirect latency could be reduced by publishing click events asynchronously
and accepting explicitly documented eventual consistency.
