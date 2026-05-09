# bookstore-env

> **Status:** Planned — not yet implemented

Docker Compose environment for running the full bookstore platform locally.

## Services
- `mysql` — relational database (port 3306)
- `redis` — JWT token store (port 6379)
- `bookstore-api` — Auth, Users, Books REST API (port 8080)
- `bookstore-order-service` — Order management (planned)

## Usage

```bash
docker compose up
```

The API will be available at `http://localhost:8080/swagger-ui/index.html`.
