---
description: "Use when writing Spring Data JPA repositories, entity mapping, service transactions, and domain persistence patterns. Covers repository interfaces, entity annotations, query methods, and JPA lifecycle behavior."
applyTo: "**/*Repository.java"
---

# Spring Data JPA Guidelines

## Repository Interfaces

Use `JpaRepository` or `CrudRepository` interfaces for data access whenever possible.

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByStatus(String status);
}
```

**Key points**:
- Prefer interface-based repositories instead of manual DAO classes
- Use `JpaRepository<T, ID>` for rich CRUD plus pagination and sorting
- For query methods, follow Spring Data naming conventions

## Entity Mapping

Annotate domain classes with JPA annotations and keep mapping simple.

```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String status;
}
```

### Best practices for entities
- Use `@Column(nullable = false)` for required fields
- Use `@Column(unique = true)` for natural keys like email
- Avoid business logic in entities; keep fields and mapping only
- Prefer immutable state only when compatible with JPA constructors

## Query Methods

Example query method support:

```java
Optional<User> findByEmail(String email);
List<User> findByStatusOrderByIdAsc(String status);
List<User> findByNameContainingIgnoreCase(String name);
```

### Custom queries
Use `@Query` for complex lookups only when naming conventions are insufficient.

```java
@Query("SELECT u FROM User u WHERE u.status = :status AND u.createdAt >= :since")
List<User> findActiveUsersSince(@Param("status") String status, @Param("since") LocalDateTime since);
```

## Transactions

Keep transactional boundaries in service classes.

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateEmailException("Email already in use");
        }
        return userRepository.save(user);
    }
}
```

**Note**: Declare `@Transactional` on services, not on repositories.

## Pagination and Sorting

```java
Page<User> findAll(Pageable pageable);
```

```java
public Page<UserResponse> listUsers(int page, int size, String status) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
    return userRepository.findByStatus(status, pageable)
        .map(this::toResponse);
}
```

## Entity Lifecycle and Dirty Checking

- Use `save()` for create/update
- Use `findById()` and modify fields inside a transactional service for automatic dirty checking
- Avoid manual `save()` inside the same transaction when the entity is already managed

```java
@Transactional
public User updateUser(Long id, UserRequest request) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
    user.setName(request.getName());
    user.setEmail(request.getEmail());
    return user;
}
```

## Custom Repository Implementation

Use custom repository implementations only for behavior that cannot be expressed with query methods.

```java
public interface OrderRepositoryCustom {
    List<Order> findOrdersByComplexCriteria(OrderSearchCriteria criteria);
}

@Repository
public class OrderRepositoryImpl implements OrderRepositoryCustom {
    private final EntityManager em;

    public OrderRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<Order> findOrdersByComplexCriteria(OrderSearchCriteria criteria) {
        // Criteria API or JPQL implementation
    }
}
```

## Performance Considerations

- Use pagination (`Pageable`) for large result sets
- Avoid eager loading on collection relationships unless required
- Use `@EntityGraph` or explicit `JOIN FETCH` for relationship queries when needed
- Use `@Query` with `LIMIT`/`FIRST` patterns for bounded queries

## Exception Handling

Convert JPA exceptions into domain-specific exceptions in service layer.

```java
public User getUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
}
```

## Schema Generation

For H2 tests, use Spring Boot defaults or explicit JPA settings.

- `spring.jpa.hibernate.ddl-auto=update`
- `spring.datasource.url=jdbc:h2:mem:copilot;DB_CLOSE_ON_EXIT=FALSE`

## When to use Spring Data JPA

- Use JPA for object-relational mapping and entity lifecycle management
- Use repository interfaces for standard CRUD operations and pagination
- Use custom implementations only for complex queries that cannot be modeled with method names or JPQL
