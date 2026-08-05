# Engineering Scenarios

## Project

AI-Assisted Production-Grade URL Shortener

## 1. Purpose

The assignment requires three engineering scenarios—greenfield, brownfield, and ambiguous—and expects each scenario to demonstrate:

- requirement understanding;
- task decomposition;
- multi-step execution;
- AI-assisted engineering;
- validation and quality gates;
- risks, trade-offs, and engineer ownership.

This document defines the three selected scenarios and the evidence that will be captured during implementation.

> **Current status:** Scenario design and decomposition are complete. Execution evidence, test results, and Git commit references will be added as implementation progresses.

---

## 2. Selected Scenarios

| Scenario | Selected Work | Why It Fits |
|---|---|---|
| Greenfield | Build the core URL shortener from scratch | New application, schema, APIs, generation, persistence, redirect |
| Brownfield | Add optional custom aliases to the existing generated-code service | Requires impact analysis and safe modification of existing API, domain, persistence, validation, tests, and documentation |
| Ambiguous | Interpret and implement “Add analytics” | The phrase does not define metrics, granularity, counting rules, privacy, consistency, or access control |

These scenarios are implemented within one evolving codebase rather than as three separate applications.

---

# 3. Scenario One — Greenfield

## 3.1 Original Requirement

> Build a URL shortener service from scratch with core APIs and reliable redirection.

## 3.2 Requirement Interpretation

The minimum useful product must:

1. accept an absolute HTTP or HTTPS URL;
2. generate a compact code;
3. persist the code-to-destination mapping;
4. return a public short URL;
5. redirect a valid short code to the original destination;
6. return a controlled error for invalid or unknown inputs;
7. remain testable and environment-independent.

The initial greenfield slice uses automatically generated short codes. Custom aliases are deliberately introduced later as the brownfield scenario.

## 3.3 Assumptions

- Backend-only service; Swagger, Postman, or cURL is sufficient for evaluation.
- Java 21, Spring Boot 3.x, Maven, and PostgreSQL.
- Generated codes use 10-character Base62 values.
- PostgreSQL is the authoritative source of truth.
- The redirect response uses HTTP `302 Found`.
- The application stores only the short code, not the complete environment-specific URL.
- Authentication is outside the initial scope.
- One application instance is used for local evaluation, but the service remains stateless.

## 3.4 Task Decomposition

| Order | Task | Dependency | Acceptance Criteria |
|---:|---|---|---|
| 1 | Bootstrap Spring Boot project | Environment setup | Application starts using Java 21 |
| 2 | Add external configuration | Project bootstrap | Base URL, DB settings, code length, retry limit are configurable |
| 3 | Add Flyway migration | PostgreSQL available | `url_mapping` table, constraints, and index created |
| 4 | Implement URL validation | DTO model | Invalid, relative, or unsupported URLs return `400` |
| 5 | Implement Base62 generator | Configuration | Correct length and alphabet; unit tested |
| 6 | Implement atomic insert repository | Schema | `ON CONFLICT DO NOTHING RETURNING` works |
| 7 | Implement bounded allocator | Generator and repository | Collision retries; exhaustion returns controlled failure |
| 8 | Implement create API | Service layer | `POST /api/v1/urls` returns `201` |
| 9 | Implement redirect API | Atomic persistence operation | `GET /{shortCode}` returns `302`; missing code returns `404` |
| 10 | Add structured error handling | Expected exceptions | Stable error contract with no stack trace |
| 11 | Add OpenAPI | Controllers and DTOs | APIs visible in Swagger |
| 12 | Add tests | Implemented flows | Unit and integration quality gates pass |
| 13 | Add documentation | Stable behavior | Setup, API, decisions, limitations documented |

## 3.5 AI-Assisted Execution Plan

AI may assist with:

- Spring Initializr dependency selection;
- first drafts of DTOs and validators;
- PostgreSQL migration review;
- Base62 generator implementation;
- unit-test generation;
- OpenAPI annotations;
- code review and failure-case analysis.

Each AI request must include:

- intent;
- relevant architecture constraints;
- acceptance criteria;
- security restrictions;
- expected output type;
- instruction not to change the public contract without approval.

## 3.6 Planned Execution Sequence

```text
Design approved
      ↓
Environment and project bootstrap
      ↓
Schema migration
      ↓
Validation and generation
      ↓
Atomic persistence
      ↓
Create API
      ↓
Redirect API
      ↓
Errors, logging, Swagger
      ↓
Unit and integration tests
      ↓
Human review and commit
```

