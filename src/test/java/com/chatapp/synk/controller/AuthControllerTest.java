package com.chatapp.synk.controller;

import com.chatapp.synk.dto.AuthDTO;
import com.chatapp.synk.dto.UserDTO;
import com.chatapp.synk.response.SuccessResponse;
import com.chatapp.synk.security.*;
import com.chatapp.synk.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private UserService userService;

    @Test
    void testAuthenticate_Success() throws Exception {

        // Arrange
        AuthDTO authDTO = new AuthDTO();
        authDTO.setPhoneNumberOrEmail("test@example.com");
        authDTO.setPassword("password123");

        CustomUserDetails realUser = new CustomUserDetails("test@example.com", "Test User", "password123", List.of(new SimpleGrantedAuthority("ROLE_USER")), "test@example.com", "http://example.com/profile.jpg", "12345");
        Authentication mockAuth = new PhoneNumberAuthenticationToken(realUser, "password123", realUser.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(jwtUtil.generateToken(Mockito.any(Map.class), Mockito.eq("test@example.com"))).thenReturn("mocked-jwt-token");

        // Act
        ResponseEntity<JwtResponse> response = authController.authenticate(authDTO);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("mocked-jwt-token", response.getBody().getJwtToken());
        assertEquals("Test User", response.getBody().getName());
        assertEquals("test@example.com", response.getBody().getEmail());
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        UserDTO inputUser = new UserDTO();
        inputUser.setId("12345");
        inputUser.setPhoneNumber("9999999999");
        inputUser.setEmail("test@example.com");
        inputUser.setPassword("secret");

        UserDTO savedUser = new UserDTO();
        savedUser.setId("12345");
        savedUser.setPhoneNumber("9999999999");
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("secret");

        when(userService.createUser(inputUser)).thenReturn(savedUser);

        // Act
        ResponseEntity<SuccessResponse<UserDTO>> response = authController.createUser(inputUser);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("200", response.getBody().getResponseCode().toString());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("********", response.getBody().getData().get(0).getPassword()); // masked
    }

    @Test
    void testRegisterUser_Failure() {
        // Arrange
        UserDTO inputUser = new UserDTO();
        inputUser.setPhoneNumber("9999999999");
        inputUser.setEmail("test@example.com");

        when(userService.createUser(inputUser)).thenThrow(new RuntimeException("DB error"));

        // Act
        ResponseEntity<SuccessResponse<UserDTO>> response = authController.createUser(inputUser);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertEquals("500", response.getBody().getResponseCode().toString());
        assertTrue(response.getBody().getData().isEmpty());
    }
}
