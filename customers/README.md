# Customers Microservice

Reactive customer management microservice built with Spring Boot and WebFlux.

## Overview

This module exposes CRUD endpoints to manage customers by identification number.
It uses:
- PostgreSQL for persistence
- Redis for short-lived customer cache
- Kafka for publishing customer lifecycle events

The service returns a consistent API envelope using `GenericResponse<T>`.

## Architecture

The microservice follows a layered structure:

- `application/rest` - HTTP controllers (`CustomerController`)
- `application/service` - business logic (`ICustomerService`, `CustomerServiceImpl`)
- `application/exception` - global exception mapping and domain/database exceptions
- `infrastructure/repository` - Spring Data JPA repository (`ICustomerRepository`)
- `infrastructure/cache` - Redis cache access (`CustomerCacheService`)
- `infrastructure/event/producer` - Kafka publisher (`CustomerEventProducer`)
- `infrastructure/config` - Redis, Kafka, and OpenAPI config
- `domain` - shared response type (`GenericResponse`)

### Request Flow

1. Request enters `CustomerController` (`/customers`).
2. Controller delegates to `CustomerServiceImpl`.
3. Service executes blocking JPA work in `Schedulers.boundedElastic()`.
4. Service reads/writes cache in Redis where applicable.
5. Service publishes Kafka events for create/delete operations.
6. Controller returns `GenericResponse<T>` wrapped in `ResponseEntity`.

## Tech Stack

From `customers/build.gradle.kts`:

- Java 21
- Spring Boot 4.0.4
- Spring WebFlux
- Spring Data JPA
- Spring Data Redis Reactive
- Spring Kafka
- Flyway (schema migration)
- PostgreSQL driver
- springdoc OpenAPI (UI + spec)
- Lombok
- JUnit 5, Reactor Test, Mockito, Spring Kafka Test

## Data Model

Main entities:

- `PersonEntity` (`person` table)
  - unique `identification_number`
- `CustomerEntity` (`customer` table)
  - one-to-one with `PersonEntity` via `person_id`

Initial schema and seed data are defined in:
- `customers/src/main/resources/db/migration/V1.0.1__BaseDatos.sql`

## API Endpoints

Controller base mapping is `/customers`.

If `SPRING_WEBFLUX_BASE-PATH=/api/v1/` is set (as in Docker Compose), effective routes become `/api/v1/customers`.

- `POST /customers` - create customer
- `GET /customers/{identificationNumber}` - get one customer
- `GET /customers?state={bool}&page={n}&size={n}&sorting={field}` - paginated query
- `PUT /customers/{identificationNumber}` - update customer (identification number is immutable)
- `DELETE /customers/{identificationNumber}` - delete customer

OpenAPI spec file:
- `customers/src/main/resources/static/openapi/customers-api.yml`

Swagger UI is configured by default to load that YAML file.

## Response Contract

All success and error responses use:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "...",
  "path": "/customers/...",
  "data": {}
}
```

`data` is `null` for delete success and most error cases.

## Error Handling

`GlobalExceptionHandler` maps exceptions to HTTP status codes:

- `NotFoundException` -> `404 Not Found`
- `DuplicateResourceException` -> `409 Conflict`
- `IllegalArgumentException` -> `400 Bad Request`
- Any other `Exception` -> `500 Internal Server Error`

### Service-Level Error Strategy

`CustomerServiceImpl` also applies reactive error mapping:

- DB unique constraint errors -> `DuplicateResourceException`
- Null constraint violations -> `IllegalArgumentsException`
- Missing records -> `NotFoundException`
- DB access issues in pagination -> wrapped `DataAccessException`

### Resilience Behavior (Non-blocking side effects)

Cache and event publication failures are logged and swallowed (`onErrorResume`), so core CRUD operations can still succeed even if Redis or Kafka is temporarily unavailable.

## Caching

`CustomerCacheService` stores `GetCustomerDTO` values in Redis using key:

- `customer:by-identification-number:{identificationNumber}`

TTL is 10 minutes.

Current cache behavior:
- `GET by id`: read-through cache
- `POST`: write cache
- `PUT`: invalidate cache
- `DELETE`: remove cache

## Event Publishing

`CustomerEventProducer` publishes:

- topic `customer-created` on create
- topic `customer-deleted` on delete

Payload type: `CustomerEvent(identificationNumber, name, state)`.

> Note: root `scripts/create-topics.sh` creates `customer-events`, while service publishes to `customer-created` and `customer-deleted`. Align topic creation/configuration before production deployment.

## Configuration

Default local config is in:
- `customers/src/main/resources/application.properties`

Important properties:

- Server: `server.port=8081`
- PostgreSQL: `spring.datasource.*`
- Flyway: `spring.flyway.*`
- Kafka producer: `spring.kafka.producer.*`
- Redis: `spring.data.redis.*`
- OpenAPI UI YAML URL: `springdoc.swagger-ui.url=/openapi/customers-api.yml`

Dockerized runtime config is in root `docker-compose.yml` (overrides hostnames/ports and sets base path `/api/v1/`).

## Tests Implemented

### Unit Tests (`CustomerServiceTest`)

Covered scenarios:

- create customer success
- create customer duplicate identification (conflict path)
- get customer from cache
- get customer not found
- list customers success
- list customers DB failure
- update customer success
- update customer not found
- delete customer success
- delete customer not found

The tests validate happy paths, exception mapping behavior, cache/event interactions, and reactive completion/error semantics using `StepVerifier`.

### Smoke Test (`CustomersApplicationTests`)

- Spring context loads successfully (`@SpringBootTest`).

## How to Run

### Prerequisites

- Java 21
- Docker + Docker Compose (recommended for dependencies)
- Optional local services if running without Docker: PostgreSQL, Redis, Kafka

### Run dependencies and services with Docker Compose (from repository root)

```bash
docker compose -f docker-compose.yml up -d
```

### Build and run only `customers` locally

```bash
cd customers
gradlew.bat clean build
gradlew.bat bootRun
```

### Run tests

```bash
cd customers
gradlew.bat test
```

## Notes and Known Gaps

- OpenAPI schema uses `status` while runtime record uses `statusCode`.
- Topic names in startup script and service publisher differ.
- The module combines reactive APIs with blocking JPA; blocking calls are explicitly isolated in `boundedElastic` to avoid blocking event-loop threads.