## 3.7 Validation

### Functional

- valid URL creates a mapping;
- generated code has length 10 and Base62 characters;
- opening the code returns `302` with the correct `Location`;
- unknown code returns `404`;
- invalid URL returns `400`.

### Concurrency and persistence

- forced first-code collision retries successfully;
- retry exhaustion returns `503`;
- database uniqueness prevents duplicate persisted codes;
- mappings survive application restart.

### Quality gates

- `mvn clean verify`;
- migration applies to a clean database;
- no secrets or machine-specific paths committed;
- API contract matches `API_DESIGN.md`;
- documentation reviewed by the engineer.

## 3.8 Risks and Controls

| Risk | Control |
|---|---|
| Generated collision | Large namespace, DB unique constraint, bounded retry |
| Race condition during creation | Atomic insert rather than check-then-insert |
| Invalid destination | Custom HTTP/HTTPS validator |
| Environment-specific URL stored | Persist short code only; externalize base URL |
| AI-generated code accepted blindly | Human review, tests, and documented changes |

## 3.9 Evidence to Add After Execution

| Evidence | Value |
|---|---|
| Git commit | _Pending_ |
| Main classes | _Pending_ |
| Test command/result | _Pending_ |
| Swagger verification | _Pending_ |
| AI prompts and review outcome | _Pending_ |
| Deviations from design | _Pending_ |

---

# 4. Scenario Two — Brownfield

## 4.1 Change Request

> Enhance the existing generated-code URL shortener so users may optionally request a memorable custom alias.

## 4.2 Why This Is Brownfield

At this point, the codebase already contains:

- creation API;
- generated-code allocation;
- persistence;
- redirect behavior;
- error handling;
- tests;
- documentation.

The change must preserve all existing generated-code behavior while introducing a second creation path.

## 4.3 Requirement Questions

Before implementation:

1. Is the alias mandatory or optional?
2. Which characters and lengths are allowed?
3. Is matching case-sensitive?
4. What happens when an alias already exists?
5. Are application routes reserved?
6. Should custom aliases be retried or silently changed?
7. Can the same destination have several aliases?
8. Does this require authentication or premium access?

## 4.4 Normalized Requirement

For the prototype:

- `customAlias` is optional.
- It contains 3–32 lowercase letters, digits, or hyphens.
- It starts and ends with a letter or digit.
- Reserved route names are rejected.
- Duplicate aliases return `409 Conflict`.
- The application never silently replaces the requested alias.
- Multiple aliases may point to the same destination.
- Authentication and premium restrictions are documented as future work.

## 4.5 Codebase Impact Analysis

| Area | Existing Behavior | Required Change |
|---|---|---|
| API request DTO | Original URL only | Add optional `customAlias` |
| API documentation | Generated code only | Document alias rules and `409` |
| Creation service | Always allocator path | Branch between custom and generated flows |
| Domain policy | None | Add reserved-alias and normalization policy |
| Persistence | Generated atomic insert | Reuse atomic insert for exact alias |
| Database schema | `short_code` supports generated value | Confirm length/character constraints support aliases |
| Error model | Validation/not-found/allocation | Add reserved and duplicate-alias codes |
| Tests | Generated path | Add valid, invalid, reserved, and concurrent duplicate tests |
| Documentation | Generated behavior | Update requirements, API, decision log, and examples |

## 4.6 Task Decomposition

| Order | Task | Acceptance Criteria |
|---:|---|---|
| 1 | Review schema compatibility | Existing migration supports 3–32 character aliases |
| 2 | Extend request DTO | Alias remains optional and validated |
| 3 | Add `CustomAliasPolicy` | Reserved and policy checks are independently tested |
| 4 | Extend creation service | Existing generated path remains unchanged |
| 5 | Reuse atomic insert | Concurrent duplicate requests produce one winner |
| 6 | Add exceptions and error mapping | Duplicate returns `409`; reserved returns `400` |
| 7 | Update OpenAPI | Alias constraints and examples are visible |
| 8 | Add regression tests | Original generated flow still passes |
| 9 | Update documentation | Impact and trade-offs are recorded |

## 4.7 AI-Assisted Brownfield Reasoning

AI assistance should be supplied with:

