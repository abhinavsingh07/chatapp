package com.chatapp.synk.service;

import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.entity.User;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.repository.UserRepository;
import com.chatapp.synk.util.Mapper;
import com.chatapp.synk.util.RandomUUIDGenerater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final String ALIAS="USER";

    // Constructor injection
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Create new user
    @Override
    public UserDTO createUser(UserDTO userDTO) {
        User user = new User();
        String generatedId= RandomUUIDGenerater.getId(ALIAS).toString();
        user.setId(generatedId);
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setName(userDTO.getName());
        user.setProfilePictureUrl(userDTO.getProfilePictureUrl());
        user.setAbout(userDTO.getAbout());

        User savedUser = userRepository.save(user);
        return Mapper.mapToDTO(savedUser);
    }

    // Get user by ID
    @Override
    public Optional<UserDTO> getUserById(String userId) {
        return userRepository.findById(userId)
                .map(Mapper::mapToDTO);
    }

    // Get user by phone number
    @Override
    public Optional<UserDTO> getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .map(Mapper::mapToDTO);
    }

    // Update user
    @Override
    public UserDTO updateUser(String userId, UserDTO userDTO) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new ServiceException("User not found with ID: " + userId);
        }

        User user = optionalUser.get();
        user.setName(userDTO.getName());
        user.setProfilePictureUrl(userDTO.getProfilePictureUrl());
        user.setAbout(userDTO.getAbout());

        User updatedUser = userRepository.save(user);
        return Mapper.mapToDTO(updatedUser);
    }


    // Delete user
    @Override
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

}
