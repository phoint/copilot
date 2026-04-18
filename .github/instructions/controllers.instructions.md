---
description: "Use when writing REST controllers, handling requests/responses, validation, or error handling. Covers endpoint design, exception handling, and response patterns."
applyTo: "**/*Controller.java"
---

# REST Controller Guidelines

## Basic Controller Structure

```java
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public UserResponse createUser(@RequestBody UserRequest request) {
        return userService.createUser(request);
    }
}
```

**Key points**:
- Use `@RequiredArgsConstructor` + field injection for dependency injection
- Use `@RequestMapping` for base path, `@GetMapping`/`@PostMapping` for operations
- Return DTOs (`*Response`), not domain entities

## Request & Response DTOs

### Request DTO with Validation
```java
@Data
@RequiredArgsConstructor
public class UserRequest {
    @NotBlank(message = "Name is required")
    private final String name;

    @Email(message = "Email must be valid")
    private final String email;

    @NotNull(message = "Status is required")
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Status must be ACTIVE or INACTIVE")
    private final String status;
}
```

### Response DTO
```java
@Data
public class UserResponse {
    private final Long id;
    private final String name;
    private final String email;
    private final String status;
    private final LocalDateTime createdAt;
}
```

## HTTP Methods & Paths

| Operation | Method | Path | Status | Example |
|-----------|--------|------|--------|---------|
| List all | GET | `/users` | 200 | `GET /users?status=ACTIVE&limit=10` |
| Get one | GET | `/users/{id}` | 200 / 404 | `GET /users/1` |
| Create | POST | `/users` | 201 | `POST /users` |
| Update | PUT | `/users/{id}` | 200 / 404 | `PUT /users/1` |
| Partial update | PATCH | `/users/{id}` | 200 / 404 | `PATCH /users/1` |
| Delete | DELETE | `/users/{id}` | 204 / 404 | `DELETE /users/1` |

## Common Endpoint Patterns

### List with Pagination & Filtering

```java
@GetMapping
public PageResponse<UserResponse> listUsers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int limit,
    @RequestParam(required = false) String status) {
    
    return userService.listUsers(page, limit, status);
}
```

### Get by ID

```java
@GetMapping("/{id}")
public UserResponse getUserById(@PathVariable Long id) {
    return userService.getUserById(id);  // throws UserNotFoundException if not found
}
```

### Create with Validation

```java
@PostMapping
public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
    UserResponse response = userService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### Update

```java
@PutMapping("/{id}")
public UserResponse updateUser(
    @PathVariable Long id,
    @Valid @RequestBody UserRequest request) {
    
    return userService.updateUser(id, request);
}
```

### Delete

```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
}
```

## Path Variables vs Query Parameters

### Path Variables (for resource IDs)
```java
@GetMapping("/users/{userId}/orders/{orderId}")
public OrderResponse getOrder(
    @PathVariable Long userId,
    @PathVariable Long orderId) {
    // ...
}
```

### Query Parameters (for filtering, pagination, sorting)
```java
@GetMapping("/users")
public List<UserResponse> listUsers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int limit,
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String sortBy) {
    // ...
}
```

## Error Handling with @ControllerAdvice

Create centralized exception handler:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex) {
        String messages = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));

        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            messages,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### Error Response DTO

```java
@Data
@RequiredArgsConstructor
public class ErrorResponse {
    private final String code;
    private final String message;
    private final LocalDateTime timestamp;
}
```

## Status Codes

| Code | Meaning | When to Use |
|------|---------|------------|
| 200 | OK | GET success, PUT/PATCH success |
| 201 | Created | POST success |
| 204 | No Content | DELETE success, update with no response body |
| 400 | Bad Request | Validation error, malformed input |
| 401 | Unauthorized | Missing/invalid authentication |
| 403 | Forbidden | Authenticated but no permission |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate email, business rule violation |
| 500 | Internal Error | Unexpected server error |

## Logging in Controllers

```java
private static final Logger log = LoggerFactory.getLogger(UserController.class);

@PostMapping
public UserResponse createUser(@Valid @RequestBody UserRequest request) {
    log.info("Creating user with email: {}", request.getEmail());
    try {
        UserResponse response = userService.createUser(request);
        log.info("User created successfully with id: {}", response.getId());
        return response;
    } catch (DuplicateEmailException e) {
        log.warn("Duplicate email attempt: {}", request.getEmail());
        throw e;
    }
}
```

## ResponseEntity for Fine-Grained Control

```java
@PostMapping
public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest request) {
    try {
        UserResponse response = userService.createUser(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("X-User-Id", response.getId().toString())
            .body(response);
    } catch (ValidationException e) {
        return ResponseEntity.badRequest().body(
            new ErrorResponse("VALIDATION_ERROR", e.getMessage(), LocalDateTime.now())
        );
    }
}
```

## Input Validation

Always validate request bodies:

```java
@PostMapping
public UserResponse createUser(@Valid @RequestBody UserRequest request) {
    return userService.createUser(request);
}
```

For custom validation logic, create validators:

```java
@Component
public class UserValidator {
    public void validateUserRequest(UserRequest request) {
        if (request.getName().length() < 2) {
            throw new ValidationException("Name must be at least 2 characters");
        }
        if (!isValidEmail(request.getEmail())) {
            throw new ValidationException("Invalid email format");
        }
    }
}
```
