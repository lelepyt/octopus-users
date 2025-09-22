# Octopus Users API

A Spring Boot service that aggregates user data from multiple databases and exposes a single REST endpoint `GET /users`.

## Features
- Aggregates users from multiple JDBC data sources
- Declarative configuration of sources and field mappings via `application.yml`
- OpenAPI/Swagger UI documentation
- Optional query filters: `id`, `username`, `name`, `surname`
- Docker Compose for local Postgres databases
- Tests including an integration test using Testcontainers

## Prerequisites
- Java 17+
- Maven 3.9+
- Docker & Docker Compose (for local databases and Testcontainers image pulls)

## Quick start

### 1) Start databases with Docker Compose
```bash
docker compose up -d
```
This starts two Postgres instances:
- `postgres1` on port 5432 with table `users`
- `postgres2` on port 5433 with table `user_table`

Seed data are created by `docker/init-db1.sql` and `docker/init-db2.sql`.

### 2) Run the application
```bash
./mvnw spring-boot:run
```
The app will start on `http://localhost:8080`.

## Configuration
Data sources are declared in `src/main/resources/application.yml` under the `data-sources` prefix. Example:
```yaml
data-sources:
  dataSources:
    - name: data-base-1
      strategy: postgres
      url: jdbc:postgresql://localhost:5432/db1?useSSL=false
      table: users
      user: testuser
      password: testpass
      mapping:
        id: user_id
        username: login
        name: first_name
        surname: last_name

    - name: data-base-2
      strategy: postgres
      url: jdbc:postgresql://localhost:5433/db2?useSSL=false
      table: user_table
      user: testuser
      password: testpass
      mapping:
        id: ldap_login
        username: ldap_login
        name: name
        surname: surname
```
- `mapping` maps the canonical fields (`id`, `username`, `name`, `surname`) to the actual column names per source.
- Currently, Postgres is supported. The `strategy` field is reserved for potential multi-DB support.

## API
### Get all users
`GET /users`

Query parameters (all optional):
- `id`: filter by user id
- `username`: filter by username/login
- `name`: filter by first name
- `surname`: filter by last name

Example request:
```bash
curl "http://localhost:8080/users?name=User&surname=Userenko"
```

Example successful response (`200 OK`):
```json
[
  {
    "id": "example-user-id-1",
    "username": "user-1",
    "name": "User",
    "surname": "Userenko"
  },
  {
    "id": "example-user-id-2",
    "username": "user-2",
    "name": "Testuser",
    "surname": "Testov"
  }
]
```

### OpenAPI / Swagger UI
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI file: `openapi.yaml`

Note: The OpenAPI `GET /users` endpoint may be modeled with explicit optional query params. If you see a single `filters` object param, it should be treated as optional, or replaced with explicit params.

## Building & Testing
Run all tests:
```bash
./mvnw clean test
```

- Unit tests cover aggregation behavior.
- Integration test (`JdbcUserSourceClientIT`) uses Testcontainers to start an ephemeral Postgres.

If Docker is not running, Testcontainers tests will be skipped/fail depending on your environment.

## Troubleshooting
- Port conflicts: Ensure local ports `5432` and `5433` are free before starting Docker Compose.
- Database connectivity: Verify credentials/URLs in `application.yml` match the Compose services.
- Swagger UI not loading: Make sure the app is running and check `http://localhost:8080/swagger-ui.html`.
- Testcontainers errors: Ensure Docker is running and that your user has permission to access the Docker daemon.

## Project structure
- `openapi.yaml` — API contract
- `src/main/java/...` — Spring Boot application code
- `src/main/resources/application.yml` — configuration for data sources
- `docker/` — SQL init scripts for local databases
- `docker-compose.yml` — local Postgres services
- `src/test/java/...` — unit and integration tests
