# Spring Boot User Management Implementation

## Project Overview

This is a Spring Boot application implementing reusable User Management building blocks for an e-commerce system. The implementation follows best practices for layered architecture, validation, security, and testing.

## Question to Answer

### 1. How does modularization in Spring Boot improve code maintainability?

Modularization in Spring Boot improves code maintainability by:
- **Separation of Concerns**: Each module (e.g., User Management) encapsulates related functionality, making it easier to understand and manage.
- **Reusability**: Modules can be reused across different parts of the application or even in other projects, reducing code duplication.
- **Scalability**: As the application grows, new features can be added to specific modules without affecting others, allowing for easier scaling.
- **Testing**: Modules can be tested independently, improving test coverage and reliability.

### 2. What are some limitations of code generation tools like GitHub Copilot?

While code generation tools like GitHub Copilot can speed up development, they have limitations:
- **Context Awareness**: They may not fully understand the broader context of the application, leading to code that is syntactically correct but semantically inappropriate.
- **Security Risks**: Generated code may include vulnerabilities if not reviewed carefully, such as hardcoded credentials or insecure practices.
- **Quality Variability**: The quality of generated code can vary, and it may not always follow best practices or coding standards.
- **Dependency on Training Data**: The generated code is based on patterns learned from existing code, which may not always be up-to-date or relevant to the specific use case.
- **Lack of Creativity**: It may not be able to come up with innovative solutions or handle complex logic that requires human intuition and experience.
### 3. What mechanisms can be employed to secure REST APIs in the user management system?
To secure REST APIs in the user management system, the following mechanisms can be employed:
- **Authentication**: Implementing authentication mechanisms such as JWT (JSON Web Tokens) or OAuth to verify the identity of users accessing the API.
- **Authorization**: Using role-based access control (RBAC) to restrict access to certain endpoints based on user roles (e.g., ADMIN, USER).
- **Input Validation**: Validating all incoming data to prevent injection attacks and ensure data integrity.
- **HTTPS**: Enforcing HTTPS to encrypt data in transit and protect against man-in-the-middle attacks.
- **Rate Limiting**: Implementing rate limiting to prevent abuse and protect against denial-of-service (DoS) attacks.
- **CORS**: Configuring Cross-Origin Resource Sharing (CORS) to control which domains can access the API.
- **Security Headers**: Adding security headers (e.g., Content-Security-Policy, X-Content-Type-Options) to protect against common web vulnerabilities.

## Architecture

The application uses a multi-module architecture:
- **core module**: Contains core entities, dtos, and exceptions
- **service module**: Contains service interfaces and implementations
- **api module**: Contains REST controllers and API-related configurations

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

## Postman Collection
A Postman collection is included in the project for easy API testing. Import `SpringBoot_Copilot.postman_collection.json` into Postman to access all endpoints with example requests and responses.

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
