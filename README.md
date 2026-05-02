# Spring Boot E-Commerce System

## Project Overview

This is a comprehensive Spring Boot application implementing an e-commerce system with User Management, Product Catalog, Category Management, and Order Service with advanced state machine patterns. The implementation follows best practices for layered architecture, validation, security, state management, and testing.

## Question to Answer

### 1. Why is it important to use @Transactional on service methods that modify multiple entities (e.g., creating an Order and updating Product stock)? What could go wrong without it?
Using `@Transactional` on service methods that modify multiple entities is crucial for ensuring data integrity and consistency. When a method is annotated with `@Transactional`, it means that all operations within that method are executed within a single transaction context. If any operation fails (e.g., an exception is thrown), the entire transaction will be rolled back, preventing partial updates to the database. For example, when creating an Order, you might need to save the Order entity and also update the stock quantity of the associated Product. If the Order is saved successfully but the Product stock update fails (e.g., due to a database constraint violation), without `@Transactional`, the Order would be created while the Product stock remains unchanged, leading to data inconsistency. With `@Transactional`, if the Product stock update fails, the Order creation will also be rolled back, ensuring that the database remains in a consistent state.

### 2. Explain the difference between a JPQL query and a Native SQL query in a @Query annotation. When would you choose one over the other?
A JPQL (Java Persistence Query Language) query is a query language that operates on the entity objects and their relationships defined in the JPA context. It is database-agnostic and allows you to write queries using the entity model rather than the underlying database schema. For example, you can query for `Order` entities based on their `status` without worrying about the actual table structure.

A Native SQL query, on the other hand, is a query that is written in the native SQL dialect of the underlying database. It allows you to leverage database-specific features and optimizations but makes your code less portable across different databases.

You would choose a JPQL query when you want to write database-agnostic code and work with the entity model. You would choose a Native SQL query when you need to use database-specific features or when performance is critical and you need to optimize the query for a specific database.

### 3. In the context of our OrderItem entity, why is storing the price directly better than just linking to the Product entity and fetching its price?
Storing the price directly in the OrderItem entity is better than just linking to the Product entity and fetching its price because it captures the price at the time of the order. This is important for historical accuracy and consistency. If you only link to the Product entity, any changes to the product's price after the order is placed would affect the order's total amount, which can lead to discrepancies and confusion for both customers and administrators. By storing the price directly in the OrderItem, you ensure that the order reflects the correct pricing information as it was at the time of purchase, regardless of any future changes to the product's price. This also simplifies calculations for discounts, promotions, and refunds, as you have a clear record of what was charged for each item in the order.
## Architecture

The application uses a multi-module architecture:
- **core module**: Contains core entities, DTOs, exceptions, enums, and pricing modifiers
- **service module**: Contains service interfaces, implementations, repositories, and Spring State Machine configuration
- **api module**: Contains REST controllers, global exception handler, and security configuration

**Dependency Flow**: `api → service → core`

### Module Responsibilities
- **core**: Domain models, validation rules, extension points (OrderPricingModifier interface)
- **service**: Business logic, transactions, state machine orchestration, repository access
- **api**: HTTP contracts, request/response marshaling, exception handling, security

## 🆕 Order Service with Spring State Machine

### Overview
The Order Service implements a sophisticated order lifecycle using **Spring State Machine 4.0.0** with **guard conditions** to enforce business rules. This design is fully extensible for future services (Discount, Promotion, Membership, Shipping) without modifying existing code.

### Order States & Transitions
```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED → REFUNDED
   ↓        ↓             ↓          (terminal)
CANCELLED (terminal)
```

**Terminal States**: CANCELLED, REFUNDED (no outgoing transitions)

### State Machine Features
- **Spring State Machine 4.0.0**: Framework-based state management with reactive APIs
- **Guard Conditions**: Business rule validation before transitions
  - `hasItems()` - Order must contain items
  - `hasValidTotal()` - Order total must be non-negative
  - `hasShippingAddress()` - Shipping address required for SHIP transition
  - `canBeCancelled()` - Cannot cancel from terminal states
  - `withinMaxOrderValue()` - Fraud prevention ($100k max)
  - `notAlreadyRefunded()` - Idempotency check

- **Extensible Pricing**: `OrderPricingModifier` interface allows future services to hook pricing logic without touching order code
  - Discount Service implementation
  - Promotion Service implementation
  - Membership Service implementation
  - Shipping Service implementation

### Order Service Components

#### Core Entities
- **Order**: Main order aggregate with bi-directional relationship to OrderItem
  - Status (enum), shippingAddress, promoCode, discountAmount, shippingCost, membershipDiscount, totalAmount
  - Audit timestamps (createdAt, updatedAt)
  - Future-service columns for stable API contracts

- **OrderItem**: Order line items (leaf entity)
  - References to Order and Product
  - quantity, unitPrice (snapshot at order time)

#### DTOs
- **OrderRequest**: Create order with items, shipping address, promo code
- **OrderResponse**: Complete order view with flattened user/product details
- **OrderUpdateRequest**: Partial update (optional fields)
- **OrderItemRequest/Response**: Line item transfer objects

