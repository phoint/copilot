package edu.ecommerce.api.repository;

import edu.ecommerce.core.entity.User;
import edu.ecommerce.service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class UserRepositoryJpaTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedpassword");
        testUser.setRole("USER");
        testUser.setStatus("ACTIVE");
    }

    @Test
    void testSaveUser_ShouldPersistAndGenerateId() {
        User savedUser = userRepository.save(testUser);
        testEntityManager.flush();

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getId()).isGreaterThan(0);
    }

    @Test
    void testFindByEmail_ShouldReturnUser() {
        testEntityManager.persistAndFlush(testUser);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found)
            .isPresent()
            .hasValueSatisfying(user -> assertThat(user.getUsername()).isEqualTo("testuser"));
    }

    @Test
    void testFindByEmail_ShouldReturnEmptyWhenNotFound() {
        testEntityManager.persistAndFlush(testUser);

        Optional<User> found = userRepository.findByEmail("notfound@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void testFindByUsername_ShouldReturnUser() {
        testEntityManager.persistAndFlush(testUser);

        Optional<User> found = userRepository.findByUsername("testuser");

        assertThat(found)
            .isPresent()
            .hasValueSatisfying(user -> assertThat(user.getEmail()).isEqualTo("test@example.com"));
    }

    @Test
    void testFindByStatus_ShouldReturnUsersByStatus() {
        User activeUser = new User();
        activeUser.setUsername("active_test_" + System.currentTimeMillis());
        activeUser.setEmail("active_test_" + System.currentTimeMillis() + "@example.com");
        activeUser.setPassword("password");
        activeUser.setRole("USER");
        activeUser.setStatus("ACTIVE");

        User inactiveUser = new User();
        inactiveUser.setUsername("inactive_test_" + System.currentTimeMillis());
        inactiveUser.setEmail("inactive_test_" + System.currentTimeMillis() + "@example.com");
        inactiveUser.setPassword("password");
        inactiveUser.setRole("USER");
        inactiveUser.setStatus("INACTIVE");

        testEntityManager.persistAndFlush(activeUser);
        testEntityManager.persistAndFlush(inactiveUser);

        List<User> activeUsers = userRepository.findByStatus("ACTIVE");

        assertThat(activeUsers)
            .hasSizeGreaterThanOrEqualTo(1)
            .allMatch(user -> user.getStatus().equals("ACTIVE"))
            .anyMatch(user -> user.getUsername().equals(activeUser.getUsername()));
    }

    @Test
    void testFindByStatus_ShouldReturnEmptyWhenNoMatch() {
        testEntityManager.persistAndFlush(testUser);

        List<User> users = userRepository.findByStatus("NONEXISTENT");

        assertThat(users).isEmpty();
    }

    @Test
    void testExistsByEmail_ShouldReturnTrueWhenExists() {
        testEntityManager.persistAndFlush(testUser);

        boolean exists = userRepository.existsByEmail("test@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByEmail_ShouldReturnFalseWhenNotExists() {
        testEntityManager.persistAndFlush(testUser);

        boolean exists = userRepository.existsByEmail("notfound@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void testExistsByUsername_ShouldReturnTrueWhenExists() {
        testEntityManager.persistAndFlush(testUser);

        boolean exists = userRepository.existsByUsername("testuser");

        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByUsername_ShouldReturnFalseWhenNotExists() {
        testEntityManager.persistAndFlush(testUser);

        boolean exists = userRepository.existsByUsername("notfound");

        assertThat(exists).isFalse();
    }

    @Test
    void testUpdate_ShouldModifyExistingUser() {
        User persistedUser = testEntityManager.persistAndFlush(testUser);

        persistedUser.setEmail("updated@example.com");
        persistedUser.setStatus("INACTIVE");
        userRepository.save(persistedUser);
        testEntityManager.flush();

        User updated = testEntityManager.find(User.class, persistedUser.getId());

        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    void testDelete_ShouldRemoveUser() {
        User persistedUser = testEntityManager.persistAndFlush(testUser);
        Long userId = persistedUser.getId();

        userRepository.deleteById(userId);
        testEntityManager.flush();

        User deleted = testEntityManager.find(User.class, userId);

        assertThat(deleted).isNull();
    }

    @Test
    void testFindById_ShouldReturnUser() {
        User persistedUser = testEntityManager.persistAndFlush(testUser);

        Optional<User> found = userRepository.findById(persistedUser.getId());

        assertThat(found)
            .isPresent()
            .hasValueSatisfying(user -> {
                assertThat(user.getUsername()).isEqualTo("testuser");
                assertThat(user.getEmail()).isEqualTo("test@example.com");
            });
    }

    @Test
    void testFindAll_ShouldReturnAllUsers() {
        User user2 = new User();
        user2.setUsername("testuser2");
        user2.setEmail("test2@example.com");
        user2.setPassword("password");
        user2.setRole("USER");
        user2.setStatus("ACTIVE");

        testEntityManager.persistAndFlush(testUser);
        testEntityManager.persistAndFlush(user2);

        List<User> allUsers = userRepository.findAll();

        assertThat(allUsers)
            .hasSizeGreaterThanOrEqualTo(2)
            .anyMatch(user -> user.getUsername().equals("testuser"))
            .anyMatch(user -> user.getUsername().equals("testuser2"));
    }

    @Test
    void testCount_ShouldReturnTotalUsers() {
        testEntityManager.persistAndFlush(testUser);

        long count = userRepository.count();

        assertThat(count).isGreaterThanOrEqualTo(1L);
    }
}
