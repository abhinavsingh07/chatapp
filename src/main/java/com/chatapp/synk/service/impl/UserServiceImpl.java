package com.chatapp.synk.service.impl;

import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.entity.User;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.repository.UserRepository;
import com.chatapp.synk.service.UserService;
import com.chatapp.synk.util.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @CachePut(value = "userCache", key = "#result.phoneNumber")
    public UserDTO createUser(UserDTO userDTO) {
        logger.info("Creating new user with phone: {}", userDTO.getPhoneNumber());
        User user = Mapper.mapToUserEntity(userDTO);
        User savedUser = userRepository.save(user);
        logger.info("User saved successfully with ID: {}", savedUser.getId());
        return Mapper.mapToUserDTO(savedUser);
    }

    @Override
    @Cacheable(value = "userCache", key = "#userId", unless = "#result == null")
    public UserDTO getUserById(String userId) {
        logger.info("Fetching user by ID: {}", userId);
        Optional<UserDTO> result = userRepository.findById(userId).map(Mapper::mapToUserDTO);

        if (result.isEmpty()) {
            logger.warn("No user found with ID: {}", userId);
            return null;
        }
        return result.get();
    }

    @Override
    @Cacheable(value = "userCache", key = "#phoneNumber" , unless = "#result == null")
    public UserDTO getUserByPhoneNumber(String phoneNumber) {
        logger.info("Fetching user by phone number: {}", phoneNumber);
        Optional<UserDTO> result = userRepository.findByPhoneNumber(phoneNumber).map(Mapper::mapToUserDTO);

        if (result.isEmpty()) {
            logger.warn("No user found with phone number: {}", phoneNumber);
        }

        return result.get();
    }

    @Override
    @CacheEvict(value = "userCache", key = "#userDTO.id")
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
        return Mapper.mapToUserDTO(updatedUser);
    }

    @Override
    //caching not needed if we create new user it is not coming here
    //@Cacheable(value = "userCache", key = "#query != null ? #query : 'NULL_KEY_TO_GET_ALL_USERS'", unless = "#result == null or #result.isEmpty()")
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

        return users.stream().map(Mapper::mapToUserDTO).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "userCache", key = "#userId")
    public void deleteUser(String userId) {
        logger.info("Deleting user with ID: {}", userId);
        userRepository.deleteById(userId);
        logger.info("User with ID {} deleted successfully", userId);
    }
}
