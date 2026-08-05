# AI Usage and Traceability

## 1. Delivery model

AI was used as an engineering assistant, not as an autonomous owner of the
solution.

The engineer retained responsibility for:

- clarifying requirements;
- accepting, rejecting, or revising design proposals;
- deciding implementation scope and commit boundaries;
- configuring the local environment;
- reviewing generated code;
- running builds and tests;
- diagnosing failures;
- approving fixes;
- validating API behavior;
- checking repository security;
- deciding what was safe to publish;
- providing final sign-off.

Generated output was not treated as complete merely because it compiled or
looked plausible. Each increment was accepted only after deterministic build,
test, runtime, and manual verification.

## 2. Design ownership

Several important design decisions were reviewed and refined by the engineer
rather than copied directly from AI output.

| Decision | Engineer review or challenge | Final decision |
|---|---|---|
| Persistence model | The engineer required the database to store the environment-neutral `shortCode`, not a complete short URL containing a host name that changes between environments. | Store `short_code` and construct the public URL from `PUBLIC_BASE_URL`. |
| Delivery approach | The engineer required the work to be separated into visible greenfield, brownfield, and ambiguous-requirement stages instead of submitting one large AI-generated change. | Separate commits were created for bootstrap, generated creation/redirect, custom aliases, and analytics. |
| Custom alias behavior | The engineer required optional user-selected aliases while preserving generated-code behavior. Duplicate user aliases must not be silently changed. | Optional custom aliases; duplicate aliases return HTTP `409 Conflict`; generated-code collisions use bounded retry. |
| Collision handling | A pre-check followed by insert was not accepted as the source of truth because concurrent requests could still collide. | PostgreSQL unique constraint plus `INSERT ... ON CONFLICT DO NOTHING RETURNING`. |
| Redirect semantics | Permanent redirect caching was not accepted for a prototype whose destination may later change. | HTTP `302 Found` with `Cache-Control: no-store`. |
| Analytics scope | The term “basic analytics” was considered ambiguous and was not expanded into visitor tracking without a requirement. | Aggregate click count, creation time, and last-accessed time only; no IP address, cookie, user-agent, device, or geolocation collection. |
| Analytics concurrency | Reading the count into Java, incrementing it, and writing it back was not accepted because concurrent updates could be lost. | One atomic PostgreSQL `UPDATE ... RETURNING` statement records the click and returns the destination. |
| Secret management | Hardcoded passwords and committing local environment files were not accepted. | Credentials are supplied through local environment variables; `.env`, passwords, private keys, and private interview material are excluded from the repository. |
| Quality control | AI-generated code was not accepted based only on code generation. | Maven tests, application startup, API smoke tests, database verification, Git diff review, and CI were used as acceptance gates. |

## 3. Increment traceability

### 3.1 Bootstrap

**AI contribution**

- proposed Spring Boot project structure;
- proposed Maven dependencies;
- drafted externalized datasource configuration;
- drafted the Flyway schema;
- drafted initial documentation and tests.

**Engineer contribution**

- installed and configured Java, Maven, Git, and PostgreSQL;
- created the application database and least-privilege database user;
- selected port `8081` because the default port was already occupied;
- configured environment variables;
- started the application and verified Actuator health;
- created and reviewed the public repository;
- removed private interview preparation from public Git history;
- rotated an exposed local password and ensured it was not stored in committed files.

**Acceptance evidence**

- application startup succeeded;
- database migration succeeded;
- Actuator health returned successfully;
- repository history and public contents were manually checked.

### 3.2 Greenfield URL creation and redirect

**AI contribution**

- generated URL-validation logic;
- generated 10-character Base62 code generation;
- generated command/query repositories;
- generated URL creation and redirect services;
- generated structured error responses and tests.

**Engineer contribution**

- reviewed the storage model;
- required short-code-only persistence;
- verified request and response semantics;
- corrected PowerShell request syntax for the local environment;
- manually created a short URL;
- copied the actual generated code into the redirect test;
- verified HTTP `302`, `Location`, and `Cache-Control` headers;
- committed the greenfield slice before starting the brownfield enhancement.

**Acceptance evidence**

- generated code `LvcAuAlGCS` was created for
  `https://www.example.com`;
- redirect returned HTTP `302`;
- unknown codes returned HTTP `404`;
- malformed requests returned structured HTTP `400` responses with a
  correlation identifier.

### 3.3 Brownfield custom-alias enhancement

**AI contribution**

- proposed extending the existing request with optional `customAlias`;
- generated alias policy, conflict exception, service changes, and tests;
- proposed reserved-name and duplicate-alias handling.

**Engineer review, rejection, and correction**

The first generated brownfield overlay was not accepted as complete.

The engineer ran `mvn clean verify` and found that the test code expected the
new `UrlCreationService` constructor and method signature, while the main
source still contained the old greenfield implementation. This exposed an
incomplete overlay/extraction.

