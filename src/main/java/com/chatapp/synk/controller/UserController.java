package com.chatapp.synk.controller;

import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<UserDTO>> searchUsers(@RequestParam(required = false) String query) {
        // ?query=abhi or ?query=9953313628
        List<UserDTO> results = userService.searchUsers(query);
        String msg = results.isEmpty() ? "No matching users found" : "Search results";
        String code = results.isEmpty() ? "204" : "200";

        return ResponseEntity.ok(
                new SuccessResponse<>(code, msg, results)
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<UserDTO>> getUserById(@PathVariable String id) {
        Optional<UserDTO> userOpt = userService.getUserById(id);
        return userOpt.map(user ->
                ResponseEntity.ok(new SuccessResponse<>("200", "User fetched", List.of(user)))
        ).orElse(ResponseEntity.ok(new SuccessResponse<>("404", "User not found", Collections.emptyList())));
    }

    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<SuccessResponse<UserDTO>> getUserByPhoneNumber(@PathVariable String phoneNumber) {
        Optional<UserDTO> userOpt = userService.getUserByPhoneNumber(phoneNumber);
        return userOpt.map(user ->
                ResponseEntity.ok(new SuccessResponse<>("200", "User fetched", List.of(user)))
        ).orElse(ResponseEntity.ok(new SuccessResponse<>("404", "User not found", Collections.emptyList())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<UserDTO>> updateUser(@PathVariable String id,@RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(new SuccessResponse<>("200", "User updated successfully", List.of(updatedUser)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<Void>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new SuccessResponse<>("200", "User deleted successfully", Collections.emptyList()));
    }
}
