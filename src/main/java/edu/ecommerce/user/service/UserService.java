package edu.ecommerce.user.service;

import edu.ecommerce.user.dto.UserRequest;
import edu.ecommerce.user.dto.UserResponse;
import edu.ecommerce.user.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest request);

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deleteUser(Long id);

    Page<UserResponse> listAllUsers(Pageable pageable);
}