#### Services
- **OrderService** (interface): Define order operations
- **OrderServiceImpl**: Implements service with Spring State Machine integration
  - Injects `StateMachineFactory<OrderStatus, OrderEvent>`
  - `applyTransition()` method orchestrates state machine + guard validation
  - Total amount recalculation with pricing modifiers
  - Pagination support for listing

#### Repositories
- **OrderRepository**: JPA with custom finders (byUserId, byStatus)
- **OrderItemRepository**: JPA with order item queries

#### Configuration
- **OrderStateMachineConfig**: `@EnableStateMachineFactory` with all state/transition/guard configuration
- **OrderEvent** enum: CONFIRM, START_PROCESSING, SHIP, DELIVER, CANCEL, REFUND

#### REST Endpoints
```
POST   /api/v1/orders                 - Create order
GET    /api/v1/orders/:id             - Get order by ID
GET    /api/v1/orders                 - List all orders (paginated)
GET    /api/v1/orders/user/:userId    - Get orders by user
PATCH  /api/v1/orders/:id/status      - Update order status (state transition)
PATCH  /api/v1/orders/:id             - Update order (partial)
POST   /api/v1/orders/:id/cancel      - Cancel order
```

#### Exception Handling
- **OrderNotFoundException**: 404 when order not found
- **InvalidOrderTransitionException**: 400 when state transition blocked by state machine or guards

---

## Components Implemented

### Core Entities
- **User Entity** (`src/main/java/edu/ecommerce/user/entity/User.java`)
  - Fields: id, username, email, password (hashed), role, status, createdAt, updatedAt
  - Constraints: Unique email, audit timestamps
  - Uses Lombok for boilerplate reduction
- **Product Entity** (`src/main/java/edu/ecommerce/product/entity/Product.java`)
  - Fields: id, name, description, price, stockQuantity, category (ManyToOne), createdAt, updatedAt
  - Constraints: Non-negative price and stock, audit timestamps
- **Category Entity** (`src/main/java/edu/ecommerce/category/entity/Category.java`)
  - Fields: id, name, description, createdAt, updatedAt
  - Constraints: Unique name, audit timestamps

### Data Access
- **UserRepository** (`src/main/java/edu/ecommerce/user/repository/UserRepository.java`)
  - Extends JpaRepository for CRUD operations
  - Custom finder methods for email and username
- **ProductRepository** (`src/main/java/edu/ecommerce/product/repository/ProductRepository.java`)
  - Extends JpaRepository for CRUD operations
  - Custom finder methods for product queries
- **CategoryRepository** (`src/main/java/edu/ecommerce/category/repository/CategoryRepository.java`)
  - Extends JpaRepository for CRUD operations
  - Custom finder methods for category queries

### Business Logic
- **UserService** (`src/main/java/edu/ecommerce/user/service/UserService.java` - Interface)
- **UserServiceImpl** (`src/main/java/edu/ecommerce/user/service/UserServiceImpl.java` - Implementation)
  - Password hashing using BCrypt
  - Transaction management
  - User creation, retrieval, update, deletion
- Validation logic for user data
- **ProductService** (`src/main/java/edu/ecommerce/product/service/ProductService.java` - Interface)
- **ProductServiceImpl** (`src/main/java/edu/ecommerce/product/service/ProductServiceImpl.java` - Implementation)
  - Product creation, retrieval, update, deletion
  - Validation logic for product data
- **CategoryService** (`src/main/java/edu/ecommerce/category/service/CategoryService.java` - Interface)
- **CategoryServiceImpl** (`src/main/java/edu/ecommerce/category/service/CategoryServiceImpl.java` - Implementation)
  - Category creation, retrieval, update, deletion
  - Validation logic for category data

### REST API
- **UserController** (`src/main/java/edu/ecommerce/user/controller/UserController.java`)
  - CRUD endpoints with pagination support
  - Input validation using Bean Validation
  - Error handling with custom exceptions
- **ProductController** (`src/main/java/edu/ecommerce/product/controller/ProductController.java`)
  - CRUD endpoints with pagination support
  - Input validation using Bean Validation
  - Error handling with custom exceptions
- **CategoryController** (`src/main/java/edu/ecommerce/category/controller/CategoryController.java`)
  - CRUD endpoints with pagination support
  - Input validation using Bean Validation
  - Error handling with custom exceptions

### Data Transfer Objects
- **UserRequest** (`src/main/java/edu/ecommerce/user/dto/UserRequest.java`)
  - Validation: Username (3-50 chars), Email (valid format), Password (8-30 chars, at least one letter and digit), Role (ADMIN/USER)
- **UserResponse** (`src/main/java/edu/ecommerce/user/dto/UserResponse.java`)
  - Excludes sensitive fields like password
- **ProductRequest** (`src/main/java/edu/ecommerce/product/dto/ProductRequest.java`)
  - Validation: Name (3-100 chars), Description (optional), Price (positive), Stock Quantity (non-negative)
- **ProductResponse** (`src/main/java/edu/ecommerce/product/dto/ProductResponse.java`)
  - Includes product details without sensitive information
- **CategoryRequest** (`src/main/java/edu/ecommerce/category/dto/CategoryRequest.java`)
  - Validation: Name (3-100 chars), Description (optional)
- **CategoryResponse** (`src/main/java/edu/ecommerce/category/dto/CategoryResponse.java`)
  - Includes category details without sensitive information

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
mvn spring-boot:run -pl api
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
