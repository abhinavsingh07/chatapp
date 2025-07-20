package com.chatapp.synk.service;

import com.chatapp.synk.dto.UserDTO;

import java.util.List;


public interface UserService {
    UserDTO createUser(UserDTO userDTO);

    UserDTO getUserById(String userId);

    UserDTO getUserByPhoneNumber(String phoneNumber);

    UserDTO updateUser(String userId, UserDTO userDTO);

    List<UserDTO> searchUsers(String query);

    void deleteUser(String userId);
}
