package com.chatapp.synk.controller;

import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.dto.UserStatusDTO;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.security.SecurityUtil;
import com.chatapp.synk.service.UserPresenceService;
import com.chatapp.synk.service.UserService;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final UserPresenceService userPresenceService;


    public UserController(UserService userService, UserPresenceService userPresenceService) {
        this.userService = userService;
        this.userPresenceService = userPresenceService;
    }

    @GetMapping("/all")
    public ResponseEntity<SuccessResponse<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        if (users.isEmpty()) {
            logger.warn("No users found");
            return ResponseEntity
                    .ok(new SuccessResponse<>(HttpStatus.NOT_FOUND, "No users found", Collections.emptyList()));
        }
        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "Users fetched successfully", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<UserDTO>> getUserById(@PathVariable String id) {
        UserDTO userOpt = userService.getUserById(id);
        if (userOpt != null) {
            return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "User fetched", List.of(userOpt)));
        } else {
            logger.warn("User with ID {} not found", id);
            return ResponseEntity
                    .ok(new SuccessResponse<>(HttpStatus.NOT_FOUND, "User not found", Collections.emptyList()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<UserDTO>> updateUser(@PathVariable String id, @RequestBody UserDTO userDTO) {
        // Ownership check: authenticated user can only update their own profile.
        String loggedInUserId = SecurityUtil.getCurrentUserIdFromSecurityContext();
        if (!id.equals(loggedInUserId)) {
            logger.warn("User [{}] attempted to update profile of user [{}]", loggedInUserId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new SuccessResponse<>(HttpStatus.FORBIDDEN, "Access denied: you can only update your own profile", Collections.emptyList()));
        }

        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity
                .ok(new SuccessResponse<>(HttpStatus.OK, "User updated successfully", List.of(updatedUser)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deleteUser(@PathVariable String id) {
        // Ownership check: authenticated user can only delete their own account.
        String loggedInUserId = SecurityUtil.getCurrentUserIdFromSecurityContext();
        if (!id.equals(loggedInUserId)) {
            logger.warn("User [{}] attempted to delete account of user [{}]", loggedInUserId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new SuccessResponse<>(HttpStatus.FORBIDDEN, "Access denied: you can only delete your own account", Collections.emptyList()));
        }

        userService.deleteUser(id);
        return ResponseEntity
                .ok(new SuccessResponse<>(HttpStatus.OK, "User deleted successfully", Collections.emptyList()));
    }

    @GetMapping("/lastActiveStatus")
    public ResponseEntity<SuccessResponse<UserStatusDTO>> getLastActiveUserStatus(@RequestParam String userId) {
        if (userId == null || userId.isEmpty()) {
            logger.warn("No user ID provided for status check");
            return ResponseEntity
                    .ok(new SuccessResponse<>(HttpStatus.BAD_REQUEST, "No user ID provided", Collections.emptyList()));
        }
        // this is will give the last active status of multiple users, as user can be
        // active in multiple devices, so we will return the list of status of all
        // devices
        List<UserStatusDTO> result = userPresenceService.getLastActiveUserStatus(userId);
        if (result.isEmpty()) {
            logger.warn("No status found for user ID {}", userId);
            return ResponseEntity
                    .ok(new SuccessResponse<>(HttpStatus.NOT_FOUND, "No status found", Collections.emptyList()));
        }
        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "User statuses fetched", result));
    }

    @GetMapping("/me")
    public ResponseEntity<SuccessResponse<UserDTO>> getUserMe() {
        //fetching userid from security context setting in jwtAuthFilter
        String userId = SecurityUtil.getCurrentUserIdFromSecurityContext();

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new SuccessResponse<>(HttpStatus.UNAUTHORIZED,"User is not authenticated",Collections.emptyList()));
        }

        UserDTO user = userService.getUserById(userId);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new SuccessResponse<>(HttpStatus.NOT_FOUND, "User not found", Collections.emptyList()));
        }

        return ResponseEntity.ok(new SuccessResponse<>(HttpStatus.OK, "User fetched successfully", List.of(user)));
    }
}
