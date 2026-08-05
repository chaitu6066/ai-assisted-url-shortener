# Non-Functional Requirements

## Project

AI-Assisted Production-Grade URL Shortener

## Purpose

This document defines the quality attributes and operational expectations for the URL Shortener prototype.

The assignment requires a runnable end-to-end prototype that demonstrates production-grade engineering, validation, risk control, modularity, reliability, security, scalability, maintainability, and clear trade-offs. The prototype will implement the most important controls directly while documenting the evolution path for internet-scale usage.

---

## NFR Classification

Each requirement is classified as one of the following:

- **Implemented in prototype** — delivered and validated within the assignment.
- **Designed for future scale** — the design avoids blocking future implementation, but the capability is not fully built.
- **Deferred** — intentionally excluded from the 24-hour implementation and documented as a limitation.

---

## NFR-1 Availability and Reliability

### Requirement

The service shall handle valid requests consistently and return predictable responses for failures.

### Prototype implementation

- Stateless Spring Boot application.
- PostgreSQL-backed persistence.
- Database uniqueness constraint for short codes.
- Graceful handling of invalid, missing, expired, or duplicate short codes.
- Global exception handling with structured JSON responses.
- Transaction boundaries around write operations.
- Health endpoint through Spring Boot Actuator, if time permits.

### Future scale design

- Multiple stateless service instances behind a load balancer.
- PostgreSQL high availability and read replicas.
- Retry, timeout, and circuit-breaker policies for external dependencies.
- Multi-zone or multi-region deployment.

### Initial target

The local prototype should complete all functional flows reliably on a single application instance and a single PostgreSQL database.

---

## NFR-2 Performance

### Requirement

The redirect operation should remain lightweight because it is expected to be the most frequent request.

### Prototype implementation

- Database index and unique constraint on `short_code`.
- Direct lookup by short code.
- Minimal processing during redirect.
- Analytics update performed in the same application flow for simplicity.
- Pagination avoided because the prototype does not expose a bulk listing API.

### Future scale design

- Redis cache for frequently accessed mappings.
- Asynchronous click-event publishing.
- Separate analytics processing pipeline.
- Database connection-pool tuning.
- Read replicas or partitioning when data volume requires it.

### Initial target

No formal internet-scale service-level objective is claimed. For local validation, normal create, redirect, and analytics requests should complete without noticeable delay.

---

## NFR-3 Scalability

### Requirement

The implementation shall remain deployable as multiple stateless instances without storing session or environment-specific state in the application instance.

### Prototype implementation

- Store only the short code, not the complete environment-specific short URL.
- Externalize base URL and database configuration.
- Avoid in-memory storage as the system of record.
- Keep application services stateless.

### Future scale design

- Horizontal scaling behind an API gateway or load balancer.
- Distributed cache.
- Database replication, partitioning, or sharding.
- Asynchronous analytics using a message broker.
- Distributed rate limiting.

### Capacity statement

The prototype shall be concurrency-safe and horizontally scalable at the application layer. It does not claim a specific internet-scale throughput, such as one million requests per second, until that capacity is validated through representative infrastructure and load testing.

The distinction is important:

- **Correctness under concurrency** is implemented.
- **A specific throughput guarantee** requires benchmarking, capacity planning, and production-like infrastructure.

---

## NFR-4 Security

### Requirement

The service shall validate untrusted input and avoid unsafe or unsupported URL schemes.

### Prototype implementation

- Accept only `http` and `https` destination URLs.
- Reject malformed URLs.
- Validate custom aliases using an allow-list pattern.
- Enforce length limits for URLs and aliases.
- Do not expose internal stack traces.
- Use parameterized persistence through Spring Data JPA.
- Avoid storing secrets in source control.
- Externalize database credentials through environment variables or local configuration.
- Document that AI prompts must not contain enterprise secrets, credentials, or proprietary source code.

### Future scale design

- Authentication and authorization for management and analytics APIs.
- Abuse detection and malicious-domain controls.
- Rate limiting.
- Audit logging.
- HTTPS termination.
- Secret management using an enterprise vault.
- Protection against phishing and unsafe destinations.

### Explicit limitation

Public redirect endpoints are intentionally unauthenticated. The prototype does not include a reputation service or malware-domain screening.

---

## NFR-5 Data Integrity and Consistency

### Requirement

Each short code must map to exactly one original URL.

### Prototype implementation

- Unique database constraint on `short_code`.
- Application-level availability check for custom aliases.
- Database constraint remains the final protection against race conditions.
- Collision retry for automatically generated codes.
- Transactional creation flow.
- Click count and last-accessed timestamp updated consistently for the prototype.

### Trade-off

Synchronous analytics updates provide simple and immediately consistent statistics, but they add database writes to the redirect path. At larger scale, analytics should become asynchronous.


### Concurrent short-code creation

Automatic short-code creation shall remain safe when many requests are processed concurrently or when multiple application instances are running.

The prototype design shall use:

- A Base62 code generated from a cryptographically strong random source.
- A sufficiently large code space; a 10-character Base62 code provides approximately 8.4 × 10^17 possible values.
- A unique database constraint on `short_code` as the final source of truth.
- A bounded retry mechanism when an automatically generated code conflicts with an existing row.
- Direct insert handling rather than relying only on a prior `exists` check, because an application-level check alone is vulnerable to race conditions.
- A `409 Conflict` response for duplicate user-provided aliases.
- A controlled service error if automatic allocation fails after the configured retry limit.

