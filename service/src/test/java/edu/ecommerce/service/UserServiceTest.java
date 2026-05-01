package edu.ecommerce.service;

import edu.ecommerce.core.dto.UserRequest;
import edu.ecommerce.core.dto.UserResponse;
import edu.ecommerce.core.dto.UserUpdateRequest;
import edu.ecommerce.core.entity.User;
import edu.ecommerce.core.exception.DuplicateEmailException;
import edu.ecommerce.core.exception.UserNotFoundException;
import edu.ecommerce.service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserRequest userRequest;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setRole("USER");
        testUser.setStatus("ACTIVE");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        userRequest = new UserRequest();
        userRequest.setUsername("testuser");
        userRequest.setEmail("test@example.com");
        userRequest.setPassword("testPassword123");
        userRequest.setRole("USER");
    }

    @Test
    public void testCreateUser_Success() {
        // given
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(userRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(userRequest.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        UserResponse response = userService.createUser(userRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testCreateUser_DuplicateEmail() {
        // given
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.createUser(userRequest))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("Email already in use");
    }

    @Test
    public void testGetUserById_Success() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // when
        UserResponse response = userService.getUserById(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetUserById_NotFound() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserById(999L))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("User not found");
    }

    @Test
    public void testGetUserByEmail_Success() {
        // given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // when
        UserResponse response = userService.getUserByEmail("test@example.com");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    public void testGetUserByEmail_NotFound() {
        // given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserByEmail("nonexistent@example.com"))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    public void testUpdateUser_Success() {
        // given
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setUsername("updateduser");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setPassword("newPassword123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newPassword123")).thenReturn("hashedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        UserResponse response = userService.updateUser(1L, updateRequest);

        // then
        assertThat(response).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testDeleteUser_Success() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        // when
        userService.deleteUser(1L);

        // then
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    public void testDeleteUser_NotFound() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.deleteUser(999L))
            .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    public void testListAllUsers_Success() {
        // given
        List<User> userList = new ArrayList<>();
        userList.add(testUser);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(userList));

        // when
        Page<UserResponse> responses = userService.listAllUsers(null);

        // then
        assertThat(responses).isNotNull();
        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).getId()).isEqualTo(1L);
    }
}
