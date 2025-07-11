package com.chatapp.synk.service;

import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.entity.User;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.repository.UserRepository;
import com.chatapp.synk.util.Mapper;
import com.chatapp.synk.util.RandomUUIDGenerater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final String ALIAS = "USER";

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        String generatedId = RandomUUIDGenerater.getId(ALIAS).toString();
        logger.info("Creating new user with ID: {} and phone: {}", generatedId, userDTO.getPhoneNumber());

        User user = new User();
        user.setId(generatedId);
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setName(userDTO.getName());
        user.setProfilePictureUrl(userDTO.getProfilePictureUrl());
        user.setAbout(userDTO.getAbout());

        User savedUser = userRepository.save(user);
        logger.info("User saved successfully with ID: {}", savedUser.getId());
        return Mapper.mapToDTO(savedUser);
    }

    @Override
    public Optional<UserDTO> getUserById(String userId) {
        logger.info("Fetching user by ID: {}", userId);
        Optional<UserDTO> result = userRepository.findById(userId).map(Mapper::mapToDTO);

        if (result.isEmpty()) {
            logger.warn("No user found with ID: {}", userId);
        }

        return result;
    }

    @Override
    public Optional<UserDTO> getUserByPhoneNumber(String phoneNumber) {
        logger.info("Fetching user by phone number: {}", phoneNumber);
        Optional<UserDTO> result = userRepository.findByPhoneNumber(phoneNumber).map(Mapper::mapToDTO);

        if (result.isEmpty()) {
            logger.warn("No user found with phone number: {}", phoneNumber);
        }

        return result;
    }

    @Override
    public UserDTO updateUser(String userId, UserDTO userDTO) {
        logger.info("Updating user with ID: {}", userId);

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            logger.error("User not found while updating. ID: {}", userId);
            throw new ServiceException("User not found with ID: " + userId);
        }

        User user = optionalUser.get();
        user.setName(userDTO.getName());
        user.setProfilePictureUrl(userDTO.getProfilePictureUrl());
        user.setAbout(userDTO.getAbout());

        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully. ID: {}", updatedUser.getId());
        return Mapper.mapToDTO(updatedUser);
    }

    @Override
    public List<UserDTO> searchUsers(String query) {
        logger.info("Searching users with query: '{}'", query);

        List<User> users;
        if (query == null || query.trim().isEmpty()) {
            logger.info("Query is blank. Fetching all users.");
            users = userRepository.findAll();
        } else {
            users = userRepository.findByNameContainingIgnoreCaseOrPhoneNumberContaining(query.trim(), query.trim());
            logger.info("Found {} user(s) matching query: '{}'", users.size(), query);
        }

        return users.stream().map(Mapper::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public void deleteUser(String userId) {
        logger.info("Deleting user with ID: {}", userId);
        userRepository.deleteById(userId);
        logger.info("User with ID {} deleted successfully", userId);
    }
}