- current package tree;
- relevant classes only;
- existing API contract;
- database constraints;
- explicit instruction to preserve generated-code behavior;
- required acceptance criteria.

AI should be asked first for an **impact analysis**, not immediate code changes.

Example task framing:

> Analyze the existing URL creation flow and identify the minimum impacted classes required to add an optional custom alias. Preserve the generated-code path, public response contract, atomic database insert, and existing tests. Do not generate code yet.

After human approval, AI may draft focused patches and tests.

## 4.8 Validation

### New behavior

- available custom alias returns `201`;
- duplicate alias returns `409`;
- reserved alias returns `400`;
- invalid syntax returns `400`;
- concurrent requests for the same alias produce exactly one stored mapping.

### Regression

- generated code still works;
- generated collisions still retry;
- redirect behavior is unchanged;
- analytics behavior is unchanged;
- prior tests remain green.

### Safe-change review

- no unnecessary schema rewrite;
- no controller-to-repository shortcut;
- no duplicate validation logic;
- no silent alias substitution.

## 4.9 Risks and Controls

| Risk | Control |
|---|---|
| Breaking existing creation flow | Separate branch and regression suite |
| Route conflict | Configurable reserved-alias policy |
| Concurrent duplicate alias | Database unique constraint |
| Scope expands into identity/subscription | Document premium controls as future enhancement |
| AI modifies unrelated code | Provide focused context and review diff before acceptance |

## 4.10 Evidence to Add After Execution

| Evidence | Value |
|---|---|
| Git commit | _Pending_ |
| Impacted files | _Pending_ |
| Regression test result | _Pending_ |
| Concurrent alias test | _Pending_ |
| AI impact analysis | _Pending_ |
| Accepted/edited/rejected suggestions | _Pending_ |

---

# 5. Scenario Three — Ambiguous Requirement

## 5.1 Original Requirement

> Add analytics to the URL shortener.

This requirement is incomplete. “Analytics” could mean anything from a total click count to a full event platform with device, geography, referrer, user identity, and time-series dashboards.

## 5.2 Ambiguities Identified

1. Which metrics are required?
2. Count every request or only successful redirects?
3. Track total clicks or unique visitors?
4. Is time-series history required?
5. Is the latest access time required?
6. Are bots included?
7. Should failed or expired links be counted?
8. Is analytics strongly consistent?
9. Who may access analytics?
10. Are IP addresses or personal data stored?
11. Must analytics failures block redirects?
12. What performance overhead is acceptable?

## 5.3 Clarification Outcome / Prototype Assumptions

The normalized prototype requirement is:

- expose aggregate analytics per short code;
- return:
  - total successfully recorded redirect count;
  - creation timestamp;
  - last-accessed timestamp;
  - original URL;
  - short URL;
- count only requests where an active mapping exists;
- do not attempt unique-visitor calculation;
- do not store IP address, user agent, geography, or referrer;
- use an atomic database increment;
- expose analytics without authentication only for evaluator convenience;
- document event-based analytics and authorization as future enhancements.

## 5.4 Acceptance Criteria

1. A new mapping reports `clickCount = 0`.
2. `lastAccessedAt` is `null` before the first redirect.
3. Each successful redirect increments the count once.
4. Each successful redirect updates `lastAccessedAt`.
5. Unknown short codes do not create analytics.
6. Concurrent redirects do not lose increments.
7. Analytics retrieval for an unknown code returns `404`.
8. The response contains no personal or sensitive data.

## 5.5 Task Decomposition

| Order | Task | Acceptance Criteria |
|---:|---|---|
| 1 | Normalize analytics scope | Aggregate-only definition documented |
| 2 | Review schema | `click_count` and `last_accessed_at` available |
| 3 | Design atomic redirect update | Increment and destination resolution in one statement |
| 4 | Implement redirect service integration | Successful redirect records analytics |
| 5 | Implement analytics query service | Indexed lookup and DTO mapping |
| 6 | Add analytics endpoint | `GET /api/v1/urls/{shortCode}/analytics` returns `200` |
| 7 | Add tests | Zero state, increments, concurrency, not found |
| 8 | Document limitations | No unique users, time series, or personal data |

## 5.6 AI-Assisted Ambiguity Resolution

AI may be used to:

- enumerate possible meanings of “analytics”;
- identify privacy and performance risks;
- compare synchronous and asynchronous designs;
- draft acceptance criteria;
- generate concurrency test ideas.

The engineer must decide the final scope.