This design prevents duplicate mappings from being persisted across threads, processes, and application instances. A synchronized Java block or an in-memory set shall not be used because those approaches do not coordinate across multiple pods.

### Concurrent analytics updates

Click analytics shall use an atomic database update such as `click_count = click_count + 1` rather than a read-modify-write operation in application memory. This prevents lost increments when the same short URL is accessed concurrently.

---

## NFR-6 Maintainability

### Requirement

The code shall be easy to understand, test, review, and extend.

### Prototype implementation

- Clear separation between controller, service, repository, entity, DTO, validation, and exception-handling concerns.
- Constructor-based dependency injection.
- Meaningful naming.
- Small, focused methods.
- Centralized exception handling.
- Externalized configuration.
- API and architectural documentation.
- Decision log and AI-usage traceability.

### Quality expectation

Generated AI output is not accepted blindly. Every generated artifact must be reviewed, edited where necessary, tested, and owned by the engineer.

---

## NFR-7 Testability and Quality

### Requirement

Core business behavior shall be validated through automated tests.

### Prototype implementation

- Unit tests for short-code generation and service logic.
- Controller tests for request validation and HTTP responses.
- Integration tests for persistence and end-to-end flows.
- Tests for duplicate custom aliases.
- Tests for generated-code collision handling.
- Tests for unknown short codes.
- Tests for invalid URL schemes.
- Maven test execution as a quality gate.

### Optional quality gates

- Static analysis.
- Code formatting.
- Test coverage reporting.
- GitHub Actions build.

---

## NFR-8 Observability

### Requirement

The service shall provide sufficient operational information to diagnose failures without logging sensitive information.

### Prototype implementation

- Structured application logs for URL creation, redirect, and failures.
- Appropriate log levels.
- Correlation/request identifier if time permits.
- No credentials or full sensitive payloads in logs.
- Spring Boot Actuator health and metrics endpoints if included.

### Future scale design

- Centralized logging.
- Distributed tracing.
- Metrics dashboards.
- Alerting based on error rate, latency, database health, and redirect failures.

---

## NFR-9 Configuration and Portability

### Requirement

The same codebase shall run across local, test, and production-like environments without code changes.

### Prototype implementation

- Externalized base URL.
- Externalized database URL, username, and password.
- Environment-specific Spring profiles if needed.
- No hard-coded machine paths or hostnames.
- Reproducible Maven build.
- Setup instructions.

### Optional enhancement

Docker Compose can package the application and PostgreSQL for easier evaluator setup. It remains optional unless sufficient time is available.

---

## NFR-10 API Usability

### Requirement

The REST APIs shall be predictable and easy to evaluate.

### Prototype implementation

- Versioned management APIs, such as `/api/v1/urls`.
- Resource-oriented naming.
- Appropriate HTTP status codes.
- Structured error body.
- OpenAPI/Swagger documentation.
- Example requests and responses in the repository.

### Redirect endpoint

The public redirect route remains short, for example `/{shortCode}`, to preserve the core product experience.

---

## NFR-11 Compatibility

### Requirement

The service shall use stable, enterprise-relevant technologies.

### Prototype target

- Java 21 LTS.
- Spring Boot 3.x.
- Maven.
- PostgreSQL.
- JUnit 5.
- OpenAPI/Swagger.

The development machine currently has Java 22. Java 21 will be installed and configured for this project to align with an LTS runtime.

---

## NFR-12 Delivery and Reviewability

### Requirement

An evaluator shall be able to clone, configure, run, test, and understand the project with minimal effort.

### Prototype implementation

- Public GitHub repository.
- Clear README.
- Setup instructions.
- Architecture and API documentation.
- Automated test command.
- Meaningful Git commits.
- Assumptions, limitations, risks, and trade-offs documented.
- AI usage documented as generated, edited, or rejected with rationale.

---

## NFR-13 AI Safety and Engineering Ownership

### Requirement

AI shall accelerate engineering tasks without replacing engineer judgment.

### Controls

- Prompts include intent, constraints, acceptance criteria, and technical context.
- Generated code is reviewed before use.
- Tests and static checks validate output.
- Security-sensitive suggestions receive explicit review.
- Rejected AI suggestions and reasons are recorded.
- The engineer owns correctness, maintainability, and production readiness.
- No confidential data, credentials, or proprietary enterprise content is submitted to unapproved AI tools.

---

## Prototype Scope Summary

### Implement now

- Stateless Spring Boot service.
- PostgreSQL persistence.
- URL and alias validation.
- Database constraints and indexes.
- Collision handling.
- Structured exception handling.
- Logging.
- Unit and integration tests.
- Swagger/OpenAPI.
- Externalized configuration.
- Documentation and AI traceability.

### Design for later

- Horizontal scaling.
- Redis caching.
- Asynchronous analytics.
- Database replication or partitioning.
- Rate limiting.
- Authentication and premium-user controls.
- Advanced observability.
- Multi-region deployment.

### Deferred limitations

- No unverified claim of one-million-request-per-second capacity.
- No distributed deployment in the submitted prototype, although the service is designed to remain stateless and safe across multiple instances.
- No multi-region failover.
- No malicious-domain reputation check.
- No complete identity or subscription model.
- No full load or penetration testing within the assignment timeframe.

---

## Production-Oriented Design Principle

The prototype implements the simplest architecture that correctly satisfies the current requirements, while avoiding decisions that would block future scale. Production readiness is demonstrated through clean design, validation, testing, configuration, documentation, and explicit risk management—not by adding unnecessary infrastructure to a time-boxed assignment.
