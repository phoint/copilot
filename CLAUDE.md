# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.
---
paths: 
  - api/src/main/java/**/*.java
  - core/src/main/java/**/*.java
  - service/src/main/java/**/*.java
---

## Build & Run Commands

```bash
mvn clean compile       # Compile all modules
mvn test                # Run all tests
mvn clean package       # Package JAR
mvn spring-boot:run -pl api  # Run application (api module only)
```

Run a single test class:
```bash
mvn test -Dtest=ProductServiceTest
mvn test -Dtest=CategoryControllerTest
mvn test -Dtest=*Test   # Run all tests matching pattern
```

## Architecture

Multi-module Spring Boot app for e-commerce:
- **core**: Domain entities, DTOs, exceptions
- **service**: Business logic, interfaces + implementations
- **api**: REST controllers, global exception handler, Spring configs


```
api → service → core
```

- **Controller** (`controller/`): REST endpoints, delegates to service, no business logic
- **Service** (`service/`): Transactions, BCrypt password hashing, business rules; defined as interface + `*Impl`
- **Repository** (`repository/`): Extends `JpaRepository`, custom finders for email/username
- **Entity** (`entity/`): JPA-annotated, audit timestamps (`createdAt`, `updatedAt`), unique email constraint
- **DTO** (`dto/`): `*Request` for validated input, `*Response` for output (no password), `*UpdateRequest` for updates
- **Exception** (`exception/`): Custom exceptions — `UserNotFoundException`, `DuplicateEmailException`, `InvalidUserDataException`
- **GlobalExceptionHandler**: `@ControllerAdvice` for centralized error responses (lives in `edu.ecommerce`)
- **Config**: `SecurityConfig` (permits all, dev mode), `AppConfig` (bean config) — also in `edu.ecommerce`

API endpoints follow pattern `/api/v1/{resource}`:
  - Users: `POST /api/v1/users/register`, `GET /api/v1/users/{id}`, `GET /api/v1/users/email/{email}`, `GET /api/v1/users?page=0&size=10`, `PUT /api/v1/users/{id}`, `DELETE /api/v1/users/{id}`
  - Categories: `POST /api/v1/categories`, `GET /api/v1/categories/{id}`, `GET /api/v1/categories`, `PUT /api/v1/categories/{id}`, `DELETE /api/v1/categories/{id}`
  - Products: `POST /api/v1/products`, `GET /api/v1/products/{id}`, `GET /api/v1/products`, `PUT /api/v1/products/{id}`, `DELETE /api/v1/products/{id}`

## Database Migrations

Flyway manages schema versioning. Migrations live in `core/src/main/resources/db/migration/`:
- **Naming**: `V{version}__description.sql` (e.g., `V1__add_users_table.sql`)
- **Execution**: Automatic on application startup; ordered by version number
- **Adding migrations**: Create new file with next version number; Flyway won't execute if tables already exist (idempotency handled via schema versioning, not script content)

Don't modify existing migration files — create new migrations for schema changes.

## Key Design Rules

**Validation belongs on DTOs, not entities.** Request DTOs carry `@NotBlank`, `@Size`, `@Email`, `@Pattern` so invalid data is rejected at the API boundary. Entities use only `@Column` constraints for schema enforcement.

**Password hashing (User module)** — raw passwords never reach the repository; hashing occurs in `UserServiceImpl`.

**Foreign key constraints**: Entities use `@ManyToOne` with nullable validation at the DTO level (e.g., `Product` requires a valid `Category`)

## Testing

Test naming: `*ServiceTest` for unit tests, `*ControllerTest` for integration tests.

- **Unit tests**: Mockito for mocking, no Spring context; placed in `service/src/test`
- **Integration tests**: `@SpringBootTest(webEnvironment = RANDOM_PORT)` + REST Assured + H2 in-memory DB

Integration test setup:
```java
@SpringBootTest(webEnvironment = RANDOM_PORT,
    properties = {"spring.datasource.url=jdbc:h2:mem:copilot;DB_CLOSE_ON_EXIT=FALSE"})
class ProductControllerTest {
    @LocalServerPort int port;
    @BeforeEach void setUp() { RestAssured.port = port; }
}
```

Use given/when/then pattern with REST Assured. H2 is configured via test properties with `create-drop` DDL (schema recreated per test run). Flyway migrations apply automatically on startup, then test fixtures populate data as needed.

## Code Style

- Java 17, 4-space indent, 120-char line limit, UTF-8
- **Lombok**: `@Data` for plain POJOs, `@Getter`/`@Setter` for selective exposure, `@RequiredArgsConstructor` for constructor DI — never `@AllArgsConstructor` on entities or DTOs
- Constructor injection only (no `@Autowired` on fields)
- `application.properties` (not YAML) for configuration
- **Naming conventions**: 
  - Classes: `*Controller`, `*Service`, `*ServiceImpl`, `*Repository`, `*Request`, `*Response`, `*UpdateRequest`, `*Test`
  - Packages: `{basepackage}.{module}.{layer}` (e.g., `edu.ecommerce.core.entity`, `edu.ecommerce.service`, `edu.ecommerce.api.controller`)
- **DTO pattern**: `*Request` for input (validated), `*Response` for output (no sensitive fields), `*UpdateRequest` for updates (subset of fields)