A useful prompt structure:

> The requirement says only “Add analytics” to an existing URL shortener. Do not write code. Identify ambiguities across product scope, counting semantics, privacy, consistency, access control, performance, and failure behavior. Then propose three implementation scopes—minimal, production-intermediate, and internet-scale—with trade-offs.

The chosen scope must then be documented before implementation.

## 5.7 Execution Design

### Prototype operation

```sql
UPDATE url_mapping
SET
    click_count = click_count + 1,
    last_accessed_at = CURRENT_TIMESTAMP
WHERE short_code = :shortCode
RETURNING original_url;
```

This one statement:

- resolves the destination;
- increments the count;
- updates the last-access time;
- prevents lost updates;
- avoids a second database round trip.

### Future high-scale operation

```text
Redirect lookup
      ↓
Return destination quickly
      ↓
Publish click event
      ↓
Asynchronous analytics processor
      ↓
Analytics store
```

The future design reduces redirect latency but introduces eventual consistency and event-delivery concerns.

## 5.8 Validation

### Unit and integration

- new mapping returns zero analytics;
- redirect updates analytics;
- multiple redirects produce the expected count;
- concurrent requests do not lose updates;
- unknown code returns `404`;
- timestamps are UTC instants;
- API output contains no personal data.

### Trade-off review

The selected synchronous aggregate approach is accepted because:

- the prototype needs immediate, simple analytics;
- the operation is atomic;
- one table is sufficient;
- richer event analytics is outside the time-boxed scope.

## 5.9 Risks and Controls

| Risk | Control |
|---|---|
| Requirement expands into a reporting platform | Explicit aggregate-only scope |
| Lost increments | Atomic SQL update |
| Redirect latency from analytics write | One DB round trip now; async path documented |
| Privacy exposure | No IP, user-agent, or identity storage |
| Unauthorized analytics access | Documented prototype limitation and future auth requirement |
| Blind AI scope expansion | Human selects and signs off normalized requirements |

## 5.10 Evidence to Add After Execution

| Evidence | Value |
|---|---|
| Git commit | _Pending_ |
| Clarification/assumption record | _Pending_ |
| Atomic SQL implementation | _Pending_ |
| Concurrent analytics test result | _Pending_ |
| AI alternatives considered | _Pending_ |
| Final human decision | _Pending_ |

---

# 6. Scenario Traceability Matrix

| Requirement | Design Artifact | Planned Code | Validation |
|---|---|---|---|
| Greenfield create | API and DB design | Creation controller/service/allocator | API and integration tests |
| Greenfield redirect | Architecture and DB design | Redirect controller/service | `302`, `404`, persistence tests |
| Brownfield custom alias | Functional/API/class design | DTO, policy, service branch, error mapping | Conflict, reserved, concurrency, regression |
| Ambiguous analytics | NFR/API/DB design | Atomic update, analytics service/API | Zero state, increments, concurrency, privacy |

---

# 7. AI Traceability Template

This table will be updated during implementation.

| Scenario | Task | AI Output | Engineer Action | Status | Rationale / Quality Gate |
|---|---|---|---|---|---|
| Greenfield | _Pending_ | _Pending_ | Generated / Edited / Rejected | _Pending_ | _Pending_ |
| Brownfield | _Pending_ | _Pending_ | Generated / Edited / Rejected | _Pending_ | _Pending_ |
| Ambiguous | _Pending_ | _Pending_ | Generated / Edited / Rejected | _Pending_ | _Pending_ |

### Status definitions

- **Generated:** accepted substantially as drafted after review.
- **Edited:** useful draft, materially changed by the engineer.
- **Rejected:** suggestion not used because it violated requirements, quality, security, or maintainability expectations.

---

# 8. Human Approval Gates

The engineer must explicitly approve:

1. normalized requirements;
2. public API changes;
3. database migrations;
4. concurrency and transaction behavior;
5. security-sensitive validation;
6. error-contract changes;
7. test coverage;
8. final Git commit and public-repository contents.

AI may assist inside these tasks but cannot approve them.

---

# 9. Definition of Done

The three-scenario deliverable is complete only when:

- all three scenarios have actual code or configuration evidence;
- task decomposition matches the final implementation;
- validation results are recorded;
- AI-assisted work is classified as generated, edited, or rejected;
- relevant commit hashes are added;
- limitations and deviations are explicit;
- the engineer can explain every selected trade-off.
