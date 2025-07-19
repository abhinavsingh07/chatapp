package com.chatapp.synk.controller;

import com.chatapp.synk.dto.UsersDTO;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.service.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    //Basic Setup with SLF4J logger
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UsersService usersService;

    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<UsersDTO>> searchUsers(@RequestParam(required = false) String query) {
        logger.info("Received search request with query: {}", query);

        List<UsersDTO> results = usersService.searchUsers(query);
        if (results.isEmpty()) {
            logger.warn("No users found for query: {}", query);
        } else {
            logger.info("Found {} users for query: {}", results.size(), query);
        }

        String msg = results.isEmpty() ? "No matching users found" : "Search results";
        String code = results.isEmpty() ? "204" : "200";

        return ResponseEntity.ok(new SuccessResponse<>(code, msg, results));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<UsersDTO>> getUserById(@PathVariable String id) {
        logger.info("Fetching user with ID: {}", id);

        Optional<UsersDTO> userOpt = usersService.getUserById(id);
        if (userOpt.isPresent()) {
            logger.info("Users with ID {} found", id);
            return ResponseEntity.ok(new SuccessResponse<>("200", "Users fetched", List.of(userOpt.get())));
        } else {
            logger.warn("Users with ID {} not found", id);
            return ResponseEntity.ok(new SuccessResponse<>("404", "Users not found", Collections.emptyList()));
        }
    }

    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<SuccessResponse<UsersDTO>> getUserByPhoneNumber(@PathVariable String phoneNumber) {
        logger.info("Fetching user with phone number: {}", phoneNumber);

        Optional<UsersDTO> userOpt = usersService.getUserByPhoneNumber(phoneNumber);
        if (userOpt.isPresent()) {
            logger.info("Users with phone {} found", phoneNumber);
            return ResponseEntity.ok(new SuccessResponse<>("200", "Users fetched", List.of(userOpt.get())));
        } else {
            logger.warn("Users with phone {} not found", phoneNumber);
            return ResponseEntity.ok(new SuccessResponse<>("404", "Users not found", Collections.emptyList()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<UsersDTO>> updateUser(@PathVariable String id, @RequestBody UsersDTO userDTO) {
        logger.info("Received update request for user ID: {}", id);

        UsersDTO updatedUser = usersService.updateUser(id, userDTO);
        logger.info("Users ID {} updated successfully", id);

        return ResponseEntity.ok(new SuccessResponse<>("200", "Users updated successfully", List.of(updatedUser)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deleteUser(@PathVariable String id) {
        logger.info("Received delete request for user ID: {}", id);

        usersService.deleteUser(id);
        logger.info("Users ID {} deleted successfully", id);

        return ResponseEntity.ok(new SuccessResponse<>("200", "Users deleted successfully", Collections.emptyList()));
    }
}
