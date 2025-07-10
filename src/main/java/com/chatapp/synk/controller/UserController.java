package com.chatapp.synk.controller;

import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Create new user
    @PostMapping
    public ResponseEntity<SuccessResponse<UserDTO>> createUser( @Valid @RequestBody UserDTO userDTO) {
        UserDTO savedUser = userService.createUser(userDTO);
        return ResponseEntity.ok(new SuccessResponse<>("200", "User created successfully", List.of(savedUser)));
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<UserDTO>> getUserById(@PathVariable String id) {
        Optional<UserDTO> userOpt = userService.getUserById(id);
        return userOpt.map(user ->
                ResponseEntity.ok(new SuccessResponse<>("200", "User fetched", List.of(user)))
        ).orElse(ResponseEntity.ok(new SuccessResponse<>("404", "User not found", Collections.emptyList())));
    }

    // Get user by phone number
    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<SuccessResponse<UserDTO>> getUserByPhoneNumber(@PathVariable String phoneNumber) {
        Optional<UserDTO> userOpt = userService.getUserByPhoneNumber(phoneNumber);
        return userOpt.map(user ->
                ResponseEntity.ok(new SuccessResponse<>("200", "User fetched", List.of(user)))
        ).orElse(ResponseEntity.ok(new SuccessResponse<>("404", "User not found", Collections.emptyList())));
    }

    // Update user
    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<UserDTO>> updateUser(@PathVariable String id,@RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(new SuccessResponse<>("200", "User updated successfully", List.of(updatedUser)));
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new SuccessResponse<>("200", "User deleted successfully", Collections.emptyList()));
    }
}
