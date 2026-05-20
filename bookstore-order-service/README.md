# bookstore-order-service

Order lifecycle microservice. Handles order creation, retrieval, listing, and cancellation. Validates book existence and price via HTTP call to [bookstore-api](../bookstore-api) before persisting any order.

**Port:** `8081`

---

## What this service does

| Operation | Rule |
|---|---|
| Create order | Validates book exists (Feign → bookstore-api); snapshots price at purchase time |
| Get order | Returns order — ownership enforced (only the order's owner can read it) |
| List orders | Returns all orders for the authenticated user |
| Cancel order | Transitions `CREATED` → `CANCELLED` — ownership enforced; already-cancelled orders rejected |

---

## Architecture

Hexagonal (Ports & Adapters) with a strict dependency rule: outer rings depend on inner; inner rings have zero Spring or infrastructure imports.

```
┌───────────────────────────────────────────────────────────┐
│                   ADAPTERS  (outer ring)                  │
│  OrderController │ OrderJpaAdapter │ BookValidationAdapter │
│  JwtAuthFilter   │ JwtPropagationInterceptor              │
├───────────────────────────────────────────────────────────┤
│                 APPLICATION  (middle ring)                │
│  OrderService  implements ►  CreateOrderUseCase           │
│                              GetOrderUseCase              │
│                              ListOrdersUseCase            │
│                              CancelOrderUseCase           │
│  Output ports: OrderRepositoryPort · BookValidationPort   │
├───────────────────────────────────────────────────────────┤
│                   DOMAIN  (inner ring)                    │
│  Order · OrderItem · OrderStatus                          │
│  InvalidOrderException · DomainException                  │
└───────────────────────────────────────────────────────────┘
```

### Package map

| Package | Role |
|---|---|
| `domain.model` | Pure Java: `Order`, `OrderItem`, `OrderStatus`, domain exceptions |
| `application.order.port.in` | Input ports (use-case interfaces) + `CreateOrderCommand` |
| `application.order.port.out` | Output ports: `OrderRepositoryPort`, `BookValidationPort` |
| `application.order.service` | `OrderService` — implements all four use cases |
| `adapters.in.rest` | `OrderController`, DTOs, `OrderRestMapper`, `GlobalExceptionHandler` |
| `adapters.out.persistence.jpa` | JPA entities (`OrderJpaEntity`, `OrderItemJpaEntity`), mapper, adapter |
| `adapters.out.catalog.feign` | `BookFeignInterface`, `BookValidationAdapter`, `JwtPropagationInterceptor` |
| `adapters.security` | `JwtValidator` (RS256 verify-only), `JwtAuthFilter`, `SecurityConfig` |

---

## Security model

This service **validates** JWTs but never **issues** them. Token issuance belongs exclusively to `bookstore-api`.

- **Incoming requests:** `JwtAuthFilter` validates the RS256 signature using the public key at `classpath:keys/public.pem` (or `$JWT_PUBLIC_KEY_PATH`).
- **No Redis:** token revocation is not checked here. A revoked token remains valid until expiry — this is a documented trade-off. Adding Redis would couple the services.
- **Outgoing Feign calls:** `JwtPropagationInterceptor` forwards the caller's Bearer token and `X-Correlation-Id` header to `bookstore-api` so the downstream service sees the same authenticated identity.

---

## Observability

Every request is assigned a correlation ID via `CorrelationIdFilter`:

1. Reads `X-Correlation-Id` from the incoming request header (client-provided).
2. Generates a new UUID if no header is present.
3. Places it in SLF4J MDC under the key `correlationId` — appears in every log line for the duration of the request.
4. Echoes it as a response header so callers can correlate traces across service boundaries.
5. Forwards it to `bookstore-api` via Feign.

Log format: `HH:mm:ss.SSS [thread] [correlationId] LEVEL logger - message`

---

## Inter-service communication

```
Client ──► bookstore-order-service ──► bookstore-api
              Feign + Bearer JWT + X-Correlation-Id
```

`BookFeignInterface` calls `GET /api/books/{bookId}` on `bookstore-api`. The base URL is configurable via `$BOOKSTORE_API_URL` (default: `http://localhost:8080`). Timeouts: `connect=3s`, `read=5s`.

---

## Data model

```
orders
  order_id     BINARY(16) PK       ← UUIDv7 (time-ordered, locally generated)
  user_id      VARCHAR(255)
  order_status VARCHAR(50)
  created_at   DATETIME

order_items
  id           BINARY(16) PK
  order_id     BINARY(16) FK → orders
  book_id      BINARY(16)          ← snapshot at purchase time
  quantity     INT
  unit_price   DECIMAL(10,2)       ← snapshot at purchase time
```

Prices and book IDs are **snapshotted** at order creation so future catalog changes do not alter historical orders.

---

## API

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/orders` | Bearer | Create an order |
| `GET` | `/api/orders` | Bearer | List orders for the authenticated user |
| `GET` | `/api/orders/{orderId}` | Bearer | Get order by ID |
| `DELETE` | `/api/orders/{orderId}` | Bearer | Cancel an order |

Full interactive docs: **http://localhost:8081/swagger-ui/index.html**

The Swagger UI dropdown lets you switch between this service's spec and `bookstore-api`'s spec without leaving the page.

---

## Running

### With Docker (recommended)

```bash
cd bookstore-env
docker compose up -d
```

### Locally (requires bookstore-api on port 8080)

```bash
mvn spring-boot:run -pl bookstore-order-service
```

**Environment variables:**

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8081` | HTTP port |
| `DB_USERNAME` | `root` | MySQL username |
| `DB_PASSWORD` | `password` | MySQL password |
| `BOOKSTORE_API_URL` | `http://localhost:8080` | bookstore-api base URL for Feign |
| `JWT_PUBLIC_KEY_PATH` | `classpath:keys/public.pem` | RSA public key for token validation |
| `CORS_ALLOWED_ORIGINS` | `*` | Restrict to specific origins in production |
| `CATALOG_API_DOCS_URL` | `http://localhost:8080/v3/api-docs` | Bookstore API spec URL shown in Swagger UI dropdown |

---

## Tests

```bash
# All tests (unit + integration)
mvn test -pl bookstore-order-service

# Single class
mvn test -Dtest=OrderServiceTest -pl bookstore-order-service
```

| Suite | Type | Tests |
|---|---|---|
| `OrderControllerIT` | Integration (`@SpringBootTest` + H2) | 9 |
| `OrderRestMapperTest` | Unit | 6 |
| `JwtValidatorTest` | Unit | 11 |
| `OrderServiceTest` | Unit | 10 |
| `OrderItemTest` | Unit | 10 |
| `OrderTest` | Unit | 20 |
| **Total** | | **66** |

Integration tests use H2 in MySQL mode. `BookValidationPort` is replaced with `@MockitoBean` — no running `bookstore-api` required.
