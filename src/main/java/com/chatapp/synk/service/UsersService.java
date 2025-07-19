package com.chatapp.synk.service;

import com.chatapp.synk.dto.UsersDTO;

import java.util.List;
import java.util.Optional;


public interface UsersService {
    UsersDTO createUser(UsersDTO userDTO);

    Optional<UsersDTO> getUserById(String userId);

    Optional<UsersDTO> getUserByPhoneNumber(String phoneNumber);

    UsersDTO updateUser(String userId, UsersDTO userDTO);

    List<UsersDTO> searchUsers(String query);

    void deleteUser(String userId);
}
