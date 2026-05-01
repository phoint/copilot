package edu.ecommerce.service.repository;

import edu.ecommerce.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findByStatus(String status);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
