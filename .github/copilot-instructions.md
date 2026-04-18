# Spring Boot Project Guidelines

## Code Style

- **Java version**: Target Java 17+ (11+)
- **Naming conventions**:
  - Controllers: `*Controller` (e.g., `HelloController`)
  - Services: `*Service` (e.g., `OrderService`)
  - Repositories: `*Repository` (e.g., `UserRepository`)
  - DTOs: `*Request`, `*Response`, `*Dto` suffixes
  - Test classes: `*Test` (e.g., `HelloControllerTest`)

- **Lombok usage**:
  - Use `@Data` for simple POJOs with getters, setters, `equals()`, `hashCode()`
  - Use `@Getter` / `@Setter` when selective attribute exposure is needed
  - Use `@RequiredArgsConstructor` for constructor-based dependency injection
  - Never use `@AllArgsConstructor` on entities or request/response classes

- **Formatting**:
  - 4 spaces indentation
  - Line length: 120 characters max
  - UTF-8 encoding throughout

## Architecture & Design

- **Layer structure**:
  - `Controller`: REST endpoints, request/response mapping
  - `Service`: Business logic, orchestration, transactions
  - `Repository`: Data access (JDBC, JPA patterns)
  - `Config`: Spring configuration beans, constants
  - `Dto`: Data transfer objects, request/response models

- **Dependency Injection**:
  - Prefer constructor injection via `@Autowired` or `@RequiredArgsConstructor`
  - Use `@Component`, `@Service`, `@RestController`, `@Repository` class annotations
  - Avoid field injection (`@Autowired` on fields)

- **Configuration**:
  - Use `application.properties` (not YAML) for Spring Boot properties
  - Externalize environment-specific settings (`dev`, `test`, `prod`)
  - Reference: See `src/main/resources/application.properties` for existing patterns

## API Design

- **REST conventions**:
  - Use `@RequestMapping` with explicit method paths (e.g., `/calc`, `/users/{id}`)
  - Use `@RequestParam` for query parameters
  - Use `@PathVariable` for URL path variables
  - Return appropriate status codes: 200 (success), 400 (bad request), 404 (not found), 500 (server error)

- **Request/Response**:
  - Wrap responses in a consistent DTO structure
  - Include all necessary fields in response objects
  - Validate input parameters and return clear error messages
  - Example pattern: `HelloController.Result` (data class with all relevant fields)

- **Error Handling**:
  - Use `@ControllerAdvice` for centralized exception handling
  - Return meaningful error messages with HTTP status codes
  - Log errors with appropriate context

## Testing

- **Framework stack**:
  - JUnit 5 (`org.junit.jupiter`)
  - REST Assured (`io.restassured`) for API testing
  - Spring Boot Test (`@SpringBootTest`)

- **Test structure**:
  - Test class name: `<Feature>Test` (e.g., `HelloControllerTest`)
  - Use `@BeforeEach` for setup (e.g., `RestAssured.port = port;`)
  - One logical test per `@Test` method (given-when-then pattern)

- **Integration test patterns**:
  - Use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` for embedded server
  - Inject `@LocalServerPort` to get the dynamically assigned port
  - Use `RestAssured.given()`, `when()`, `then()` for fluent API assertions
  - Assert both structure and values via `.body("field", is(expectedValue))`

- **Database for tests**:
  - Use in-memory H2 database: `jdbc:h2:mem:copilot;DB_CLOSE_ON_EXIT=FALSE`
  - Configure via `properties` attribute in `@SpringBootTest` annotation
  - Example: `properties = {"spring.datasource.url=jdbc:h2:mem:copilot;DB_CLOSE_ON_EXIT=FALSE"}`

- **Coverage expectations**:
  - Aim for 70%+ code coverage
  - Test happy paths and error conditions
  - Verify data transformations (especially in JDBC-based code)

## Build and Test

- **Build tool**: Maven (see `pom.xml`)
- **Commands**:
  - Build: `mvn clean compile`
  - Test: `mvn test`
  - Package: `mvn clean package`
  - Run: `mvn spring-boot:run`

- **Properties file**:
  - Main config: `src/main/resources/application.properties`
  - Development overrides: `src/main/resources/application-dev.properties`
  - Test config: Provide via `@SpringBootTest(properties = {...})`

## Best Practices

1. **Keep controllers thin**: Business logic belongs in services
2. **Immutability**: Prefer immutable DTOs and value objects
3. **Logging**: Use SLF4J with proper log levels (DEBUG, INFO, WARN, ERROR)
4. **Security**: Validate all inputs, sanitize output
5. **Performance**: 
   - Use named parameters in SQL (via `NamedParameterJdbcTemplate`)
   - Minimize database queries (avoid N+1 problems)
   - Cache when appropriate
6. **Documentation**:
   - Document complex business logic with clear comments
   - Use meaningful variable/method names over verbose comments
   - Include Javadoc for public APIs
