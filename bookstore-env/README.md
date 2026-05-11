# bookstore-env

Docker Compose environment that runs the full bookstore platform locally with a single command. Designed to mirror a production-like setup: health-checked dependencies, named volumes for persistence, and environment-variable-driven configuration.

## Stack

| Service | Image | Port | Role |
|---|---|---|---|
| `mysql` | mysql:8.0 | 3306 | Primary datastore |
| `redis` | redis:7-alpine | 6379 | JWT session store (allowlist pattern) |
| `bookstore-api` | built from source | 8080 | Spring Boot REST API |

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Compose v2)

## Quick start

```bash
# From the repo root
cd bookstore-env
docker compose up
```

The first run builds the `bookstore-api` image (multi-stage Maven build). Subsequent runs reuse the cached image unless source changes.

Once all services are healthy, the API is available at:
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **Base URL:** http://localhost:8080

**Default credentials seeded on startup:**

| Username | Password | Role |
|---|---|---|
| `admin` | `Admin123!` | ADMINISTRATOR |

A `DataSeeder` also seeds 5 sample books on first run. Seeding is idempotent — restarting the stack never duplicates data.

## Service startup order

`bookstore-api` waits for both `mysql` and `redis` to pass their healthchecks before starting:

```
mysql  (healthcheck: mysqladmin ping every 10s, up to 10 retries)  ──┐
                                                                       ├──► bookstore-api
redis  (healthcheck: redis-cli ping every 5s, up to 5 retries)    ──┘
```

This prevents `HikariPool` connection errors on slow machines where MySQL takes 20–30 seconds to initialize.

## Postman collection

A ready-to-import Postman collection is available at [`/postman/bookstore-api.postman_collection.json`](../postman/bookstore-api.postman_collection.json).

It includes pre-configured variables (`{{local_http_base_url}}`, `{{token}}`), pre-request scripts, and test assertions. Running `Auth > Login` automatically saves the JWT for all subsequent requests.

## Persistent storage

MySQL data is stored in a named Docker volume (`mysql_data`). Stopping and restarting the stack preserves all data.

To start completely fresh:

```bash
docker compose down -v
```

## Debugging

A separate override file is provided for attaching a remote debugger (e.g. IntelliJ Remote JVM Debug on port 5005):

```bash
docker compose -f docker-compose.yml -f docker-compose.debug.yml up
```

The debug port is intentionally excluded from the base compose file to avoid exposing it in production-like environments.