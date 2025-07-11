package com.chatapp.synk.service;

import com.chatapp.synk.dto.UserDTO;

import java.util.List;
import java.util.Optional;


public interface UserService {
    // Create/register user
    UserDTO createUser(UserDTO userDTO);

    // Get user by ID
    Optional<UserDTO> getUserById(String userId);

    // Get user by phone number
    Optional<UserDTO> getUserByPhoneNumber(String phoneNumber);

    // Update user profile
    UserDTO updateUser(String userId, UserDTO userDTO);

    // Search users by name or phone
    List<UserDTO> searchUsers(String query);

    // Delete user by ID
    void deleteUser(String userId);
}
