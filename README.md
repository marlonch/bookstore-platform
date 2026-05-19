# Bookstore API
> RESTful API for bookstore catalog management with role-based access control.
> Built with **Java 17 · Spring Boot 4 · Hexagonal Architecture · JWT · Redis**.

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

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Persistence | MySQL 8 + Spring Data JPA (H2 in tests) |
| Token store | Redis 7 (JWT allowlist with TTL) |
| Security | Spring Security 6 + JJWT 0.12.5 |
| API docs | SpringDoc OpenAPI 2 (Swagger UI) |
| Build | Maven (multi-module) |
| Tests | JUnit 5 + Mockito + TestRestTemplate |
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

Once all services are healthy, the API is available at:
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **Base URL:** http://localhost:8080

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
# 1. Create the database
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS bookstore_db;"

# 2. Run
mvn spring-boot:run -pl bookstore-api
```

**Environment variables (all have defaults for local dev):**

| Variable | Default | Description |
|---|---|---|
| `DB_USERNAME` | `root` | MySQL username |
| `DB_PASSWORD` | `password` | MySQL password |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `JWT_PRIVATE_KEY_PATH` | `classpath:keys/private.pem` | RSA private key — **mount a real key in production** |
| `JWT_PUBLIC_KEY_PATH` | `classpath:keys/public.pem` | RSA public key — safe to distribute |
| `JWT_EXPIRATION_HOURS` | `24` | Token TTL in hours |

---

## Running Tests

```bash
# All tests (unit + integration)
mvn test -pl bookstore-api

# Single class
mvn test -Dtest=BookControllerIT -pl bookstore-api

# Single method
mvn test -Dtest=BookControllerIT#createBook_asAdmin_returns201WithBody -pl bookstore-api
```

Tests use H2 (in-memory) instead of MySQL and a `@MockitoBean` for Redis — no external services required.

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

## Trying the API

Import [`postman/bookstore-api.postman_collection.json`](postman/bookstore-api.postman_collection.json) into Postman.
Run **Auth > Login** first — the JWT is saved automatically and applied to all subsequent requests.

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

---

## Project Structure

```
bookstore-platform/
├── bookstore-api/               ← Spring Boot application
│   └── src/main/java/com/hub/
│       ├── domain/              ← Pure Java: models, exceptions (no Spring)
│       │   ├── catalog/
│       │   │   ├── book/        ← Book aggregate, BookId, ISBN, BookStatus
│       │   │   └── stock/       ← Stock aggregate
│       │   ├── identity/        ← User, UserId, Role, UserStatus
│       │   └── auth/            ← TokenMetadata, TokenStatus
│       ├── application/         ← Use-case interfaces + services (no Spring)
│       │   ├── catalog/
│       │   │   ├── book/        ← book use-case ports + commands
│       │   │   └── stock/       ← stock use-case ports + commands
│       │   ├── identity/
│       │   ├── auth/
│       │   └── shared/          ← TransactionPort (cross-cutting output port)
│       └── adapters/            ← Spring-aware implementations
│           ├── in/rest/         ← Controllers, DTOs, GlobalExceptionHandler
│           ├── out/persistence/ ← JPA entities, repositories, mappers
│           ├── out/session/     ← Redis token store
│           ├── security/        ← JwtAuthFilter, SecurityConfig
│           └── DataSeeder.java  ← Demo data on startup
├── bookstore-env/               ← Docker Compose environment
│   ├── docker-compose.yml       ← MySQL + Redis + bookstore-api
│   └── docker-compose.debug.yml ← Override: enables remote JVM debug on port 5005
└── postman/
    └── bookstore-api.postman_collection.json
```