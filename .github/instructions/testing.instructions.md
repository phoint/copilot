---
description: "Use when writing or updating test classes, test fixtures, mocking strategies, or test data builders. Covers test structure, REST Assured patterns, and integration test setup."
applyTo: "**/*Test.java"
---

# Testing Guidelines

## Test Class Structure

```java
@SpringBootTest(
    classes = App.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.datasource.url=jdbc:h2:mem:copilot;DB_CLOSE_ON_EXIT=FALSE"}
)
public class HelloControllerTest {
    @LocalServerPort
    int port;

    @BeforeEach
    public void setUp() throws Exception {
        RestAssured.port = port;
    }

    @Test
    public void testFeature() {
        // given-when-then pattern
    }
}
```

**Key points**:
- Always use `SpringBootTest.WebEnvironment.RANDOM_PORT` for embedded server testing
- Inject `@LocalServerPort` for the dynamically assigned port
- Set `RestAssured.port` in `@BeforeEach`
- Use in-memory H2 for tests: `jdbc:h2:mem:copilot;DB_CLOSE_ON_EXIT=FALSE`

## REST Assured Patterns

### GET Request with Assertions
```java
@Test
public void testGetUser() {
    when()
        .get("/users/{id}", 1)
    .then()
        .statusCode(200)
        .body("id", is(1))
        .body("name", notNullValue());
}
```

### POST Request with Body
```java
@Test
public void testCreateUser() {
    given()
        .contentType(ContentType.JSON)
        .body(new UserRequest("John", "john@example.com"))
    .when()
        .post("/users")
    .then()
        .statusCode(201)
        .body("id", notNullValue())
        .body("name", is("John"));
}
```

### Query Parameters
```java
@Test
public void testSearchWithParams() {
    given()
        .param("status", "active")
        .param("limit", 10)
    .when()
        .get("/users")
    .then()
        .statusCode(200)
        .body("size()", lessThanOrEqualTo(10));
}
```

### Error Handling
```java
@Test
public void testUserNotFound() {
    when()
        .get("/users/{id}", 999)
    .then()
        .statusCode(404)
        .body("message", containsString("not found"));
}
```

## Test Data Builders

Create reusable builders for common test objects:

```java
static class UserRequestBuilder {
    private String name = "John Doe";
    private String email = "john@example.com";

    UserRequestBuilder name(String name) {
        this.name = name;
        return this;
    }

    UserRequestBuilder email(String email) {
        this.email = email;
        return this;
    }

    UserRequest build() {
        return new UserRequest(name, email);
    }
}

@Test
public void testCreateUserWithBuilder() {
    given()
        .contentType(ContentType.JSON)
        .body(new UserRequestBuilder().name("Alice").email("alice@example.com").build())
    .when()
        .post("/users")
    .then()
        .statusCode(201);
}
```

## Test Naming & Organization

- **Happy path**: `test<Feature>()` — e.g., `testCreateUser()`
- **Exceptions**: `test<Feature>_<Condition>()` — e.g., `testCreateUser_MissingEmail()`
- **One logical test per method** — Avoid multiple assertions on unrelated data
- **Use descriptive variable names** — `expectedUserId`, `actualResponse`, not `result` or `r`

## Mocking Strategies

For service-level tests (if mocking dependencies):

```java
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    UserRepository userRepository;

    @InjectMocks
    OrderService orderService;

    @Test
    public void testCreateOrder() {
        // Arrange
        User mockUser = new User(1L, "John");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // Act
        Order order = orderService.createOrder(1L, "item123");

        // Assert
        assertThat(order.getUserId()).isEqualTo(1L);
        verify(userRepository, times(1)).findById(1L);
    }
}
```

## Coverage Goals

- Aim for 70%+ overall coverage
- Test happy paths (successful operations)
- Test error conditions (validation failures, not found, server errors)
- Test data transformations (especially JDBC result mapping)
- Avoid over-testing trivial getters/setters (Lombok-generated)
