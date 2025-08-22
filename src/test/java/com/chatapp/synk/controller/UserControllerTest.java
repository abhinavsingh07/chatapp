package com.chatapp.synk.controller;

import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserDTO mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserDTO();
        mockUser.setId("1");
        mockUser.setName("John Doe");
        mockUser.setEmail("john@example.com");
    }

    @Test
    void testGetAllUsers_whenUsersExist() {
        when(userService.getAllUsers()).thenReturn(List.of(mockUser));

        ResponseEntity<SuccessResponse<UserDTO>> response = userController.getAllUsers();

        assertEquals("200", response.getBody().getResponseCode());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("John Doe", response.getBody().getData().get(0).getName());
    }

    @Test
    void testGetAllUsers_whenNoUsersExist() {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        ResponseEntity<SuccessResponse<UserDTO>> response = userController.getAllUsers();

        assertEquals("404", response.getBody().getResponseCode());
        assertTrue(response.getBody().getData().isEmpty());
    }

    @Test
    void testGetUserById_whenUserExists() {
        when(userService.getUserById("1")).thenReturn(mockUser);

        ResponseEntity<SuccessResponse<UserDTO>> response = userController.getUserById("1");

        assertEquals("200", response.getBody().getResponseCode());
        assertEquals("John Doe", response.getBody().getData().get(0).getName());
    }

    @Test
    void testGetUserById_whenUserNotFound() {
        when(userService.getUserById("99")).thenReturn(null);

        ResponseEntity<SuccessResponse<UserDTO>> response = userController.getUserById("99");

        assertEquals("404", response.getBody().getResponseCode());
        assertTrue(response.getBody().getData().isEmpty());
    }

    @Test
    void testUpdateUser() {
        UserDTO updatedUser = new UserDTO();
        updatedUser.setId("1");
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("updated@example.com");

        when(userService.updateUser("1", mockUser)).thenReturn(updatedUser);

        ResponseEntity<SuccessResponse<UserDTO>> response = userController.updateUser("1", mockUser);

        assertEquals("200", response.getBody().getResponseCode());
        assertEquals("Updated Name", response.getBody().getData().get(0).getName());
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userService).deleteUser("1");

        ResponseEntity<SuccessResponse<Void>> response = userController.deleteUser("1");

        assertEquals("200", response.getBody().getResponseCode());
        assertEquals("User deleted successfully", response.getBody().getMessage());
        verify(userService, times(1)).deleteUser("1");
    }
}
