# Spring Boot User Management Implementation

## Project Overview

This is a Spring Boot application implementing reusable User Management building blocks for an e-commerce system. The implementation follows best practices for layered architecture, validation, security, and testing.

## Architecture

The application uses a layered architecture:

- **Controller Layer**: REST endpoints with validation and error handling
- **Service Layer**: Business logic with transactions and password hashing
- **Repository Layer**: Data access using Spring Data JPA
- **Entity Layer**: JPA entities with audit fields

## Components Implemented

### Core Entities
- **User Entity** (`src/main/java/edu/ecommerce/user/entity/User.java`)
  - Fields: id, username, email, password (hashed), role, status, createdAt, updatedAt
  - Constraints: Unique email, audit timestamps
  - Uses Lombok for boilerplate reduction

### Data Access
- **UserRepository** (`src/main/java/edu/ecommerce/user/repository/UserRepository.java`)
  - Extends JpaRepository for CRUD operations
  - Custom finder methods for email and username

### Business Logic
- **UserService** (`src/main/java/edu/ecommerce/user/service/UserService.java` - Interface)
- **UserServiceImpl** (`src/main/java/edu/ecommerce/user/service/UserServiceImpl.java` - Implementation)
  - Password hashing using BCrypt
  - Transaction management
  - User creation, retrieval, update, deletion

### REST API
- **UserController** (`src/main/java/edu/ecommerce/user/controller/UserController.java`)
  - CRUD endpoints with pagination support
  - Input validation using Bean Validation
  - Error handling with custom exceptions

### Data Transfer Objects
- **UserRequest** (`src/main/java/edu/ecommerce/user/dto/UserRequest.java`)
  - Validation: Username (3-50 chars), Email (valid format), Password (8-30 chars, at least one letter and digit), Role (ADMIN/USER)
- **UserResponse** (`src/main/java/edu/ecommerce/user/dto/UserResponse.java`)
  - Excludes sensitive fields like password

### Exception Handling
- **GlobalExceptionHandler** (`src/main/java/edu/ecommerce/GlobalExceptionHandler.java`)
  - Handles validation errors, not found, and general exceptions
  - Returns appropriate HTTP status codes and error messages

### Configuration
- **SecurityConfig** (`src/main/java/edu/ecommerce/SecurityConfig.java`)
  - Permits all requests for API testing (can be secured later)
- **AppConfig** (`src/main/java/edu/ecommerce/AppConfig.java`)
  - Additional bean configurations

## Testing

### Unit Tests
- **UserServiceTest** (`src/test/java/edu/ecommerce/user/service/UserServiceTest.java`)
  - 10 tests using Mockito for mocking dependencies
  - Tests service methods: create, find, update, delete, password hashing

### Integration Tests
- **UserControllerTest** (`src/test/java/edu/ecommerce/user/controller/UserControllerTest.java`)
  - 10 tests using REST Assured for API testing
  - Tests endpoints with H2 in-memory database
  - Covers happy paths and error scenarios

### Test Configuration
- Uses `@SpringBootTest` with RANDOM_PORT for embedded server
- H2 database configured via test properties
- Unique test data to avoid conflicts

## Dependencies

Key dependencies (from pom.xml):
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Validation
- H2 Database (runtime)
- Lombok
- REST Assured (test)
- JUnit 5 (test)

## Build and Run

### Prerequisites
- Java 17+
- Maven 3.6+

### Commands
```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Package the application
mvn clean package

# Run the application
mvn spring-boot:run
```

### Application Properties
- Main config: `src/main/resources/application.properties`
- Database: H2 in-memory for development/testing
- Logging: Basic Spring Boot logging

## API Usage

### Create User
```bash
POST /api/v1/users/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "Password123",
  "role": "USER"
}
```

### Get User by ID
```bash
GET /api/v1/users/{id}
```

### Get User by Email
```bash
GET /api/v1/users/email/{email}
```

### List All Users (with Pagination)
```bash
GET /api/v1/users?page=0&size=10&sort=username,asc
```

Response includes pagination metadata:
```json
{
  "content": [
    {
      "id": 1,
      "username": "john_doe",
      "email": "john@example.com",
      "role": "USER",
      "status": "ACTIVE",
      "createdAt": "2026-04-18T...",
      "updatedAt": "2026-04-18T..."
    }
  ],
  "pageable": {
    "page": 0,
    "size": 10,
    "sort": ["username,asc"]
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "size": 10,
  "number": 0
}
```

### Update User
```bash
PUT /api/v1/users/{id}
Content-Type: application/json

{
  "username": "john_doe_updated",
  "email": "john_updated@example.com",
  "password": "NewPassword123",
  "role": "ADMIN"
}
```

### Delete User
```bash
DELETE /api/v1/users/{id}
```

## Validation Rules

- **Username**: 3-50 characters, required
- **Email**: Valid email format, required, unique
- **Password**: 8-30 characters, required, at least one letter and one digit
- **Role**: Must be "ADMIN" or "USER"

## Security Features

- Passwords are hashed using BCrypt before storage
- Input validation prevents common vulnerabilities
- Unique constraints on email prevent duplicates

## Future Extensions

This User Management module serves as a foundation for:
- Product Management
- Order Management
- Authentication/Authorization
- User profiles and preferences

## Testing Results

- Total Tests: 22
- Unit Tests: 10 (UserService)
- Integration Tests: 10 (UserController)
- Legacy Tests: 2 (HelloController)
- All tests pass successfully

## Code Quality

- Follows Spring Boot best practices
- Uses constructor injection
- Proper exception handling
- Comprehensive test coverage (70%+)
- Clean, readable code with meaningful names

## Question to Answer

### 1. When generating the UserDTO, Copilot added validation annotations like @Email and @Size. Why is it better to perform validation at the DTO level (in the Controller layer) rather than directly on the JPA Entity?

Performing validation at the DTO level (Controller layer) is better than on the JPA Entity for several reasons:

**Separation of Concerns**: DTOs handle data transfer and validation, while Entities focus on persistence. This keeps the Entity clean and focused on database mapping.

**Flexibility**: Different DTOs can have different validation rules for the same Entity. For example, `UserRequest` has password validation (8-30 chars, alphanumeric), but the Entity doesn't need these constraints since passwords are hashed.

**Security**: Validation at the boundary (Controller) prevents invalid data from reaching the business logic. If validation was on the Entity, it might only trigger during persistence, allowing invalid data to flow through the service layer.

**Performance**: Entity validation might occur during every database operation, while DTO validation happens only at API entry points.

**Example from code**:
- `UserRequest` (DTO) has `@NotBlank`, `@Size(min=8, max=30)`, `@Pattern` for password validation
- `User` (Entity) has only `@NotNull` and `@Column` constraints for database schema

### 2. Compare the prompts you used to generate the User entity versus the UserDTO. What keywords or phrases did you find were important to differentiate between a persistence object (Entity) and a data transfer object (DTO)?

**Entity Generation Prompts**: Used terms like "JPA entity", "database table", "@Entity", "audit fields", "relationships". Keywords like "persistence", "database", "JPA" clearly indicated it was for data storage.

**DTO Generation Prompts**: Used terms like "request DTO", "response DTO", "data transfer object", "validation annotations", "API input/output". Keywords like "DTO", "request", "response", "transfer" differentiated it from persistence objects.

**Important Keywords/Phrases**:
- **Entity**: "JPA entity", "database entity", "@Entity annotation", "audit timestamps", "unique constraints"
- **DTO**: "DTO class", "request/response object", "validation annotations", "API model", "data transfer"

These keywords help Copilot understand the context and generate appropriate code with the right annotations and structure.