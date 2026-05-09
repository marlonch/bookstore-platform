# bookstore-order-service

> **Status:** Planned — not yet implemented

Event-driven order management service for the bookstore platform.

## Responsibilities
- Create and track book orders
- Publish domain events (OrderPlaced, OrderCancelled)
- Follows hexagonal architecture (same pattern as `bookstore-api`)

## Planned Stack
- Java 17 · Spring Boot
- Messaging: Apache Kafka
- Persistence: MySQL
