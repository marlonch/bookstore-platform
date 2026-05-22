# Bookstore Platform
> Multi-service platform for bookstore catalog and order management.
> Built with **Java 17 · Spring Boot 4 · Hexagonal Architecture · JWT RS256 · Redis**.

[![CI](https://github.com/marlonch/bookstore-api/actions/workflows/ci.yml/badge.svg)](https://github.com/marlonch/bookstore-api/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=marlonch_bookstore-platform&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=marlonch_bookstore-platform)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=marlonch_bookstore-platform&metric=coverage)](https://sonarcloud.io/summary/new_code?id=marlonch_bookstore-platform)

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal-orange)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## Architecture

Three concentric rings with a strict dependency rule: outer rings depend on inner; inner rings are pure Java with no Spring or infrastructure imports.

```
┌──────────────────────────────────────────────────┐
│           ADAPTERS  (outer ring)                 │
│  REST Controllers │ JPA (MySQL) │ Redis │ JWT    │
├──────────────────────────────────────────────────┤
│           APPLICATION  (middle ring)             │
│    AuthService  │  UserService  │  BookService   │
│    Input ports (use-case interfaces + commands)  │
│    Output ports (repository / token contracts)   │
├──────────────────────────────────────────────────┤
│           DOMAIN  (inner ring)                   │
│  Book  │  Stock  │  User  │  TokenMetadata  │    │
│  BookId · UserId · ISBN (value objects)          │
│  BookStatus · Exceptions                         │
└──────────────────────────────────────────────────┘
```

The domain and application layers have zero Spring dependencies — they are tested with plain JUnit + Mockito, with no Spring context startup cost. Swapping MySQL for PostgreSQL or Redis for another store requires changes only in the adapter layer.

---

## Services

| Service | Port | Role |
|---|---|---|
| `bookstore-api` | 8080 | Catalog management, authentication, JWT issuance (RS256 private key) |
| `bookstore-order-service` | 8081 | Order lifecycle, JWT validation (RS256 public key only), Feign → bookstore-api |

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Persistence | MySQL 8 + Spring Data JPA (H2 in tests) |
| Token store | Redis 7 (JWT allowlist — bookstore-api only) |
| Security | Spring Security 6 + JJWT 0.12.5 (RS256) |
| Inter-service | Spring Cloud OpenFeign + JWT propagation |
| API docs | SpringDoc OpenAPI 2 (Swagger UI) |
| Build | Maven (multi-module) |
| Tests | JUnit 5 + Mockito + WebTestClient |
| Container | Docker + Docker Compose |

---

## Quick Start

**Prerequisites:** [Docker Desktop](https://www.docker.com/products/docker-desktop/)

```bash
git clone https://github.com/marlonch/bookstore-platform
cd bookstore-platform/bookstore-env
docker compose up -d
```

The first run builds the `bookstore-api` image (multi-stage Maven build). Subsequent runs reuse the cached image.

Once all services are healthy:
- **Swagger UI (bookstore-api):** http://localhost:8080/swagger-ui/index.html
- **Swagger UI (order-service):** http://localhost:8081/swagger-ui/index.html
- **bookstore-api base URL:** http://localhost:8080
- **bookstore-order-service base URL:** http://localhost:8081

Both Swagger UIs include a dropdown to switch between the two services' specs — you can browse the full platform API from a single tab.

**Credentials seeded on startup:**

| Username | Password | Role |
|---|---|---|
| `admin` | `Admin123!` | ADMINISTRATOR |
| `user` | `User123!` | NON_ADMINISTRATOR |

5 sample books are also seeded. Seeding is idempotent — restarting the stack never duplicates data.

**Remote debugging:**

```bash
docker compose -f docker-compose.yml -f docker-compose.debug.yml up -d
```

Attaches a JDWP agent to bookstore-api with `suspend=n` (service starts immediately without waiting for a debugger). Connect your IDE to `localhost:5005`.

---

## Local Development (without Docker)

**Prerequisites:** Java 17, Maven, MySQL 8 on `localhost:3306`, Redis on `localhost:6379`.

```bash
# 1. Create the databases
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS bookstore_db; CREATE DATABASE IF NOT EXISTS orders_db;"

# 2. Run bookstore-api (port 8080)
mvn spring-boot:run -pl bookstore-api

# 3. Run bookstore-order-service (port 8081) — in a separate terminal
mvn spring-boot:run -pl bookstore-order-service
```

**Environment variables — bookstore-api:**

| Variable | Default | Description |
|---|---|---|
| `DB_USERNAME` | `root` | MySQL username |
| `DB_PASSWORD` | `password` | MySQL password |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `JWT_PRIVATE_KEY_PATH` | `classpath:keys/private.pem` | RSA private key — **mount a real key in production** |
| `JWT_PUBLIC_KEY_PATH` | `classpath:keys/public.pem` | RSA public key — safe to distribute |
| `JWT_EXPIRATION_HOURS` | `24` | Token TTL in hours |

**Environment variables — bookstore-order-service:**

| Variable | Default | Description |
|---|---|---|
| `DB_USERNAME` | `root` | MySQL username |
| `DB_PASSWORD` | `password` | MySQL password |
| `BOOKSTORE_API_URL` | `http://localhost:8080` | bookstore-api base URL (Feign target) |
| `JWT_PUBLIC_KEY_PATH` | `classpath:keys/public.pem` | RSA public key for token validation |

---

## CI/CD

Every push and pull request to `main` triggers a GitHub Actions pipeline:

| Step | Command | Purpose |
|---|---|---|
| Build, Test & Coverage | `mvn -B verify -Pcoverage` | Compiles, runs all tests, generates JaCoCo coverage report |
| OWASP Dependency Check | `mvn -B dependency-check:aggregate -Powasp` | Scans dependencies for known CVEs (CVSS ≥ 7 fails the build) |
| SonarCloud Scan | `mvn -B sonar:sonar` | Uploads coverage and static analysis results to SonarCloud |

The OWASP HTML report is uploaded as a GitHub Actions artifact on every run.

---

## Running Tests

```bash
# All tests for bookstore-api (unit + integration)
mvn test -pl bookstore-api

# All tests for bookstore-order-service (unit + integration)
mvn test -pl bookstore-order-service

# Single class
mvn test -Dtest=OrderControllerIT -pl bookstore-order-service

# Single method
mvn test -Dtest=OrderControllerIT#createOrder_withValidToken_returns201AndOrder -pl bookstore-order-service

# Generate JaCoCo coverage report (both modules)
mvn verify -Pcoverage

# Scan dependencies for CVEs
mvn dependency-check:aggregate -Powasp
```

Both services use H2 (in-memory) for integration tests and `@MockitoBean` for external dependencies — no Docker required to run tests.

---

## API Reference

### Authentication

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/login` | Public | Obtain JWT |
| `POST` | `/api/auth/logout` | Bearer | Revoke current token in Redis |

### Books

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/books` | Bearer | List all books |
| `GET` | `/api/books/{id}` | Bearer | Get book by ID |
| `POST` | `/api/books` | ADMIN | Create a book |
| `PUT` | `/api/books/{id}` | ADMIN | Update a book |
| `DELETE` | `/api/books/{id}` | ADMIN | Delete a book |
| `PUT` | `/api/books/{bookId}/assign/{userId}` | ADMIN | Assign book to user |

### Users

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/users` | ADMIN | List all users |
| `POST` | `/api/users` | ADMIN | Create a user |
| `GET` | `/api/users/{id}` | ADMIN | Get user by ID |
| `PUT` | `/api/users/{id}` | ADMIN | Update a user |
| `DELETE` | `/api/users/{id}` | ADMIN | Delete a user |
| `GET` | `/api/users/{id}/books` | Bearer | List books assigned to user |

### Orders (bookstore-order-service — port 8081)

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/orders` | Bearer | Create an order (validates book via Feign → bookstore-api) |
| `GET` | `/api/orders` | Bearer | List orders for the authenticated user |
| `GET` | `/api/orders/{orderId}` | Bearer | Get order by ID |
| `DELETE` | `/api/orders/{orderId}` | Bearer | Cancel an order |

## Trying the API

Import [`postman/bookstore-api.postman_collection.json`](postman/bookstore-api.postman_collection.json) into Postman.
1. Run **Auth > Login** — JWT saved automatically to `{{token}}` and applied to all requests.
2. The **Orders** folder targets `{{order_service_base_url}}` (defaults to `http://localhost:8081`).
3. **Create Order** uses `{{book_id}}` (saved after Create Book) and saves `{{order_id}}` on success.

Full interactive docs: **http://localhost:8080/swagger-ui/index.html**

---

## Security Model

Authentication is JWT-based (**RS256**) with a Redis-backed **token allowlist**. Every login creates a UUID `tokenId`, stores it in Redis (`ACTIVE`, 24h TTL), and embeds it as the JWT `jti` claim. On every authenticated request, `JwtAuthFilter` validates the RSA signature and then checks Redis — a missing or `REVOKED` entry rejects the request regardless of a valid signature.

This means logout is immediate and effective across all instances: revoking a token on node A is instantly visible to node B because both share Redis.

Authorization uses Spring Security's `@PreAuthorize` at the controller level:
- `isAuthenticated()` — any valid session
- `hasAuthority('ADMINISTRATOR')` — admin-only operations

---

## Design Decisions

**Why Hexagonal Architecture?**
The domain and application services are pure Java — no Spring, no JPA, no Redis imports. Unit tests run in milliseconds with no Spring context startup cost. Swapping any infrastructure component requires changes only in the adapter layer.

**Why Redis for token revocation?**
JWTs are stateless by design, but logout requires stateful revocation. Redis gives O(1) lookup per request, and a TTL matching the JWT expiration ensures keys are cleaned up automatically without a background job.

**Why RS256 instead of HS256 for JWT signing?**
HS256 uses a single shared secret — any service that needs to verify tokens must hold the secret, becoming an additional exposure point. RS256 signs with a private key (kept only in bookstore-api) and verifies with a public key that is safe to distribute. Other services that validate tokens locally only need the public key.

**Why store `tokenId` (UUID) as the JWT `jti` claim instead of the full JWT?**
The `jti` is a small, opaque UUID stored as the Redis key. Avoids storing large JWT strings and aligns with RFC 7519's intent for `jti` as a unique token identifier.

**Why `BookId` and `UserId` instead of plain `Long`?**
Typed value objects wrapping UUID v7 prevent entire classes of bugs where IDs of different entities are mixed up. `findById(BookId)` cannot accidentally accept a `UserId` — the compiler rejects it. UUID v7 is time-ordered, which keeps index locality in MySQL while being globally unique without a centralized sequence.

**Why `Optional<UserId>` for `Book.ownerId`?**
A bare `null` field is invisible at the call site — callers can forget to null-check. `Optional<UserId>` makes the absence of an owner an explicit, compile-time-visible fact. The typed wrapper also means passing a `BookId` where an owner ID is expected is a compile error, not a runtime surprise.

**Why H2 in integration tests instead of Testcontainers?**
H2's `MODE=MySQL` is fast and requires no Docker daemon in CI. The trade-off is reduced production parity. A natural next step would be replacing H2 with Testcontainers for true MySQL fidelity.

**Why a Correlation ID on every request?**
A single user action can touch both services. Without a shared identifier, logs from `bookstore-api` and `bookstore-order-service` are unrelatable — you cannot tell which log lines from service A belong to the same logical request as which lines from service B. `CorrelationIdFilter` runs on every inbound request (highest filter precedence), assigns a UUID if the client did not supply `X-Correlation-Id`, places it in SLF4J MDC, and echoes it in the response header. `JwtPropagationInterceptor` forwards the same ID on all Feign calls so the trace is consistent end to end.

---

## Project Structure

```
bookstore-platform/
├── bookstore-api/               ← Catalog service (port 8080)
│   └── src/main/java/com/hub/
│       ├── domain/              ← Pure Java: models, exceptions (no Spring)
│       │   ├── catalog/         ← Book aggregate, BookId, ISBN, BookStatus, Stock
│       │   ├── identity/        ← User, UserId, Role, UserStatus
│       │   └── auth/            ← TokenMetadata, TokenStatus
│       ├── application/         ← Use-case interfaces + services (no Spring)
│       └── adapters/            ← Spring-aware implementations
│           ├── in/rest/         ← Controllers, DTOs, GlobalExceptionHandler
│           ├── out/persistence/ ← JPA entities, repositories, mappers
│           ├── out/session/     ← Redis token store
│           └── security/        ← JwtProvider (RS256 sign), JwtAuthFilter
├── bookstore-order-service/     ← Order service (port 8081)
│   └── src/main/java/com/hub/bookstoreorderservice/
│       ├── domain/              ← Order, OrderItem, OrderStatus (pure Java)
│       ├── application/order/   ← Use-case ports + OrderService
│       └── adapters/
│           ├── in/rest/         ← OrderController, DTOs, GlobalExceptionHandler
│           ├── out/catalog/     ← BookFeignInterface, JwtPropagationInterceptor
│           ├── out/persistence/ ← OrderJpaEntity, OrderJpaAdapter
│           └── security/        ← JwtValidator (RS256 verify), JwtAuthFilter
├── bookstore-env/               ← Docker Compose environment
│   ├── docker-compose.yml       ← MySQL (bookstore_db + orders_db) + Redis + both services
│   ├── docker-compose.debug.yml ← Override: enables remote JVM debug on port 5005
│   └── init-db.sql              ← Creates orders_db on first MySQL startup
└── postman/
    └── bookstore-api.postman_collection.json  ← Auth, Books, Users, Orders
```