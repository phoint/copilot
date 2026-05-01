package edu.ecommerce.service;

import edu.ecommerce.core.dto.UserRequest;
import edu.ecommerce.core.dto.UserResponse;
import edu.ecommerce.core.dto.UserUpdateRequest;
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
