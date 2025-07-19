package com.chatapp.synk.service.impl;

import com.chatapp.synk.dto.UsersDTO;
import com.chatapp.synk.entity.Users;
import com.chatapp.synk.exceptionHandler.ServiceException;
import com.chatapp.synk.repository.UsersRepository;
import com.chatapp.synk.service.UsersService;
import com.chatapp.synk.util.Mapper;
import com.chatapp.synk.util.RandomUUIDGenerater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsersServiceImpl implements UsersService {
    private static final Logger logger = LoggerFactory.getLogger(UsersServiceImpl.class);

    @Autowired
    private UsersRepository usersRepository;
    @Override
    @CachePut(value = "userCache", key = "#userDTO.phoneNumber")
    public UsersDTO createUser(UsersDTO userDTO) {
        logger.info("Creating new user with phone: {}", userDTO.getPhoneNumber());
        Users user = Mapper.mapToUserEntity(userDTO);
        Users savedUser = usersRepository.save(user);
        logger.info("Users saved successfully with ID: {}", savedUser.getId());
        return Mapper.mapToUserDTO(savedUser);
    }

    @Override
    @Cacheable(value = "userCache", key = "#userId", unless = "#result.isEmpty()")
    public Optional<UsersDTO> getUserById(String userId) {
        logger.info("Fetching user by ID: {}", userId);
        Optional<UsersDTO> result = usersRepository.findById(userId).map(Mapper::mapToUserDTO);

        if (result.isEmpty()) {
            logger.warn("No user found with ID: {}", userId);
        }

        return result;
    }

    @Override
    @Cacheable(value = "userCache", key = "#phoneNumber")
    public Optional<UsersDTO> getUserByPhoneNumber(String phoneNumber) {
        logger.info("Fetching user by phone number: {}", phoneNumber);
        Optional<UsersDTO> result = usersRepository.findByPhoneNumber(phoneNumber).map(Mapper::mapToUserDTO);

        if (result.isEmpty()) {
            logger.warn("No user found with phone number: {}", phoneNumber);
        }

        return result;
    }

    @Override
    @CacheEvict(value = "userCache", key = "#userDTO.id")
    public UsersDTO updateUser(String userId, UsersDTO userDTO) {
        logger.info("Updating users with ID: {}", userId);

        Optional<Users> optionalUser = usersRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            logger.error("Users not found while updating. ID: {}", userId);
            throw new ServiceException("Users not found with ID: " + userId);
        }

        Users users = optionalUser.get();
        users.setName(userDTO.getName());
        users.setProfilePictureUrl(userDTO.getProfilePictureUrl());
        users.setAbout(userDTO.getAbout());

        Users updatedUsers = usersRepository.save(users);
        logger.info("Users updated successfully. ID: {}", updatedUsers.getId());
        return Mapper.mapToUserDTO(updatedUsers);
    }

    @Override
    //caching not needed if we create new user it is not coming here
    //@Cacheable(value = "userCache", key = "#query != null ? #query : 'NULL_KEY_TO_GET_ALL_USERS'", unless = "#result == null or #result.isEmpty()")
    public List<UsersDTO> searchUsers(String query) {
        logger.info("Searching users with query: '{}'", query);

        List<Users> users;
        if (query == null || query.trim().isEmpty()) {
            logger.info("Query is blank. Fetching all users.");
            users = usersRepository.findAll();
        } else {
            users = usersRepository.findByNameContainingIgnoreCaseOrPhoneNumberContaining(query.trim(), query.trim());
            logger.info("Found {} user(s) matching query: '{}'", users.size(), query);
        }

        return users.stream().map(Mapper::mapToUserDTO).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "userCache", key = "#userId")
    public void deleteUser(String userId) {
        logger.info("Deleting user with ID: {}", userId);
        usersRepository.deleteById(userId);
        logger.info("Users with ID {} deleted successfully", userId);
    }
}