After that was corrected, runtime testing showed that
`CustomAliasAlreadyExistsException` was reaching the generic exception handler
and being logged as an unexpected failure. The engineer inspected
`GlobalExceptionHandler`, confirmed that the required imports and handlers were
missing, and requested a correction.

The engineer did not commit the brownfield change until:

- the build passed;
- custom-alias creation succeeded;
- duplicate alias returned HTTP `409`;
- reserved alias returned HTTP `400`;
- generated-code creation still worked;
- the final Git diff was reviewed.

**Acceptance evidence**

- `travel-2026` was created successfully;
- duplicate `travel-2026` returned
  `CUSTOM_ALIAS_ALREADY_EXISTS`;
- reserved alias `api` returned `RESERVED_CUSTOM_ALIAS`;
- generated 10-character codes continued to work after the enhancement.

### 3.4 Ambiguous analytics requirement

**AI contribution**

- proposed a privacy-conscious aggregate interpretation;
- generated analytics DTO, service, controller, repository update, and tests;
- proposed one atomic PostgreSQL statement for click counting.

**Engineer review, rejection, and correction**

The first analytics overlay was not accepted after code generation.

Application startup failed because `UrlMappingRowMapper` had not been
registered as a Spring bean. The engineer captured the exact startup failure,
identified that constructor injection could not resolve the mapper, and
required the missing Spring registration to be fixed.

Later, the final quality-gate test failed because standalone MockMvc serialized
`Instant` as a numeric epoch value instead of the ISO-8601 format expected by
the API contract. The engineer ran the complete suite, found the failure, and
required the test `ObjectMapper` to be configured with Java time support rather
than weakening the expected API contract.

The engineer also verified an important behavioral rule: reading analytics
must not increment the click count. Only a redirect should increment it.

**Acceptance evidence**

- analytics initially returned `clickCount = 0`;
- one redirect changed the count to `1`;
- the redirect populated `lastAccessedAt`;
- reading analytics again left the count unchanged;
- the complete test suite passed after the serialization correction.

## 4. Generated, edited, rejected, and deferred

### Generated with AI assistance

- project skeleton and Maven configuration;
- Flyway migration;
- controllers, DTOs, services, repositories, validators, and exception types;
- unit and HTTP-contract tests;
- PowerShell verification commands;
- architecture, API, schema, scenario, and quality-gate documentation;
- GitHub Actions workflow.

### Edited or corrected through engineer review

- environment-specific configuration;
- server port;
- request syntax used in PowerShell;
- `UrlCreationService` brownfield integration;
- custom-alias exception mappings;
- Spring registration of `UrlMappingRowMapper`;
- MockMvc Java-time serialization;
- README and public/private documentation boundaries;
- commit ordering and messages;
- test data and manual verification steps.

### Rejected or not adopted

- hardcoded database credentials;
- committing `.env`, private keys, build output, ZIP files, or private interview
  preparation;
- storing a full environment-specific short URL in the database;
- separate “check then insert” collision handling;
- silently replacing a conflicting custom alias;
- unbounded collision retry;
- Java read-modify-write click counting;
- permanent `301` redirect caching for this mutable prototype;
- collecting visitor-identifying analytics without an explicit requirement;
- unsupported performance or internet-scale claims;
- autonomous commits or publication without human review.

### Deferred for a production evolution

- authentication and authorization;
- tenant isolation;
- rate limiting and abuse controls;
- URL expiration, disablement, and deletion;
- Redis caching;
- asynchronous click-event processing;
- multi-region routing;
- database partitioning or sharding;
- browser UI;
- advanced analytics.

These were documented as future considerations rather than presented as
implemented capabilities.

## 5. Quality gates used before acceptance

Every accepted increment passed the applicable controls:

1. source review;
2. `mvn clean verify`;
3. application startup;
4. manual API verification;
5. error-path verification;
6. regression testing;
7. database-result verification where applicable;
8. Git diff inspection;
9. secret/private-file review;
10. separate commit and push;
11. public repository verification;
12. GitHub Actions verification for the final build.

## 6. AI limitations observed

The exercise produced direct evidence that AI-generated output can be locally
plausible but incomplete across file boundaries.

Observed limitations included:

- a test/main-source signature mismatch after a brownfield overlay;
- missing business-exception mappings;
- missing Spring bean registration;
- a test serializer that did not match the application’s API format;
- shell commands that needed adjustment for PowerShell behavior;
- the need for explicit controls to prevent private or secret material from
  entering a public repository.

These were not hidden. They demonstrate why the delivery model requires build,
runtime, security, and human-review gates.

## 7. Final accountability statement

AI accelerated drafting and implementation, but it did not own the solution.

The engineer:

- decided what behavior was required;
- challenged and revised design proposals;
- rejected incomplete output;
- found integration defects through deterministic testing;
- approved each correction;
- verified end-to-end behavior;
- controlled repository publication;
- accepted final responsibility for the submitted implementation.
